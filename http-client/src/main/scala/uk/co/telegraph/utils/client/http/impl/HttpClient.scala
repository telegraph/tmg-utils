package uk.co.telegraph.utils.client.http.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging.{DebugLevel, LogLevel}
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK, RequestTimeout}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.scaladsl.Sink.head
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{Materializer, Supervision}
import com.typesafe.config.{Config, ConfigFactory}
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.http.exceptions.{HttpFailureException, HttpInvalidStatusException}
import uk.co.telegraph.utils.client.http.impl.HttpClient._
import uk.co.telegraph.utils.client.http.impl.settings.HttpSettings
import uk.co.telegraph.utils.client.http.scaladsl.HttpContext
import uk.co.telegraph.utils.client.models.ClientDetails

import scala.collection.convert.WrapAsJava._
import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}


trait HttpClient extends GenericClient {

  protected implicit val system:ActorSystem
  protected implicit val materializer:Materializer

  /**
    * Collect Http Endpoints
    */
  protected val settings:HttpSettings
  protected val endpointConfig:Config

  protected lazy val logging       : LoggingAdapter = system.log
  private   lazy val defaultHeaders: List[RawHeader] = settings.defaultHeaders.map(x => RawHeader(x._1, x._2)).toList
  private   lazy val defaultConfig : Config = ConfigFactory.parseMap(Map(
    "name" -> this.getClass.getSimpleName,
    "ping" -> s"curl ${settings.defaultHeaders.map(x => s"""-H "${x._1}: ${x._2}"""").mkString(" ")} -X ${settings.health.method.value} ${settings.baseUrl}${settings.health.path} -v"
  ))
  protected lazy val logLevel      : LogLevel = DebugLevel

  /**
    * Set the Connector type
    */
  protected lazy val httpConnectorFlow:HttpConnector = {
    if (settings.isSecure)
      Http().cachedHostConnectionPoolHttps(
        host     = settings.host,
        port     = settings.port,
        settings = settings.connectionPool
      )(materializer)
    else
      Http().cachedHostConnectionPool(
        host     = settings.host,
        port     = settings.port,
        settings = settings.connectionPool
      )(materializer)
  }

  /**
    * Sets the Supervision Decider
    */
  private val decider: Supervision.Decider = _ => Supervision.Stop

  /**
    * Basic HttpRequest -> HttpResponse Flow
    */
  lazy val httpClientFlow:Flow[HttpRequest, HttpContext, NotUsed] = Flow[HttpRequest]
    .map( logRequest(logging, logLevel) )
    .map( appendDefaultHeaders )
    .map( request => (request, request))
    .via( httpConnectorFlow )
    .flatMapConcat({
      case (Success(response), request) =>
        logResponse(logging, logLevel)(response)
        Source.single( HttpContext(request, response) )
      case (Failure(error),    request) =>
        Source.failed(HttpFailureException(request, error))
    })
    .withAttributes(supervisionStrategy(decider))

  /**
    * Allow us to add default headers like Authentication headers to the
    * request using just configuration
    */
  private def appendDefaultHeaders(request:HttpRequest):HttpRequest = {
    val headersToAdd = defaultHeaders.filterNot( head => {
      request.headers.map(_.lowercaseName).contains(head.lowercaseName)
    })
    request.addHeaders(headersToAdd)
  }

  /**
    * Get Details from Client Flow
    */
  protected lazy val getDetailsFlow:Flow[FiniteDuration, ClientDetails, NotUsed] = {
    val detailRequest = HttpRequest( method = settings.health.method, uri = s"${settings.baseUrl}${settings.health.path}")
    val clientConfig  = endpointConfig.withFallback(defaultConfig)

    Flow[FiniteDuration]
      .flatMapConcat( timeout => Source.single( detailRequest )
        .via( httpClientFlow.filterByStatus(OK).ignorePayload )
        .map( _ => ClientDetails(200, clientConfig) )
        .completionTimeout( timeout )
      )
      .recover({
        case ex:HttpInvalidStatusException => ClientDetails(ex.statusCode.intValue,       clientConfig, ex)
        case ex:TimeoutException           => ClientDetails(RequestTimeout.intValue,      clientConfig, ex)
        case ex:Throwable                  => ClientDetails(InternalServerError.intValue, clientConfig, ex)
      })
  }

  /**
    * Return Client Details
    */
  override def getDetails(implicit timeout: FiniteDuration = 5 seconds): Future[ClientDetails] =
    Source.single(timeout).via(getDetailsFlow).runWith(head)

  @deprecated("This method is deprecated since the name is not that meaningful. Please use 'execRequest'", "1.0.1-bxx")
  def single(request: HttpRequest)(implicit ex:ExecutionContext):Future[HttpResponse] =
    execRequest(request)

  @deprecated("This method is deprecated since the name is not that meaningful. Please use 'execRequestAndGetContext'", "1.0.1-bxx")
  def singleWithRequest(request: HttpRequest):Future[HttpContext] =
    execRequestAndGetContext(request)

  /**
    * Executes a single http Request and returns the HttpResponse
    */
  implicit def execRequest(request:HttpRequest)(implicit ex:ExecutionContext):Future[HttpResponse] = {
    execRequestAndGetContext(request:HttpRequest).map(_.response)
  }
  /**
    * Executes a single http Request and returns the context (HttpRequest + HttpResponse)
    */
  implicit def execRequestAndGetContext(request:HttpRequest):Future[HttpContext] = {
    Source.single(request).via(httpClientFlow).runWith(head)
  }

  /**
    * Method to log Http Responses
    */
  private def logResponse(logging:LoggingAdapter, level:LogLevel): (HttpResponse) => HttpResponse =
    logValue[HttpResponse](logging, level)
}

object HttpClient{

  type HttpConnector =
    Flow[(HttpRequest, HttpRequest), (Try[HttpResponse], HttpRequest), HostConnectionPool]
}

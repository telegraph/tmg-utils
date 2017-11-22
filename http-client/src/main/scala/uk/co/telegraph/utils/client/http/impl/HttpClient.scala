package uk.co.telegraph.utils.client.http.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging.{DebugLevel, LogLevel}
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Sink.head
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{ActorAttributes, Materializer, Supervision}
import com.typesafe.config.{Config, ConfigFactory}
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.http.exceptions.HttpFailureException
import uk.co.telegraph.utils.client.http.impl.HttpClient._
import uk.co.telegraph.utils.client.http.impl.settings.HttpSettings
import uk.co.telegraph.utils.client.http.scaladsl.HttpContext
import uk.co.telegraph.utils.client.models.ClientDetails

import scala.collection.convert.WrapAsJava._
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

trait HttpClient extends GenericClient {

  implicit val system:ActorSystem
  implicit val materializer:Materializer
  import system.dispatcher

  /**
    * Collect Http Endpoints
    */
  val settings:HttpSettings
  val endpointConfig:Config

  lazy val logging       : LoggingAdapter = system.log
  lazy val defaultHeaders: List[RawHeader] = settings.defaultHeaders.map(x => RawHeader(x._1, x._2)).toList
  lazy val defaultConfig : Config = ConfigFactory.parseMap(Map(
    "name" -> this.getClass.getSimpleName,
    "ping" -> s"curl ${settings.defaultHeaders.map(x => s"""-H "${x._1}: ${x._2}"""").mkString(" ")} -X ${settings.health.method.value} ${settings.baseUrl}${settings.health.path} -v"
  ))
  lazy val logLevel      : LogLevel = DebugLevel

  /**
    * Set the Connector type
    */
  lazy val httpConnectorFlow:HttpConnector = {
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
  protected val decider: Supervision.Decider = _ => Supervision.Resume

  /**
    * Basic HttpRequest -> HttpResponse Flow
    */
  lazy val httpClientFlow:Flow[HttpRequest, HttpContext, NotUsed] = Flow[HttpRequest]
    .map( logRequest(logging, logLevel) )
    .map( x => (x, x))
    .via( httpConnectorFlow )
    .flatMapConcat({
      case (Success(response), request) =>
        logResponse(logging, logLevel)(response)
        Source.single( HttpContext(request, response) )
      case (Failure(error),    request) =>
        Source.failed(HttpFailureException(request, error))
    })
    .withAttributes(ActorAttributes.supervisionStrategy(decider))

  /**
    * Get Details from Client Flow
    */
  lazy val getDetailsFlow:Flow[FiniteDuration, ClientDetails, NotUsed] = Flow[FiniteDuration]
    .flatMapConcat( timeout => Source
      .single(HttpRequest( method = settings.health.method, uri = s"${settings.baseUrl}${settings.health.path}") )
      .via( httpClientFlow.ignorePayload )
      .map( _ => ClientDetails(200, endpointConfig.withFallback(defaultConfig)) )
      .completionTimeout( timeout )
    )
    .recover({
      case ex:Throwable => ClientDetails(404, endpointConfig.withFallback(defaultConfig), ex)
    })

  /**
    * Return Client Details
    */
  override def getDetails(implicit timeout: FiniteDuration = 5 seconds): Future[ClientDetails] =
    Source.single(timeout).via(getDetailsFlow).runWith(head)

  /**
    * Executes a single http Request
    */
  implicit def single(request: HttpRequest):Future[HttpResponse] =
    singleWithRequest(request).map(_.response)

  implicit def singleWithRequest(request: HttpRequest):Future[HttpContext] =
    Source.single(request).via(httpClientFlow).runWith(head)

  /**
    * Method to log Http Responses
    */
  def logResponse(logging:LoggingAdapter, level:LogLevel): (HttpResponse) => HttpResponse = logValue[HttpResponse](logging, level)
}

object HttpClient{

  type HttpConnector =
    Flow[(HttpRequest, HttpRequest), (Try[HttpResponse], HttpRequest), HostConnectionPool]
}

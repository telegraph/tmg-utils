package uk.co.telegraph.utils.client.http.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import org.json4s.Formats
import org.json4s.jackson.JsonMethods
import uk.co.telegraph.utils.client.models.ClientDetails

import scala.concurrent.Future
import scala.concurrent.Future.failed
import scala.concurrent.duration.FiniteDuration
import scala.util.{ Failure, Success, Try }


class SimpleHttpClient(httpClient: HttpClient)(implicit val _actorSystem: ActorSystem, implicit val _materializer: Materializer) {

  import _actorSystem.dispatcher

  def send(httpRequest: HttpRequest): Future[SimpleResponse] = {
    sendRequest(httpRequest).flatMap(response => Unmarshal(response.entity).to[String].map(body => (response.status, body)))
      .map { case (statusCode, body) =>
        if (!statusCode.isSuccess()) throw new UnsuccessfulResponseException(httpRequest, statusCode.intValue(), body)
        else SimpleResponse(statusCode.intValue(), body)
      }.recoverWith {
      case e: UnsuccessfulResponseException => failed(e)
      case e: Exception => failed(new SimpleHttpClientException(httpRequest, e))
    }
  }

  private def sendRequest(httpRequest: HttpRequest): Future[HttpResponse] = {
    Try {
      httpClient.single(httpRequest)
    } match {
      case Success(response) => response
      case Failure(exc) => throw new RejectedRequestException(httpRequest, exc)
    }
  }

  def getDetails(implicit timeout: FiniteDuration): Future[ClientDetails] = httpClient.getDetails
}

case class SimpleResponse(statusCode: Int, body: String) {
  def bodyAs[A](implicit formats: Formats, mf: scala.reflect.Manifest[A]): A = JsonMethods.parse(body).extract[A]
}

case class UnsuccessfulResponseException(val request: HttpRequest, val statusCode: Int, val responseBody: String)
  extends RuntimeException(s"An error occurred requesting $request, returned code $statusCode and body [$responseBody]")

case class RejectedRequestException(val request: HttpRequest, exception: Throwable)
  extends RuntimeException(s"The http client rejected request $request and failed immediately", exception)

case class SimpleHttpClientException(val request: HttpRequest, exception: Exception)
  extends RuntimeException(s"An exception occurred while requesting $request", exception)
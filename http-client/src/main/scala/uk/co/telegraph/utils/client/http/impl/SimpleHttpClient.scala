package uk.co.telegraph.utils.client.http.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import uk.co.telegraph.utils.client.models.ClientDetails

import scala.concurrent.Future
import scala.concurrent.Future.failed
import scala.concurrent.duration.FiniteDuration


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

  private def sendRequest(httpRequest: HttpRequest) =
    try {
      httpClient.single(httpRequest)
    } catch {
      case e: Exception => throw new RejectedRequestException(httpRequest, e)
    }

  def getDetails(implicit timeout: FiniteDuration): Future[ClientDetails] = httpClient.getDetails
}

case class SimpleResponse(statusCode: Int, body: String)

class UnsuccessfulResponseException(val request: HttpRequest, val statusCode: Int, val responseBody: String)
  extends RuntimeException(s"An error occurred requesting $request, returned code $statusCode and body [$responseBody]")

class RejectedRequestException(val request: HttpRequest, exception: Exception)
  extends RuntimeException(s"The http client rejected request $request and failed immediately", exception)

class SimpleHttpClientException(val request: HttpRequest, exception: Exception)
  extends RuntimeException(s"An exception occurred while requesting $request", exception)
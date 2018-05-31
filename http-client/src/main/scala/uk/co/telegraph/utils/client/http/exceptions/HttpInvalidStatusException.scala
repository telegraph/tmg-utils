package uk.co.telegraph.utils.client.http.exceptions

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode}

class HttpInvalidStatusException(message:String, cause:Throwable, statusCode:StatusCode = InternalServerError) extends HttpClientException(message, cause)

object HttpInvalidStatusException{

  private [exceptions] def errorMsgFormatter(httpRequest: HttpRequest, httpResponse: HttpResponse, cause:Throwable = null) =
    s"Request [${httpRequest.method.name} ${httpRequest.uri}] failed with $httpResponse. Cause: '${Option(cause).map(_.getMessage).getOrElse("-")}'"

  def apply(httpRequest: HttpRequest, httpResponse: HttpResponse, cause:Throwable = null, statusCode:StatusCode = InternalServerError):HttpClientException =
    new HttpInvalidStatusException(errorMsgFormatter(httpRequest, httpResponse, cause), cause, statusCode)
}

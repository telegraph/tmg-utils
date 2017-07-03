package uk.co.telegraph.utils.client.http.exceptions

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

class HttpInvalidStatusException(message:String, cause:Throwable) extends HttpClientException(message, cause)

object HttpInvalidStatusException{

  private [exceptions] def errorMsgFormatter(httpRequest: HttpRequest, httpResponse: HttpResponse, cause:Throwable = null) =
    s"Request [${httpRequest.method.name} ${httpRequest.uri}] failed with $httpResponse. Cause: '${Option(cause).map(_.getMessage).getOrElse("-")}'"

  def apply(httpRequest: HttpRequest, httpResponse: HttpResponse, cause:Throwable = null):HttpClientException =
    new HttpInvalidStatusException(errorMsgFormatter(httpRequest, httpResponse, cause), cause)
}
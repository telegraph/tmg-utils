package uk.co.telegraph.utils.client.http.exceptions

import akka.http.scaladsl.model.HttpRequest

case class HttpFailureException(message:String, cause:Throwable) extends HttpClientException(message, cause)

object HttpFailureException{
  private [exceptions] def errorMsgFormatter(httpRequest: HttpRequest, cause:Throwable = null) =
    s"Request [${httpRequest.method.name} ${httpRequest.uri}] failed and no response was returned. Cause: '${Option(cause).map(_.getMessage).getOrElse("-")}'"

  def apply(httpRequest: HttpRequest, cause:Throwable = null):HttpClientException =
    HttpFailureException(errorMsgFormatter(httpRequest, cause), cause)
}

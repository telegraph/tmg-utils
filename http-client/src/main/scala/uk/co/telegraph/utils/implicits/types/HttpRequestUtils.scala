package uk.co.telegraph.utils.implicits.types

import akka.http.scaladsl.model.{HttpHeader, HttpRequest}

trait HttpRequestUtils {
  /**
    *
    * @param req the [[HttpRequest]] for operation
    */
  implicit class RichHttpRequest(req: HttpRequest) {

    /**
      * Will apply headers to the request if present
      */
    def withOptHeaders(optHeaders: Option[Seq[HttpHeader]]): HttpRequest = {
      optHeaders.map(req.withHeaders(_:_*)).getOrElse(req)
    }
  }
}
package uk.co.telegraph.utils.client.http

import akka.NotUsed
import akka.stream.scaladsl.Flow
import scala.language.implicitConversions

package object scaladsl {

  implicit def toHttpClientFlowExtensions[In](left:Flow[In, HttpContext, NotUsed]): HttpClientFlowExtensions[In] = {
    HttpClientFlowExtensions[In](left)
  }
}

package uk.co.telegraph.utils.client.http

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

package object scaladsl {

  implicit def toHttpClientFlowExtensions[In](left:Flow[In, HttpContext, NotUsed])(implicit mat:Materializer): HttpClientFlowExtensions[In] = {
    HttpClientFlowExtensions[In](left)
  }

  implicit def toHttpClientFutureExtensions(left:Future[HttpContext])(implicit ec:ExecutionContext, mat:Materializer):HttpClientFutureExtensions =
    HttpClientFutureExtensions(left)
}

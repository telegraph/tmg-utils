package uk.co.telegraph.utils.client.http.impl

import akka.http.scaladsl.model.{HttpHeader, HttpMethods, HttpRequest}
import akka.stream.scaladsl.Sink
import org.json4s.jackson.JsonMethods
import uk.co.telegraph.utils.implicits.types.HttpRequestUtils
import scala.concurrent.{ExecutionContext, Future}

trait JsonHttpClient
  extends HttpClient
    with JsonMethods
    with HttpRequestUtils {

  implicit def executionContext: ExecutionContext

  protected def get[T](url: String, optHeaders: Option[Seq[HttpHeader]] = None)(fn: String => T): Future[T] = {
    single(
      HttpRequest()
        .withMethod(HttpMethods.GET)
        .withUri(url)
        .withOptHeaders(optHeaders)
    ).flatMap(
      _.entity.getDataBytes().runWith(Sink.head, materializer).map(str =>
        fn(str.utf8String)
      )
    )
  }
}

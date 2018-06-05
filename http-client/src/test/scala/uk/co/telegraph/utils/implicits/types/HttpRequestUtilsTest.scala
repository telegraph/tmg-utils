package uk.co.telegraph.utils.implicits.types

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpRequest
import org.scalatest.{FreeSpec, Matchers}

class HttpRequestUtilsTest extends FreeSpec
  with HttpRequestUtils
  with Matchers {

  "Implicitly append optional [Seq[HttpHeader]]" - {
    "withOptHeaders - Some" - {
      val optHeaders = Some(Seq(headers.`Content-Type`(ContentTypes.`application/json`)))

      val actual = HttpRequest().withOptHeaders(optHeaders).getHeaders()
      val expected = HttpRequest().withHeaders(optHeaders.get:_*).getHeaders()

      actual shouldBe expected
    }
    "withOptHeaders - None" - {
      val optHeaders = None

      val actual = HttpRequest().withOptHeaders(optHeaders).getHeaders()
      val expected = HttpRequest().getHeaders()

      actual shouldBe expected
    }
  }
}

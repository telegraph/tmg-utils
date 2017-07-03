package uk.co.telegraph.utils.server.directives

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{FunSpec, Matchers}

class UuidDirectivesTest extends FunSpec with Matchers with ScalatestRouteTest{

  import DirectivesExt._

  val SampleRoute = get{
      withEventId{ eventId =>
        complete(eventId)
      }
    }

  describe("Given the UuidDirective, "){
    it("I should be able to generate a new Uuid if not present in the header"){
      Get() ~> SampleRoute ~> check {
        responseAs[String] should not be "11111111-1111-1111-1111-111111111111"
      }
    }

    it("I should be able to reuse the Uuid if present in the header"){
      val headers = List(RawHeader("x-event-id", "11111111-1111-1111-1111-111111111111"))
      Get().withHeaders(headers) ~> SampleRoute ~> check {
        responseAs[String] shouldBe "11111111-1111-1111-1111-111111111111"
      }
    }
  }
}

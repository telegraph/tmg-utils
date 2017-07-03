package uk.co.telegraph.utils.server.directives

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}
import uk.co.telegraph.utils.server.directives.DirectivesExt._

class CorsDirectiveTest extends FunSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterAll
{

  import CorsDirectiveTest._

  val SampleAllCorsRoute =
    allowAllCORS{
      get{
        complete("test")
      }
    }

  val SampleSomeCorsRoute =
    allowOriginsCORS("http://www.test.com"){
      get{
        complete("test")
      }
    }

  describe("Given the 'CorsDirective' and a route that allows all addresses, "){
    it("I should be able to use CORS for all requests"){
      Options() ~> SampleAllCorsRoute ~> check {
        response.status shouldBe StatusCodes.OK
        response.headers shouldBe ExpectedAllCorsHeaders
      }
    }
  }

  describe("Given the 'CorsDirective' and a route that does allows only some addresses, "){
    it("I should be able to use CORS for a specific Origin"){
      val headers = List(
        RawHeader("Origin", "www.test.com")
      )
      Options("www.test.com").withHeaders(headers) ~> SampleSomeCorsRoute ~> check {
        response.status shouldBe StatusCodes.OK
        response.headers shouldBe ExpectedSomeCorsHeaders
      }
    }
  }
}

object CorsDirectiveTest {


  val GenericHeaders = List(
    `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT, HttpMethods.DELETE, HttpMethods.OPTIONS),
    `Access-Control-Allow-Headers`(
      Authorization.name,
      Origin.name,
      Accept.name,
      Host.name,
      `Content-Type`.name,
      `Accept-Encoding`.name,
      `Accept-Language`.name,
      `User-Agent`.name
    ),
    `Access-Control-Allow-Credentials`(true)
  )

  val ExpectedAllCorsHeaders = List(`Access-Control-Allow-Origin`.*) ++ GenericHeaders
  val ExpectedSomeCorsHeaders = List(`Access-Control-Allow-Origin`("http://www.test.com")) ++ GenericHeaders
}

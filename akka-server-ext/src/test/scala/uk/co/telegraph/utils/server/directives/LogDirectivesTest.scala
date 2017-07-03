package uk.co.telegraph.utils.server.directives

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

class LogDirectivesTest extends FunSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterAll
{

  import DirectivesExt._

  val SampleRoute = get{
    logRoute("11111111-1111-1111-1111-111111111111"){
        complete("test")
      }
    }

//  val baos = new ByteArrayOutputStream()
//  val ps = new PrintStream(baos)
//  val old = System.out

//  override def beforeAll(): Unit =
//    System.setOut(ps)
//
//  override def afterAll(): Unit =
//    System.setOut(old)

  describe("Given the LogDirective, "){
    it("I should be able to log both request and responses"){
      Get() ~> SampleRoute ~> check {
        responseAs[String] shouldBe "test"
//        System.out.flush()
//        val Array(requestLog, responseLog) = baos.toString.split("\n")

//        requestLog  should endWith ("HttpRequest(HttpMethod(GET),http://example.com/,List(),HttpEntity.Strict(none/none,ByteString()),HttpProtocol(HTTP/1.1))")
//        responseLog should endWith ("Complete(HttpResponse(200 OK,List(),HttpEntity.Strict(text/plain; charset=UTF-8,test),HttpProtocol(HTTP/1.1)))")
      }
    }
  }
}

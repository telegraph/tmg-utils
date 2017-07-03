package uk.co.telegraph.utils.server.filters

import java.util.Base64.getEncoder

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest._
import AuthFilterTest._
import play.api.Configuration
import play.api.http.HttpEntity
import play.api.mvc.{RequestHeader, ResponseHeader, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future
import scala.concurrent.Future.successful

class AuthFilterTest
  extends FunSpec
  with Matchers
  with ScalaFutures
  with OneInstancePerTest
  with BeforeAndAfter
  with BeforeAndAfterAll
{

  override def afterAll():Unit = {
    MaterializerTest.shutdown()
    ActorSystemTest.terminate()
  }

  val mapper:RequestHeader => Future[Result] = _ => {
    successful(SampleResult)
  }
  describe("Given the AuthFilter with config, "){

    val filter = new AuthFilter(ConfigurationTest)(MaterializerTest)

    it("when receiving a non secure route, my request should be processed."){
      val request = FakeRequest("Get", "/health")
      whenReady(filter(mapper)(request)){ res =>
        res shouldBe SampleResult
      }
    }

    it("when receiving a secure route with the right password, my request should be processed."){
      val request = FakeRequest("Get", "/admin")
        .withHeaders(("Authorization", "Basic dXNlcjE6cGFzczE="))

      whenReady(filter(mapper)(request)){ res =>
        res shouldBe SampleResult
      }
    }

    it("when receiving a secure route with the wrong password, my request should not be processed."){
      val request = FakeRequest("Get", "/admin")
        .withHeaders(("Authorization", "Basic " + getEncoder.encode(s"use1:pass2".getBytes).toString))

      whenReady(filter(mapper)(request)){ res =>
        res shouldBe AuthFilter.UnauthorizedResponse.value.get.get
      }
    }
  }

  describe("Given the AuthFilter without config, "){

    val filter = new AuthFilter(Configuration.empty)(MaterializerTest)

    it("the default config should be used and block /admin"){
      val request = FakeRequest("Get", "/admin")
        .withHeaders(("Authorization", "Basic YWRtaW46YWRtaW4="))

      whenReady(filter(mapper)(request)){ res =>
        res shouldBe SampleResult
      }
    }

  }
}

object AuthFilterTest {

  val ConfigTest = ConfigFactory.load("application.conf")
  val ConfigurationTest:Configuration = Configuration(ConfigTest)
  implicit val ActorSystemTest = ActorSystem("auth-filter")
  implicit val MaterializerTest = ActorMaterializer()

  val SampleResult = Result(
    header = ResponseHeader(200),
    body   = HttpEntity.NoEntity
  )
}

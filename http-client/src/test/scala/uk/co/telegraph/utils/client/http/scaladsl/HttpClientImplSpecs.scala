package uk.co.telegraph.utils.client.http.scaladsl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.testkit.TestKit
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.typesafe.config.{Config, ConfigFactory}
import org.json4s.DefaultFormats
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}
import uk.co.telegraph.utils.client.http.impl.HttpClient.HttpConnector
import uk.co.telegraph.utils.client.http.scaladsl.HttpClientImplSpecs._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Try}

class HttpClientImplSpecs extends TestKit(ActorSystemTest)
  with FreeSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll
{
  import ActorSystemTest.dispatcher

  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds), interval = Span(500, Millis))

  val wm = new WireMockServer(options().port(ServerPort))

  override protected def beforeAll(): Unit = {
    wm.start()

    wm.stubFor(
      get(urlPathEqualTo("/sample-service/ping")).willReturn(aResponse().withStatus(302))
    )
    wm.stubFor{
      get(urlPathEqualTo("/sample-service")).willReturn(aResponse().withStatus(200).withBody("""{"status":"OK"}""").withHeader("content-type","application/json"))
    }
    wm.stubFor{
      get(urlPathEqualTo("/sample-service/slow")).willReturn(aResponse().withFixedDelay(5000).withStatus(200))
    }
  }

  override protected def afterAll(): Unit = {
    wm.stop()
    ActorMaterializerTest.shutdown()
    ActorSystemTest.terminate()
  }

  implicit val formats = DefaultFormats
  val client = new HttpClientImpl("app.details-endpoint")

  "Given the 'HttpClient', " - {
    "When calling execRequest" - {
      wm.stubFor(
        get(urlPathEqualTo("/details-endpoint/product/1"))
          .withHeader("x-auth", new EqualToPattern("some-key"))
          .withHeader("x-zumo", new EqualToPattern("2.0"))
          .willReturn(aResponse().withStatus(200))
      )
      wm.stubFor(
        get(urlPathEqualTo("/details-endpoint/product/2"))
          .withHeader("x-auth", new EqualToPattern("other-some-key"))
          .withHeader("x-zumo", new EqualToPattern("2.0"))
          .willReturn(aResponse().withStatus(200))
      )

      "I should be able to execute a request and keep the default headers + the headers set in the request" in {
        val request = RequestBuilding.Get(s"${client.settings.baseUrl}/product/1")
          .withHeaders(RawHeader("x-zumo", "2.0"))

        whenReady(client.execRequest(request)){ res =>
          res shouldBe a [HttpResponse]
          res.status shouldBe StatusCodes.OK
          res.discardEntityBytes()
        }
      }

      "I should be able to execute a request and override the default headers" in {
        val request = RequestBuilding.Get(s"${client.settings.baseUrl}/product/2")
          .withHeaders(RawHeader("x-zumo", "2.0"), RawHeader("x-auth", "other-some-key"))

        whenReady(client.execRequest(request)){ res =>
          res shouldBe a [HttpResponse]
          res.status shouldBe StatusCodes.OK
          res.discardEntityBytes()
        }
      }

      "I should be able to execute a request and get both request and response" in {
        val request = RequestBuilding.Get(s"${client.settings.baseUrl}/product/2")
          .withHeaders(RawHeader("x-zumo", "2.0"), RawHeader("x-auth", "other-some-key"))

        whenReady(client.execRequestAndGetContext(request)){ res =>
          res shouldBe a [HttpContext]
          res.request  shouldBe a [HttpRequest]
          res.response shouldBe a [HttpResponse]
        }
      }
    }

    "When queries an available client for details," - {

      "I should get a ClientFailure Object due to a 404" in {
        wm.stubFor(
          head(urlPathEqualTo("/details-endpoint/ping")).willReturn(aResponse().withStatus(404))
        )

        whenReady(client.getDetails){ res =>
          res.name    shouldBe "Enpoint used to test getDetails"
          res.status  shouldBe 404
          res.configs shouldBe Map(
            ("health.path",                              "/ping"),
            ("headers.x-auth",                           "some-key"),
            ("baseUrl",                                  "http://127.0.0.1:21123/details-endpoint"),
            ("host-connection-pool.client.idle-timeout", "500 millis")
          )
          res.command shouldBe "curl -H \"x-auth: some-key\" -X HEAD http://127.0.0.1:21123/details-endpoint/ping -v"
          res.cause.map(_.`type`) shouldBe Some("HttpInvalidStatusException")
        }
      }

      "I should get a ClientFailure Object due to a timeout" in {
        wm.stubFor(
          head(urlPathEqualTo("/details-endpoint/ping")).willReturn(aResponse().withFixedDelay(500))
        )
        whenReady(client.getDetails(100 millis)){ res =>
          res.name    shouldBe "Enpoint used to test getDetails"
          res.status  shouldBe 408
          res.configs shouldBe Map(
            ("health.path",                              "/ping"),
            ("headers.x-auth",                           "some-key"),
            ("baseUrl",                                  "http://127.0.0.1:21123/details-endpoint"),
            ("host-connection-pool.client.idle-timeout", "500 millis")
          )
          res.command shouldBe "curl -H \"x-auth: some-key\" -X HEAD http://127.0.0.1:21123/details-endpoint/ping -v"
          res.cause.map(_.`type`) shouldBe Some("TimeoutException")
        }
      }

      "I should get a ClientFailure Object if my stream throws an exception" in {
        wm.stubFor(
          head(urlPathEqualTo("/details-endpoint/ping")).willReturn(aResponse().withFixedDelay(1000))
        )
        whenReady( client.getDetails ){ res =>
          res.name    shouldBe "Enpoint used to test getDetails"
          res.status  shouldBe 500
          res.configs shouldBe Map(
            ("health.path",                              "/ping"),
            ("headers.x-auth",                           "some-key"),
            ("baseUrl",                                  "http://127.0.0.1:21123/details-endpoint"),
            ("host-connection-pool.client.idle-timeout", "500 millis")
          )
          res.command shouldBe "curl -H \"x-auth: some-key\" -X HEAD http://127.0.0.1:21123/details-endpoint/ping -v"
          res.cause.map(_.`type`) shouldBe Some("HttpFailureException")
        }
      }

      "I should get a ClientSuccess Object" in {
        wm.stubFor(
          head(urlPathEqualTo("/details-endpoint/ping")).willReturn(aResponse().withStatus(200))
        )
        whenReady(client.getDetails) { res =>
          res.name shouldBe "Enpoint used to test getDetails"
          res.status shouldBe 200
          res.configs shouldBe Map(
            ("health.path",                              "/ping"),
            ("headers.x-auth",                           "some-key"),
            ("baseUrl",                                  "http://127.0.0.1:21123/details-endpoint"),
            ("host-connection-pool.client.idle-timeout", "500 millis")
          )
          res.command shouldBe "curl -H \"x-auth: some-key\" -X HEAD http://127.0.0.1:21123/details-endpoint/ping -v"
          res.cause shouldBe None
        }
      }
    }
  }
}

object HttpClientImplSpecs {

  val ServerPort = 21123
  val SampleEndpointBaseUrl = "http://127.0.0.1:21123/sample-service"

  val SampleEndpointConfig: Config = ConfigFactory.load("application.tst.conf")

  implicit val ActorSystemTest      : ActorSystem = ActorSystem("TestSystem", SampleEndpointConfig)
  implicit val ActorMaterializerTest: ActorMaterializer = ActorMaterializer()
}

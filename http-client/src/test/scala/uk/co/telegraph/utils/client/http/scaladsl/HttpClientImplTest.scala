package uk.co.telegraph.utils.client.http.scaladsl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.testkit.TestKit
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.typesafe.config.{Config, ConfigFactory}
import org.json4s.DefaultFormats
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}
import uk.co.telegraph.helpers.{PayloadInt, PayloadStr}
import uk.co.telegraph.utils.client.http.exceptions.{HttpInvalidStatusException, HttpUnmarshallingException}
import uk.co.telegraph.utils.client.http.scaladsl.HttpClientImplTest._
import uk.co.telegraph.utils.client.models.ClientFailureDetails

import scala.language.postfixOps

class HttpClientImplTest extends TestKit(ActorSystemTest)
  with FreeSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll
{
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(10, Seconds), interval = Span(500, Millis))

  val wm = new WireMockServer(options().port(ServerPort))

  override protected def beforeAll(): Unit = {
    wm.start()
    wm.stubFor(
      head(urlPathEqualTo("/sample-service/ping")).willReturn(aResponse().withStatus(302))
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
  import uk.co.telegraph.utils.client.http.serialization.Json4sSupport._

  "Given the 'HttpClient', " - {
    "When executing an httpRequest, I should be able to filter by statusCode " in {

      val client = new HttpClientImpl("app.http")
      val request:HttpRequest = RequestBuilding.Get(s"$SampleEndpointBaseUrl")
      val flow: Flow[HttpRequest, PayloadStr, NotUsed] = Flow[HttpRequest]
        .via(client.httpClientFlow)
        .filterByStatus(OK)
        .unmarshalTo[PayloadStr]

      val resultFuture = Source.single(request).via(flow).runWith(Sink.head)
      whenReady(resultFuture){ res =>
        res shouldBe PayloadStr("OK")
      }
    }

    "When executing an httpRequest, If the expected statusCode does not match, I should get an error" in {

      val client = new HttpClientImpl("app.http")
      val request:HttpRequest = RequestBuilding.Get(s"$SampleEndpointBaseUrl")
      val flow: Flow[HttpRequest, String, NotUsed] = Flow[HttpRequest]
        .via(client.httpClientFlow)
        .filterByStatus(Found)
        .unmarshalTo[String]

      val resultFuture = Source.single(request).via(flow).runWith(Sink.head).failed
      whenReady(resultFuture){ res =>
        res shouldBe a [HttpInvalidStatusException]
      }
    }


    "When executing an httpRequest, I should be able to unmarshal the data into an object" in {
      val client = new HttpClientImpl("app.http")
      val request:HttpRequest = RequestBuilding.Get(s"$SampleEndpointBaseUrl")
      val flow: Flow[HttpRequest, PayloadStr, NotUsed] = Flow[HttpRequest]
        .via(client.httpClientFlow)
        .unmarshalTo[PayloadStr]

      val resultFuture = Source.single(request).via(flow).runWith(Sink.head)
      whenReady(resultFuture){ res =>
        res shouldBe PayloadStr("OK")
      }
    }

    "When executing an httpRequest, I should receive an error if the unmarshal fails" in {

      val client = new HttpClientImpl("app.http")
      val request:HttpRequest = RequestBuilding.Get(s"$SampleEndpointBaseUrl")
      val flow: Flow[HttpRequest, PayloadInt, NotUsed] = Flow[HttpRequest]
        .via(client.httpClientFlow)
        .unmarshalTo[PayloadInt]

      val resultFuture = Source.single(request).via(flow).runWith(Sink.head).failed
      whenReady(resultFuture){ res =>
        res shouldBe a [HttpUnmarshallingException]
      }
    }

    "When queries an available client for details, I should get a ClientSuccess Object" in {
      val client = new HttpClientImpl("app.http")
      whenReady(client.getDetails){ res =>
        res.name    shouldBe "HttpClientImpl"
        res.status  shouldBe 200
        res.configs shouldBe Map(
          ("health.method",  "HEAD"),
          ("health.path",    "/ping"),
          ("headers.x-auth", "some-key"),
          ("baseUrl",        SampleEndpointBaseUrl),
          ("secure",         "false")
        )
        res.command shouldBe s"""curl -H "x-auth: some-key" -X HEAD $SampleEndpointBaseUrl/ping -v"""
        res.cause   shouldBe None
      }
    }

    "When queries a non available client for details, I should get a ClientFailure Object" in {
      val client = new HttpClientImpl("app.invalid")
      whenReady(client.getDetails){ res =>
        res.name    shouldBe "HttpClientImpl"
        res.status  shouldBe 404
        res.configs shouldBe Map(
          ("health.method",      ""),
          ("health.path",        "/ping"),
          ("headers.x-auth",     "some-key"),
          ("baseUrl",            "http://127.0.0.1:21124/sample-service"),
          ("secure",             "false")
        )
        res.command shouldBe "curl -H \"x-auth: some-key\" -X HEAD http://127.0.0.1:21124/sample-service/ping -v"
        res.cause   shouldBe Some(ClientFailureDetails(
          `type`  = "HttpFailureException",
          message = "Request [HEAD http://127.0.0.1:21124/sample-service/ping] failed and no response was returned. Cause: 'Tcp command [Connect(127.0.0.1:21124,None,List(),Some(10 seconds),true)] failed because of Connection refused'"
        ))
      }
    }

    "I should be able to execute HttpRequest commands" in {
      val client = new HttpClientImpl("app.https")

      whenReady(client.single(RequestBuilding.Head("https://www.google.com"))){ res =>
        res.status shouldBe Found
      }
    }

    "It should timeout " in {
      val client = new HttpClientImpl("app.timeout")
      val request:HttpRequest = RequestBuilding.Get(s"$SampleEndpointBaseUrl/slow")
      val flow: Flow[HttpRequest, HttpContext, NotUsed] = Flow[HttpRequest]
        .via(client.httpClientFlow)

      val resultFuture = Source.single(request).via(flow).runWith(Sink.head)
      whenReady(resultFuture.failed){ ex =>
        ex.getMessage shouldBe "Request [GET http://127.0.0.1:21123/sample-service/slow] failed and no response was returned. Cause: 'TCP idle-timeout encountered on connection to [127.0.0.1:21123], no bytes passed in the last 1 second'"
      }
    }
  }
}

object HttpClientImplTest {

  val ServerPort = 21123
  val SampleEndpointBaseUrl = "http://127.0.0.1:21123/sample-service"

  val SampleEndpointConfig: Config = ConfigFactory.load("application.tst.conf")

  implicit val ActorSystemTest      : ActorSystem = ActorSystem("TestSystem", SampleEndpointConfig)
  implicit val ActorMaterializerTest: ActorMaterializer = ActorMaterializer()
}

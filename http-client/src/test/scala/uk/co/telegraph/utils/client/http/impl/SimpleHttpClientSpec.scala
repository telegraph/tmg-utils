package uk.co.telegraph.utils.client.http.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers, OneInstancePerTest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.Future

class SimpleHttpClientSpec extends FreeSpec with Matchers with MockFactory with BeforeAndAfterAll with OneInstancePerTest with ScalaFutures {
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))

  val httpClient: HttpClient = stub[HttpClient]
  val simpleHttpClient: SimpleHttpClient = new SimpleHttpClient(httpClient)

  implicit lazy val TestActorSystem: ActorSystem = ActorSystem(name = "test", config = ConfigFactory.empty())
  implicit lazy val TestMaterializer: ActorMaterializer = ActorMaterializer()

  override protected def afterAll(): Unit = {
    TestMaterializer.shutdown()
    TestActorSystem.terminate()
  }

  val someRequest = Get("http://someUrl")

  "The simple http client" - {
    "returns the response status code and body from the request" in {
      httpClient.single _ when someRequest returns Future.successful(HttpResponse(
        status = StatusCodes.OK,
        entity = HttpEntity("Hello, world")
      ))

      whenReady(simpleHttpClient.send(someRequest)) { response =>
        response.statusCode shouldBe 200
        response.body shouldBe "Hello, world"
      }
    }

    "returns a failure if the status code returned is not 200" in {
      httpClient.single _ when someRequest returns Future.successful(HttpResponse(
        status = StatusCodes.NotFound,
        entity = HttpEntity("Hello, world")
      ))

      whenReady(simpleHttpClient.send(someRequest).failed) { ex =>
        ex shouldBe a [UnsuccessfulResponseException]
        val e = ex.asInstanceOf[UnsuccessfulResponseException]
        e.statusCode shouldBe 404
        e.responseBody shouldBe "Hello, world"
      }
    }

    "does not return a failure for a 201" in {
      httpClient.single _ when someRequest returns Future.successful(HttpResponse(
        status = StatusCodes.Created,
        entity = HttpEntity("Hello, world")
      ))

      whenReady(simpleHttpClient.send(someRequest)) { response =>
        response.statusCode shouldBe 201
        response.body shouldBe "Hello, world"
      }
    }

    "returns an exception if the underlying http client throws an exception before sending the request" in {
      httpClient.single _ when someRequest throws new RuntimeException("ouch")

      assertThrows[RejectedRequestException] {
        simpleHttpClient.send(someRequest)
      }
    }

    "returns an exception if an exception is thrown after the request is sent" in {
      httpClient.single _ when someRequest returns Future.failed(new RuntimeException("ouch"))

      whenReady(simpleHttpClient.send(someRequest).failed) { ex =>
        ex shouldBe a [SimpleHttpClientException]
      }
    }
  }

}

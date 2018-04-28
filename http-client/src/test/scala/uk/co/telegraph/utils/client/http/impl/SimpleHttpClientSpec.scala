package uk.co.telegraph.utils.client.http.impl

import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.Future.{failed, successful}

class SimpleHttpClientSpec extends FreeSpec with Matchers with MockFactory with ScalaFutures {
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))

  val httpClient: HttpClient = stub[HttpClient]
  val simpleHttpClient: SimpleHttpClient = new SimpleHttpClient(httpClient)

  val someRequest = Get("http://someUrl")

  "The simple http client" - {
    "returns the response status code and body from the request" in {
      httpClient.single _ when someRequest returns successful(HttpResponse(
        status = StatusCodes.OK,
        entity = HttpEntity("Hello, world")
      ))

      whenReady(simpleHttpClient.send(someRequest)) { response =>
        response.statusCode shouldBe 200
        response.body shouldBe "Hello, world"
      }
    }

    "returns a failure if the status code returned is not 200" in {
      httpClient.single _ when someRequest returns successful(HttpResponse(
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
      httpClient.single _ when someRequest returns successful(HttpResponse(
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
      httpClient.single _ when someRequest returns failed(new RuntimeException("ouch"))

      whenReady(simpleHttpClient.send(someRequest).failed) { ex =>
        ex shouldBe a [SimpleHttpClientException]
      }
    }
  }

}

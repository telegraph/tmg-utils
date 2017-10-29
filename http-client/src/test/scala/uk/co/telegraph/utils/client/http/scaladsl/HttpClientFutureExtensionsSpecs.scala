package uk.co.telegraph.utils.client.http.scaladsl

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.json4s.{DefaultFormats, Formats}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers, OneInstancePerTest}
import uk.co.telegraph.utils.client.http.exceptions.{HttpInvalidStatusException, HttpUnmarshallingException}

import scala.concurrent.Future.successful
import scala.concurrent.duration._
import scala.language.postfixOps


class HttpClientFutureExtensionsSpecs
  extends FreeSpec
  with ScalaFutures
  with Matchers
  with OneInstancePerTest
  with BeforeAndAfterAll
  with MockFactory
{
  import HttpClientFutureExtensionsSpecs._
  import ActorSystemTest.dispatcher

  import uk.co.telegraph.utils.client.http.serialization.Json4sSupport._
  implicit val formats:Formats = DefaultFormats
  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds), interval = Span(500, Millis))

  override def afterAll():Unit = {
    ActorMaterializerTest.shutdown()
    ActorSystemTest.terminate()
  }

  "Given an HttpContext" - {
    "I should be able to ignore the payload" - {
      "and data should be drained successfully" in {
        whenReady(successful(SampleContext).filterByStatus()) { res =>
          //TODO: Check a way to validate that data was drained
          res shouldBe SampleContext
        }
      }
    }

    "I should be able to filter by status code" - {
      "get the response if matches" in {
        whenReady( successful( SampleContext ).filterByStatus() ){ res =>
          res shouldBe SampleContext
        }
      }

      "get an exception and have my Entity drained" - {
        whenReady( successful( SampleContext ).filterByStatus(StatusCodes.Created).failed ){ ex =>
          ex shouldBe a [HttpInvalidStatusException]
        }
      }
    }

    "I should be able to get the raw data" - {
      "successfully" in {
        whenReady( successful( SampleContext).rawData(100 millis) ){ res =>
          res shouldBe SamplePayload
        }
      }
    }

    "I should be able to unmarshal" -{
      "successfully" in {
        whenReady( successful( SampleContext).unmarshalTo[TestResponse] ){ res =>
          res shouldBe TestResponse("OK")
        }
      }

      "with error but drain the data and thrown a known exception" in {
        whenReady( successful( SampleContextInvalid ).unmarshalTo[TestResponse].failed ){ res =>
          res shouldBe a [HttpUnmarshallingException]
        }
      }
    }
  }
}

object HttpClientFutureExtensionsSpecs{

  implicit val ActorSystemTest      : ActorSystem       = ActorSystem()
  implicit val ActorMaterializerTest: ActorMaterializer = ActorMaterializer()

  case class TestResponse( status:String )

  val SampleRequest  = RequestBuilding.Get("/")
  val SamplePayload  = """{"status":"OK"}"""
  val SampleResponse = HttpResponse(
    status = StatusCodes.OK,
    entity = HttpEntity(ContentTypes.`application/json`, SamplePayload)
  )

  val SampleContext        = HttpContext(SampleRequest, SampleResponse)
  val SampleContextInvalid = HttpContext(SampleRequest, HttpResponse(
    status = StatusCodes.OK,
    entity = HttpEntity(
      ContentTypes.`application/json`,
      Source.failed(new RuntimeException("Invalid Stream"))
    )
  ))
}

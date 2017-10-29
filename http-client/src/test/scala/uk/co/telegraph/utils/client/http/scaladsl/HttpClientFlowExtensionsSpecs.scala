package uk.co.telegraph.utils.client.http.scaladsl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers, OneInstancePerTest}
import uk.co.telegraph.utils.client.http.exceptions.{HttpInvalidStatusException, HttpUnmarshallingException}

import scala.concurrent.duration._
import scala.language.postfixOps

class HttpClientFlowExtensionsSpecs
  extends FreeSpec
  with Matchers
  with ScalaFutures
  with OneInstancePerTest
  with BeforeAndAfterAll
{
  import HttpClientFlowExtensionsSpecs._

  implicit val defaultDuration: FiniteDuration = 1 minutes
  import TestActorSystem.dispatcher

  import uk.co.telegraph.utils.client.http.serialization.Json4sSupport._
  implicit val formats:Formats = DefaultFormats
  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds), interval = Span(500, Millis))

  override protected def afterAll(): Unit = {
    TestMaterializer.shutdown()
    TestActorSystem.terminate()
  }

  "Given an HttpContext" - {
    "I should be able to ignore the payload" - {
      "and data should be drained successfully" in {
        val flowUnderTest:Flow[HttpContext, HttpContext, NotUsed] = Flow[HttpContext]
          .ignorePayload

        val result = Source.single(SampleContext)
          .via(flowUnderTest)
          .runWith(Sink.head)

        whenReady( result ){ res =>
          res shouldBe SampleContext
        }
      }
    }

    "I should be able to filter by status code" - {
      "get the response if matches" in {
        val flowUnderTest:Flow[HttpContext, HttpContext, NotUsed] = Flow[HttpContext]
          .filterByStatus(StatusCodes.OK)

        val result = Source.single(SampleContext)
          .via(flowUnderTest)
          .runWith(Sink.head)

        whenReady( result ){ res =>
          res shouldBe SampleContext
        }
      }

      "get an exception and have my Entity drained" in {
        val flowUnderTest:Flow[HttpContext, HttpContext, NotUsed] = Flow[HttpContext]
          .filterByStatus(StatusCodes.Created)

        val result = Source.single(SampleContext)
          .via(flowUnderTest)
          .runWith(Sink.head)

        whenReady( result.failed ){ res =>
          res shouldBe a [HttpInvalidStatusException]
        }
      }
    }

    "I should be able to get the raw data" - {
      val flowUnderTest:Flow[HttpContext, String, NotUsed] = Flow[HttpContext]
        .rawData

      "successfully" in {
        val result = Source.single(SampleContext)
          .via(flowUnderTest)
          .runWith(Sink.head)

        whenReady( result ){ res =>
          res shouldBe SamplePayload
        }
      }
    }

    "I should be able to unmarshal" - {
      val flowUnderTest:Flow[HttpContext, TestResponse, NotUsed] = Flow[HttpContext]
        .unmarshalTo[TestResponse]

      "successfully" in {
        val result = Source.single(SampleContext)
          .via(flowUnderTest)
          .runWith(Sink.head)

        whenReady( result ){ res =>
          res shouldBe TestResponse("OK")
        }
      }

      "with error but drain the data and thrown a known exception" in {
        val result = Source.single(SampleContextInvalid)
          .via(flowUnderTest)
          .runWith(Sink.head)

        whenReady( result.failed ){ res =>
          res shouldBe a [HttpUnmarshallingException]
        }
      }
    }
  }
}

object HttpClientFlowExtensionsSpecs {

  implicit val TestActorSystem = ActorSystem()
  implicit val TestMaterializer = ActorMaterializer()

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

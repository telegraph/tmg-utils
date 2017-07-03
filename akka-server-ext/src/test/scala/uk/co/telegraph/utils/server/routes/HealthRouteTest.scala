package uk.co.telegraph.utils.server.routes

import java.time.{ZoneId, ZonedDateTime}

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.ArgumentMatchers.{eq => mkEq}
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest._
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.ClientDetails
import uk.co.telegraph.utils.server.routes.HealthRouteTest._

import scala.collection.convert.WrapAsJava._
import scala.concurrent.Future.successful
import scala.concurrent.duration._
import scala.language.postfixOps

class HealthRouteTest extends FunSpec
  with ScalatestRouteTest
  with BeforeAndAfterAll
  with BeforeAndAfter
  with Matchers
{
  val SampleRoute = new HealthRoute {
    override val clients = Seq(MockedClient)
    override val servicePath = "test-route"
    override implicit val system = ActorSystemTest
    override implicit val materializer = ActorMaterializerTest

    val logger:LoggingAdapter = MockedLogger
  }

  override protected def afterAll(): Unit = {
    ActorMaterializerTest.shutdown()
    ActorSystemTest.terminate()
  }

  before{
    when(MockedClient.getDetails(mkEq(DefaultTimeout))).thenReturn(successful(SampleClientSuccess))
  }

  describe("Given the /health route, "){

    it("I should be able to get the service status monitor"){
      Get("/health") ~> SampleRoute.healthRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] shouldBe """{"status":"OK"}"""
      }
    }

    it("I should be able to get a fresh version of my monitor"){
      Get("/test-route/health?cached=false") ~> SampleRoute.healthRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] shouldBe SampleDetailsNonCachedJson
      }
    }

    it("I should be able to get a cached version of my monitor"){
      Get("/test-route/health") ~> SampleRoute.healthRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] shouldBe SampleDetailsCachedJson
      }

      Get("/test-route/health?cached=true") ~> SampleRoute.healthRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] shouldBe SampleDetailsCachedJson
      }
    }

    it("I should be able to get an unhealthy DTO when the client returns a 404"){
      reset(MockedClient)
      when(MockedClient.getDetails(mkEq(DefaultTimeout))).thenReturn(successful(SampleClientFailure))

      Get("/test-route/health?cached=false") ~> SampleRoute.healthRoute ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[String] shouldBe SampleDetailsUnhealthyJson
      }
    }
  }
}

object HealthRouteTest {

  val DefaultTimeout = 5 seconds
  val MockedLogger = mock(classOf[LoggingAdapter])
  val MockedClient = mock(classOf[GenericClient])
  val SampleConfig:Config   = ConfigFactory.parseMap(Map(
    "app.name" -> "test-srv",
    "app.version" -> "1.0.0-test"
  ))
  implicit val ActorSystemTest = ActorSystem("test-system", SampleConfig)
  implicit val ActorMaterializerTest = ActorMaterializer()

  val SampleDateTime = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
  val SampleClientSuccess = ClientDetails(
    status   = 200,
    config   = ConfigFactory.parseMap(Map(
      "name" -> "client-1",
      "ping" -> "test"
    )),
    dateTime = SampleDateTime
  )
  val SampleClientFailure = ClientDetails(
    status   = 404,
    config   = ConfigFactory.parseMap(Map(
      "name" -> "client-1",
      "ping" -> "test"
    )),
    dateTime = SampleDateTime,
    cause = new RuntimeException("Sample Error")
  )

  val SampleDetailsNonCachedJson = s"""{"name":"test-srv","version":"1.0.0-test","status":"healthy","cached":false,"clients":[{"name":"${SampleClientSuccess.name}","status":${SampleClientSuccess.status},"date-time":${SampleClientSuccess.`date-time`},"date-time-str":"${SampleClientSuccess.`date-time-str`}","configs":{},"command":"${SampleClientSuccess.command}"}]}"""
  val SampleDetailsCachedJson    = s"""{"name":"test-srv","version":"1.0.0-test","status":"healthy","cached":true,"clients":[{"name":"${SampleClientSuccess.name}","status":${SampleClientSuccess.status},"date-time":${SampleClientSuccess.`date-time`},"date-time-str":"${SampleClientSuccess.`date-time-str`}","configs":{},"command":"${SampleClientSuccess.command}"}]}"""
  val SampleDetailsUnhealthyJson = s"""{"name":"test-srv","version":"1.0.0-test","status":"unhealthy","cached":false,"clients":[{"name":"${SampleClientFailure.name}","status":${SampleClientFailure.status},"date-time":${SampleClientFailure.`date-time`},"date-time-str":"${SampleClientFailure.`date-time-str`}","configs":{},"command":"${SampleClientFailure.command}","cause":{"type":"RuntimeException","message":"Sample Error"}}]}"""
}

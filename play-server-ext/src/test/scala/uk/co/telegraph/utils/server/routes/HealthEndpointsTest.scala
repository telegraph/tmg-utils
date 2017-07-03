package uk.co.telegraph.utils.server.routes

import java.time.{ZoneId, ZonedDateTime}

import akka.actor.ActorSystem
import com.google.inject.multibindings.Multibinder
import com.google.inject.{AbstractModule, Key}
import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.ArgumentMatchers.{anyBoolean, eq => mEq}
import org.mockito.Mockito
import org.mockito.Mockito.{reset, when}
import org.scalatest._
import play.api.http.MimeTypes
import play.api.mvc.EssentialFilter
import play.api.test.Helpers._
import play.api.test._
import play.filters.cors.CORSFilter
import uk.co.telegraph.utils.client.models.{ClientDetails, MonitorDto}
import uk.co.telegraph.utils.client.monitor.Monitor
import uk.co.telegraph.utils.server.filters.EventIdFilter
import uk.co.telegraph.utils.server.routes.HealthEndpointsTest._

import scala.concurrent.Future.successful

class HealthEndpointsTest
  extends FreeSpec
  with Matchers
  with OneInstancePerTest
  with BeforeAndAfterAll
  with BeforeAndAfter
{

  val HealthEndpointsController = new HealthEndpoints(MonitorMock, stubControllerComponents())(ActorSystemTest)

  before {
    reset(MonitorMock)

    when(MonitorMock.queryHealth(anyBoolean()))
      .thenReturn(successful(SampleMonitorDto))
  }

  override protected def afterAll(): Unit = {
    ActorSystemTest.terminate()
  }

  "Given the HealthEndpoint, I should be able to" - {

    "invoke the internalHealth" in {
      val response = HealthEndpointsController.internalHealth.apply(FakeRequest(GET, "/health"))

      val content = contentAsString(response)
      val result = await(response)

      result.body.contentType shouldBe Some(MimeTypes.JSON)
      result.header.status shouldBe OK
      content shouldBe """{"status":"OK"}"""
    }

    "invoke the externalHealth" in {
      val response = HealthEndpointsController.externalHealth().apply(FakeRequest(GET, "/sample/health"))

      val content = contentAsString(response)
      val result = await(response)

      result.body.contentType shouldBe Some(MimeTypes.JSON)
      result.header.status shouldBe OK
      content shouldBe """{"name":"play-server-ext","version":"1.0.0-SNAPSHOT","status":"Healthy","cached":false,"clients":[{"name":"test-service","status":200,"date-time":1483228800,"date-time-str":"2017-01-01T00:00:00.000+00:00","configs":{"endpoint":"www.test.com"},"command":"curl -m HEADER http://test-service.com"}]}"""
    }
  }
}

object HealthEndpointsTest {
  val ConfigTest: Config = ConfigFactory.load("application.conf")
  implicit val ActorSystemTest = ActorSystem("sample-system", ConfigTest)
  val MonitorMock: Monitor = Mockito.mock(classOf[Monitor])

  val SampleDate: ZonedDateTime = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))

  val ExpectedClientSuccess = ClientDetails(
    name = "test-service",
    status = 200,
    `date-time` = SampleDate.toEpochSecond,
    `date-time-str` = "2017-01-01T00:00:00.000+00:00",
    configs = Map(
      "endpoint" -> "www.test.com"
    ),
    command = "curl -m HEADER http://test-service.com"
  )

  val SampleMonitorDto = MonitorDto(
    cached  = false,
    clients = Seq(ExpectedClientSuccess)
  )

  class FakeServerModule extends AbstractModule {
    override def configure(): Unit = {
      val filterBinder = Multibinder.newSetBinder(binder(), Key.get(classOf[EssentialFilter]))
      filterBinder.addBinding().to(classOf[CORSFilter])
      filterBinder.addBinding().to(classOf[EventIdFilter])

      bind(classOf[Monitor]).toInstance(MonitorMock)
    }
  }
}

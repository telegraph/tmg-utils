package uk.co.telegraph.utils.server.flows

import java.time.{ZoneId, ZonedDateTime}

import akka.actor.{ActorRef, ActorSystem}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.testkit.TestActor.{AutoPilot, KeepRunning}
import akka.testkit.{TestActor, TestProbe}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.parseMap
import org.mockito.Mockito
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.{ClientDetails, MonitorDto}
import uk.co.telegraph.utils.client.monitor.Monitor
import uk.co.telegraph.utils.client.monitor.MonitorActor.GetData
import uk.co.telegraph.utils.server.exceptions.MonitorException
import uk.co.telegraph.utils.server.model.{Cause, FailurePayload, HealthDto, HealthEnum}
import uk.co.telegraph.utils.server.flows.HealthFlowTest._
import org.mockito.ArgumentMatchers.{eq => mkEq}

import scala.collection.convert.WrapAsJava._
import scala.collection.immutable.{Queue => MQueue}
import scala.concurrent.Future
import scala.concurrent.Future.successful

class HealthFlowTest extends FunSpec
  with ScalaFutures
  with BeforeAndAfterAll
  with BeforeAndAfter
  with Matchers
{

  val flow = new HealthFlow {
    override implicit val materializer = ActorMaterializerTest
    override implicit val system = ActorSystemTest
    override lazy val monitor = MockedMonitor
    override val logger  = MockedLogger
    override val clients = null
  }

  before{
    reset(MockedMonitor)
  }

  override protected def afterAll(): Unit = {
    ActorMaterializerTest.shutdown()
    ActorSystemTest.terminate()
  }

  describe("Given the HealthFlow, "){
    it("I should be able to Monitor my clients and get a proper response"){

      when(MockedMonitor.queryHealth(mkEq(false)))
        .thenReturn(successful(MonitorDto(cached = true, List(SampleClientSuccess))))

      whenReady( flow.health(true) ) { res =>
        res.statusCode shouldBe StatusCodes.OK
        res.data shouldBe a [HealthDto]
        res.data shouldBe SampleHealth
      }
    }

    it("I should be able to handle Messages"){

      when(MockedMonitor.queryHealth(mkEq(false)))
        .thenReturn(Future.failed(new RuntimeException("sample")))

      whenReady( flow.health(true) ) { res =>
        res.statusCode shouldBe StatusCodes.InternalServerError
        res.data shouldBe a [FailurePayload]
        res.data shouldBe FailurePayload(
          statusCode = MonitorException.MonitoringStatusCode,
          cause      = Cause(
            message   = "Fail to get Clients Health Status - sample",
            exception = Some("MonitorException"),
            errorCode = MonitorException.MonitoringErrorCode
          )
        )
      }
    }
  }
}

object HealthFlowTest {

  val MockedMonitor = mock(classOf[Monitor])
  val MockedLogger  = mock(classOf[LoggingAdapter])

  val SampleConfig:Config   = parseMap(Map(
    "app.name"    -> "test-srv",
    "app.version" -> "1.0.0-test"
  ))
  implicit val ActorSystemTest = ActorSystem("test-system", SampleConfig)
  implicit val ActorMaterializerTest = ActorMaterializer()

  val SampleDateTime = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
  val SampleClientSuccess = ClientDetails(
    status   = 200,
    config   = parseMap(Map(
      "name" -> "client-1",
      "ping" -> "test"
    )),
    dateTime = SampleDateTime
  )
  val SampleHealth = HealthDto(
    name    = "test-srv",
    version = "1.0.0-test",
    status  = HealthEnum.Healthy,
    cached  = true,
    clients = Seq(SampleClientSuccess)
  )
}

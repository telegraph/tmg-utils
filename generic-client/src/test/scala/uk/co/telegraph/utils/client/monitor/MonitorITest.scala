package uk.co.telegraph.utils.client.monitor

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import MonitorITest._
import org.mockito.ArgumentMatchers.{eq => mkEq}
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.time.{Millis, Seconds, Span}
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.ClientDetails

import scala.concurrent.Future.{failed, successful}
import scala.language.postfixOps
import scala.concurrent.duration._

class MonitorITest
  extends FreeSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with OneInstancePerTest
  with ScalaFutures
{
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(10, Seconds), interval = Span(500, Millis))

  implicit val ActorSystemTest = ActorSystem("monitor-test", ConfigTest)

  before {
    reset(MockClient)
  }

  after {
    ActorSystemTest.terminate()
  }

  "Given a monitor instance, " - {

    "I should get a cached version when queried" in {
      when(MockClient.getDetails(mkEq(DefaultTimeout))).thenReturn(successful(SampleConnectedMessage1))

      val monitor = Monitor(Seq(MockClient))

      Thread.sleep(3000)
      whenReady(monitor.queryHealth() ){ res =>
        res.cached  shouldBe true
        res.clients shouldBe Seq(SampleConnectedMessage1)
      }
    }

    "I should get a cached version when if the " in {
      when(MockClient.getDetails(mkEq(DefaultTimeout)))
        .thenReturn(successful(SampleConnectedMessage1))
        .thenReturn(failed(new RuntimeException("test")))

      val monitor = Monitor(Seq(MockClient))

      Thread.sleep(2000)
      whenReady( monitor.queryHealth(true) ){ res =>
        res.cached  shouldBe false
        res.clients shouldBe Seq(SampleConnectedMessage1)
      }
    }

  }
}

object MonitorITest {

  val DefaultTimeout: FiniteDuration = 5 seconds
  val ConfigTest    : Config         = ConfigFactory.load("application-tst.conf")
  val MockClient    : GenericClient  = mock(classOf[GenericClient])

  val SampleDateTime1        : ZonedDateTime = ZonedDateTime.now()
  val SampleConnectedMessage1: ClientDetails = ClientDetails(
    name            = "sample-1",
    status          = 200,
    `date-time`     = SampleDateTime1.toEpochSecond,
    `date-time-str` = SampleDateTime1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    configs         = Map.empty,
    command         = "test"
  )
}

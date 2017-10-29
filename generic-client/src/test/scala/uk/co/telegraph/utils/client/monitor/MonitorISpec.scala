package uk.co.telegraph.utils.client.monitor

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.ClientDetails
import uk.co.telegraph.utils.client.monitor.MonitorISpec._

import scala.concurrent.Future.{failed, successful}
import scala.concurrent.duration._
import scala.language.postfixOps

class MonitorISpec
  extends FreeSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with OneInstancePerTest
  with ScalaFutures
  with MockFactory
{
  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds), interval = Span(500, Millis))

  implicit val ActorSystemTest = ActorSystem("monitor-test", ConfigTest)
  val MockClient    : GenericClient  = mock[GenericClient]

  after {
    ActorSystemTest.terminate()
  }

  "Given a monitor instance, " - {

    "I should get a cached version when queried" in {
      (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returns(successful(SampleConnectedMessage1)).once()

      val monitor = Monitor(Seq(MockClient))

      Thread.sleep(3000)
      whenReady(monitor.queryHealth() ){ res =>
        res.cached  shouldBe true
        res.clients shouldBe Seq(SampleConnectedMessage1)
      }
    }

    "I should get a cached version when if the " in {
      inSequence{
        (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returns(successful(SampleConnectedMessage1)).once()
        (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returns(failed(new RuntimeException("test"))).once()
      }

      val monitor = Monitor(Seq(MockClient))

      Thread.sleep(2000)
      whenReady( monitor.queryHealth(true) ){ res =>
        res.cached  shouldBe false
        res.clients shouldBe Seq(SampleConnectedMessage1)
      }
    }

  }
}

object MonitorISpec {

  val DefaultTimeout: FiniteDuration = 5 seconds
  val ConfigTest    : Config         = ConfigFactory.load("application-tst.conf")

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

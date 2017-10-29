package uk.co.telegraph.utils.client.monitor

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{ActorSystem, OneForOneStrategy, Props}
import akka.event.LoggingAdapter
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.{ClientDetails, MonitorDto}
import uk.co.telegraph.utils.client.monitor.MonitorActor.{GetData, Refresh}
import uk.co.telegraph.utils.client.monitor.MonitorActorSpec._
import uk.co.telegraph.utils.client.monitor.settings.MonitorSettings

import scala.concurrent.Future.{failed, successful}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

class MonitorActorSpec extends TestKit(ActorSystemTest)
  with ImplicitSender
  with FreeSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with OneInstancePerTest
  with MockFactory
{

  val MockClient       : GenericClient  = mock[GenericClient]
  val MockLogging      : LoggingAdapter = stub[LoggingAdapter]
  val MonitorActorProps: Props          = Props(new MonitorActorMock(MonitorSettings(), Seq(MockClient), MockLogging))

  override def afterAll(): Unit ={
    TestKit.shutdownActorSystem(system)
  }

  "Given the Monitor System, " - {

    "When no data is yet available, " - {

      "If the 'Refresh' message is received, the system should collect fresh data" in {
        (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout)
          .returning(successful(SampleConnectedMessage1))
          .once()

        val actorRef = TestActorRef(MonitorActorProps)

        actorRef ! Refresh
        actorRef.stop()
      }

      "If the 'GetData' message is received, the system should return and store fresh data" in {
        (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout)
          .returning(successful(SampleConnectedMessage1))
          .once()

        val actorRef = TestActorRef(MonitorActorProps)

        actorRef ! GetData()
        expectMsg(MonitorDto(cached = false, Seq(SampleConnectedMessage1)) )

        actorRef.stop()
      }
    }

    "When the cache is pre-loaded with data, " - {

      "If the 'GetData' message is received, the system should return the cached data" in {
        (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout)
          .returning(successful(SampleConnectedMessage1))
          .once()

        val actorRef = TestActorRef(MonitorActorProps)

        actorRef ! Refresh

        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef.stop()
      }

      "If the 'GetData' message is received with 'fresh=True', the system should return the new data" in {
        inSequence{
          (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returning(successful(SampleConnectedMessage1)).once()
          (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returning(successful(SampleConnectedMessage2)).once()
        }

        val actorRef = TestActorRef(MonitorActorProps)
        actorRef ! Refresh

        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef ! GetData(true)
        expectMsg(MonitorDto(cached = false, Seq(SampleConnectedMessage2)) )

        actorRef.stop()
      }

      "If the 'Refresh' message is received, new data must be cached." in {
        inSequence{
          (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returning(successful(SampleConnectedMessage1)).once()
          (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returning(successful(SampleConnectedMessage2)).once()
        }

        val actorRef = TestActorRef(MonitorActorProps)

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage2)) )

        actorRef.stop()
      }

      "If the 'Refresh' operation fails, I should keep the cached values" in {
        inSequence{
          (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returning(successful(SampleConnectedMessage1)).once()
          (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returning(successful(SampleConnectedMessage2)).once()
          (MockClient.getDetails(_:FiniteDuration)).expects(DefaultTimeout).returning(failed(new RuntimeException("Sample Exception"))).once()
        }

        val actorRef = TestActorRef(MonitorActorProps)

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage2)) )

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage2)) )

        actorRef.stop()
      }

      "If the actor dies, it should restart " in {
        val actorRef = TestActorRef[MonitorActorMock](MonitorActorProps)
        actorRef.underlyingActor.supervisorStrategy shouldBe a [OneForOneStrategy]
      }

      "should log non valid messages" in {
        (MockLogging.error(_:String)).verify(s"Message will not be processed: 'Test'").returns(())

        val actorRef = TestActorRef[MonitorActorMock](MonitorActorProps)
        actorRef ! "Test"
      }
    }
  }
}

object MonitorActorSpec{
  val DefaultTimeout:FiniteDuration = 5 second
  val Config: Config = ConfigFactory.parseString(
    """app.monitoring {
      | delay         : 0 seconds
      | interval      : 2 seconds
      | client-timeout: 5 seconds
      |}
    """.stripMargin)

  implicit val ActorSystemTest = ActorSystem("monitoring-test", Config)

  val SampleDateTime1: ZonedDateTime = ZonedDateTime.now()
  val SampleConnectedMessage1 = ClientDetails(
    name            = "sample-1",
    status          = 200,
    `date-time`     = SampleDateTime1.toEpochSecond,
    `date-time-str` = SampleDateTime1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    configs         = Map.empty,
    command         = "test"
  )
  val SampleDateTime2: ZonedDateTime = ZonedDateTime.now()
  val SampleConnectedMessage2 = ClientDetails(
    name            = "sample-2",
    status          = 200,
    `date-time`     = SampleDateTime2.toEpochSecond,
    `date-time-str` = SampleDateTime2.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    configs         = Map.empty,
    command         = "test"
  )

  class MonitorActorMock
  (
    val settings:MonitorSettings,
    val clients:Seq[GenericClient],
    override val log: LoggingAdapter
  ) extends MonitorActor
}

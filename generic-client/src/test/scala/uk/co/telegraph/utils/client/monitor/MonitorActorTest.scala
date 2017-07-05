package uk.co.telegraph.utils.client.monitor

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import scala.concurrent.Future.{failed, successful}
import akka.actor.{ActorSystem, OneForOneStrategy, Props}
import akka.event.LoggingAdapter
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.ArgumentMatchers.{eq => mkEq}
import org.mockito.Mockito._
import org.scalatest.{mock => _, _}
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.{ClientDetails, MonitorDto}
import uk.co.telegraph.utils.client.monitor.MonitorActor.{GetData, Refresh}
import uk.co.telegraph.utils.client.monitor.MonitorActorTest._
import uk.co.telegraph.utils.client.monitor.settings.MonitorSettings

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

class MonitorActorTest extends TestKit(ActorSystemTest)
  with ImplicitSender
  with FunSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with OneInstancePerTest
{

  before{
    reset(MockClient)
    when(MockClient.getDetails(mkEq( DefaultTimeout )))
      .thenReturn(successful(SampleConnectedMessage1))
      .thenReturn(successful(SampleConnectedMessage2))
      .thenReturn(failed(new RuntimeException("Sample Exception")))
  }

  override def afterAll(): Unit ={
    TestKit.shutdownActorSystem(system)
  }

  describe("Given the Monitor System, "){

    describe("When no data is yet available, "){

      it("If the 'Refresh' message is received, the system should collect fresh data"){
        val actorRef = TestActorRef(Props(classOf[MonitorActorMock]) )

        actorRef ! Refresh
        verify( MockClient, times(1) ).getDetails( mkEq( DefaultTimeout ))
        actorRef.stop()
      }

      it("If the 'GetData' message is received, the system should return and store fresh data"){
        val actorRef = TestActorRef(Props(classOf[MonitorActorMock]) )

        actorRef ! GetData()
        expectMsg(MonitorDto(cached = false, Seq(SampleConnectedMessage1)) )
        verify( MockClient, times(1) ).getDetails( mkEq( DefaultTimeout ))
        actorRef.stop()
      }
    }

    describe("When the cache is pre-loaded with data, "){

      it("If the 'GetData' message is received, the system should return the cached data"){
        val actorRef = TestActorRef(Props(classOf[MonitorActorMock]) )

        actorRef ! Refresh

        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        verify( MockClient, times(1) ).getDetails( mkEq( DefaultTimeout ))
        actorRef.stop()
      }

      it("If the 'GetData' message is received with 'fresh=True', the system should return the new data"){
        val actorRef = TestActorRef(Props(classOf[MonitorActorMock]) )

        actorRef ! Refresh

        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef ! GetData(true)
        expectMsg(MonitorDto(cached = false, Seq(SampleConnectedMessage2)) )

        verify( MockClient, times(2) ).getDetails( mkEq( DefaultTimeout ))
        actorRef.stop()
      }

      it("If the 'Refresh' message is received, new data must be cached."){
        val actorRef = TestActorRef(Props(classOf[MonitorActorMock]) )

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage2)) )

        verify( MockClient, times(2) ).getDetails( mkEq( DefaultTimeout ))
        actorRef.stop()
      }

      it("If the 'Refresh' operation fails, I should keep the cached values"){
        val actorRef = TestActorRef(Props(classOf[MonitorActorMock]) )

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage1)) )

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage2)) )

        actorRef ! Refresh
        actorRef ! GetData()
        expectMsg(MonitorDto(cached = true, Seq(SampleConnectedMessage2)) )

        verify( MockClient, times(3) ).getDetails( mkEq( DefaultTimeout ))
        actorRef.stop()
      }

      it("If the actor dies, it should restart "){
        val actorRef = TestActorRef[MonitorActorMock](Props(classOf[MonitorActorMock]) )
        actorRef.underlyingActor.supervisorStrategy shouldBe a [OneForOneStrategy]
      }

      it("should log non valid messages"){
        val actorRef = TestActorRef[MonitorActorMock](Props(classOf[MonitorActorMock]) )
        actorRef ! "Test"
        verify(MockLogging, times(1)).error(s"Message will not be processed: 'Test'")
      }
    }
  }
}

object MonitorActorTest{
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
  val MockClient: GenericClient = mock(classOf[GenericClient])
  val MockLogging: LoggingAdapter = mock(classOf[LoggingAdapter])

  class MonitorActorMock extends MonitorActor{
    override lazy val settings: MonitorSettings = MonitorSettings()
    override def log: LoggingAdapter = MockLogging
    override val clients = Seq(MockClient)
  }
}

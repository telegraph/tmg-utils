package uk.co.telegraph.utils.client.monitor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest._
import uk.co.telegraph.utils.client.monitor.ScheduledActorTest._
import uk.co.telegraph.utils.client.monitor.settings.MonitorSettings

import scala.concurrent.duration._
import scala.language.postfixOps

class ScheduledActorTest extends TestKit(ActorSystemTest)
  with ImplicitSender
  with FreeSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
{

  "Given the Scheduler Actor" - {

    "I should get multiple messages" in {
      val probe = TestProbe()
      val actorRef = TestActorRef(Props(classOf[SchedulerTest], probe.ref))


      val Seq(t1, t2, t3) = probe.receiveN(3, 20 seconds).asInstanceOf[Seq[Long]]
      actorRef.stop()

      printf(s"Events triggered at [${t2-t1}, ${t3-t2}]")
      (t2 - t1).toInt shouldBe (2000 +- 500)
      (t3 - t2).toInt shouldBe (2000 +- 500)
    }
  }
}

object ScheduledActorTest{

  val Config: Config = ConfigFactory.parseString(
    """app.monitoring {
      | delay         : 0 seconds
      | interval      : 2 seconds
      | client-timeout: 5 seconds
      |}
    """.stripMargin)
  implicit val ActorSystemTest = ActorSystem("monitoring-test", Config)

  class SchedulerTest(prob:ActorRef) extends Actor with ScheduledActor{
    override val settings: MonitorSettings = MonitorSettings()
    override protected def onTick(): AnyRef = Tick

    override def receive: Receive = {
      case Tick =>
        log.info("Tick Received!")
        prob ! System.currentTimeMillis()
      case other =>
        log.warning(s"Non processed message $other")
    }
  }

  object Tick
}

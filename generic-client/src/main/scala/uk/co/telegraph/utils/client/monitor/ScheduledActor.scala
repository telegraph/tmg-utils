package uk.co.telegraph.utils.client.monitor

import akka.actor.{Actor, ActorLogging}
import com.typesafe.config.Config

import scala.language.postfixOps
import uk.co.telegraph.utils.client._

import scala.concurrent.duration.FiniteDuration

trait ScheduledActor extends ActorLogging { this: Actor =>

  import context.dispatcher

  /**
    * Endpoint configuration
    */
  val endpointConfig:Config

  lazy val delay:FiniteDuration     = endpointConfig.getDuration("delay")
  lazy val interval:FiniteDuration  = endpointConfig.getDuration("interval")
  lazy val scheduler = context.system.scheduler.schedule(delay, interval, self, onTick())

  /**
    * Start Scheduler
    */
  override def preStart(): Unit = {
    scheduler
  }

  /**
    * Cancel Scheduler
    */
  override def postStop(): Unit = {
    scheduler.cancel()
  }

  protected def onTick():AnyRef
}

package uk.co.telegraph.utils.client.monitor

import akka.actor.{Actor, ActorLogging, Cancellable}
import uk.co.telegraph.utils.client.monitor.settings.MonitorSettings

import scala.language.postfixOps

trait ScheduledActor extends ActorLogging { this: Actor =>

  import context.dispatcher

  val settings:MonitorSettings

  lazy val scheduler: Cancellable = context.system.scheduler.schedule(settings.delay, settings.interval, self, onTick())

  // Create the scheduler
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

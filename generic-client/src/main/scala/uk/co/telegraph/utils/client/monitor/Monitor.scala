package uk.co.telegraph.utils.client.monitor

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.MonitorDto
import uk.co.telegraph.utils.client.monitor.MonitorActor.GetData

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait Monitor {
  def queryHealth(freshData:Boolean = false):Future[MonitorDto]
}

object Monitor {

  private case class MonitorImpl(clients:Seq[GenericClient])(implicit system: ActorSystem)
    extends Monitor
  {
    implicit val timeout:Timeout = 20 seconds
    private val monitoringActor = system.actorOf(MonitorActor.props(clients))

    def queryHealth(freshData:Boolean = false):Future[MonitorDto] = {
      (monitoringActor ? GetData(freshData)).mapTo[MonitorDto]
    }
  }

  def apply(clients:Seq[GenericClient])(implicit system: ActorSystem):Monitor =
    MonitorImpl(clients)
}

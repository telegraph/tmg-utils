package uk.co.telegraph.utils.client.monitor

import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.pipe
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.{ClientDetails, MonitorDto}
import MonitorActor._

import scala.concurrent.Future
import scala.concurrent.Future._
import scala.concurrent.duration.{Duration, FiniteDuration}
import java.time.{Duration => JDuration}

import scala.concurrent.duration.Duration.fromNanos
import scala.language.implicitConversions

private [monitor] trait MonitorActor
  extends Actor
  with ActorLogging
{
  import context.dispatcher

  implicit val timeout:FiniteDuration

  val clients:Seq[GenericClient]

  lazy val receive        = monitor()

  private def monitor(implicit cachedDetails:Seq[ClientDetails] = Seq.empty): Receive ={
    case Refresh =>
      log.debug("Refresh Cache")
      queryClients onSuccess {
        case details => context become monitor(details)
      }
    case GetData(refreshData) =>
      log.debug(s"Get HealthChecks: Fresh = $refreshData")
      // If no force refresh and cache exists
      if( !refreshData && cachedDetails.nonEmpty ){
        sender() ! MonitorDto(clients = cachedDetails, cached = true)
      }else{
        queryClients.map({ details =>
          // Update Cache ASAP
          context become monitor(details)
          // Fire Reply
          MonitorDto(clients = details, cached = false)
        }).pipeTo(sender())
      }
    case other =>
      log.error(s"Message will not be processed: '$other'")
  }

  /**
    * Query Clients for Details
    */
  def queryClients( implicit defaultDetails:Seq[ClientDetails]):Future[Seq[ClientDetails]] =
    sequence(clients.map( _.getDetails ))
      .recover({
        case ex:Throwable =>
          log.error(ex, "Failed to QueryClients")
          defaultDetails
      })

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(){
      case _ => SupervisorStrategy.restart
    }
}

object MonitorActor{

  val ConfigPath = "app.monitoring"

  sealed trait MonitorMsg
  private [monitor] object Refresh extends MonitorMsg
  private [monitor] case class GetData(freshData:Boolean = false  ) extends MonitorMsg

  implicit private def toFinitDuration(jd:JDuration):FiniteDuration =
    fromNanos(jd.toNanos)

  private case class MonitorActorImpl(clients:Seq[GenericClient]) extends MonitorActor with ScheduledActor{

    val endpointConfig = context.system.settings.config.getConfig(ConfigPath)

    implicit val timeout:FiniteDuration = endpointConfig.getDuration("client-timeout")

    override protected def onTick(): AnyRef = {
      Refresh
    }
  }

  def props(clients:Seq[GenericClient]):Props = Props(classOf[MonitorActorImpl], clients)
}

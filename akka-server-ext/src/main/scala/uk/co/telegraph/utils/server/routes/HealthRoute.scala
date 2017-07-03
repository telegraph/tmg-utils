package uk.co.telegraph.utils.server.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.HttpEntity
import org.json4s.{DefaultFormats, Formats}
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.monitor.MonitorActor
import uk.co.telegraph.utils.server.directives.DirectivesExt._
import uk.co.telegraph.utils.server.flows.HealthFlow
import uk.co.telegraph.utils.server.serializers._

trait HealthRoute extends HealthFlow{

  implicit val system:ActorSystem

  val servicePath:String

  val clients:Seq[GenericClient]

  val healthRoute = doBasicHealth() ~ doFullHealth()
  lazy val monitoring  = system.actorOf(MonitorActor.props(clients))

  private def doBasicHealth() =
    get {
      path("health"){
        complete(HttpEntity(`application/json`, """{"status":"OK"}"""))
      }
    }

  private def doFullHealth() =
    get {
      path(servicePath / "health"){
        import HealthRoute.formats

        parameter( 'cached.as[Boolean].? ){ isCached =>
          completeWithFlow( isCached.getOrElse(true), healthFlow)
        }
      }
    }
}

object HealthRoute{
  implicit val formats:Formats = DefaultFormats +
    ErrorCodeSerializer            +
    StatusCodeSerializer           +
    HealthDtoSerializer
}
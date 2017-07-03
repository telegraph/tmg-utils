package uk.co.telegraph.utils.server.routes

import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.Singleton
import io.swagger.annotations.{ApiOperation, ApiParam, ApiResponse, ApiResponses}
import org.json4s.jackson.Serialization._
import play.api.http.{ContentTypes, MimeTypes}
import play.api.mvc._
import uk.co.telegraph.utils.client.monitor.Monitor
import uk.co.telegraph.utils.server.models.HealthDto

@Singleton
class HealthEndpoints @Inject()(monitor:Monitor, cc:ControllerComponents)(implicit system:ActorSystem)
  extends AbstractController(cc)
{
  import system.dispatcher

  private lazy val endpointConfig = system.settings.config.getConfig("app")
  private lazy val appVersion = endpointConfig.getString("version")
  private lazy val appName    = endpointConfig.getString("name")

  def internalHealth: Action[AnyContent] = Action{
    Ok("""{"status":"OK"}""" ).as(MimeTypes.JSON)
  }

  @ApiOperation(value = "Indicate the health value of the service and the services that it connects to")
  @ApiResponses(Array(
    new ApiResponse (code = 200, message = "a Json object containing the healthcheck of the service"),
    new ApiResponse (code = 500, message = "a Json object containing the healthcheck of the service")))
  def externalHealth
  (
    @ApiParam(value = "Determine if we will receive cached data for the clients or it will do a new query to them", defaultValue  = "true", allowableValues = "true, false")  cached:Boolean = true
  ): Action[AnyContent] = Action.async{ _ =>
    import HealthDto.Serializer
    monitor.queryHealth(!cached)
      .map( res => Ok( write(HealthDto(
          name = appName,
          version = appVersion,
          cached = res.cached,
          clients = res.clients)
        ))
        .as(ContentTypes.JSON)
      )
  }
}



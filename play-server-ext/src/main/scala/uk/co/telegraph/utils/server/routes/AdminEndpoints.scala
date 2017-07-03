package uk.co.telegraph.utils.server.routes

import javax.inject.{Inject, Singleton}

import com.typesafe.config.ConfigRenderOptions.concise
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import play.api.Configuration
import play.api.mvc._

@Api(value = "/")
@Singleton
class AdminEndpoints @Inject()(config:Configuration, cc:ControllerComponents) extends AbstractController(cc) {

  @Api(value = "/admin")
  @ApiOperation( value = "Obtain the configuration of the service")
  @ApiResponses(Array(new ApiResponse (code = 200, message = "a Json object containing the configuration of the service")))
  def getConfig = Action{ implicit res =>
      Ok( config.underlying.root().render(concise()) ).as(JSON)
  }
}

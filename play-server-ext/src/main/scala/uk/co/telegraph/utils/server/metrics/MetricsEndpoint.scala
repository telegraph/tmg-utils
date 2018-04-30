package uk.co.telegraph.utils.server.metrics

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.json.{MetricsModule => DropwizardMetricsModule}
import com.fasterxml.jackson.databind.ObjectMapper
import play.api.http.ContentTypes
import play.api.mvc._

class MetricsEndpoint @Inject()(controllerComponents: ControllerComponents, metricRegistry: MetricRegistry)
  extends AbstractController(controllerComponents) {

  val mapper: ObjectMapper = new ObjectMapper()
    .registerModule(
      new DropwizardMetricsModule(
        TimeUnit.SECONDS,
        TimeUnit.MILLISECONDS,
        false
      )
    )

  def getMetrics: Action[AnyContent] = Action {
    Results.Ok(mapper.writeValueAsString(metricRegistry)).as(ContentTypes.JSON)
  }
}

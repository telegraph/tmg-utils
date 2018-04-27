package uk.co.telegraph.travel.metrics

import com.codahale.metrics.{MetricRegistry, Timer}
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import play.api.mvc._
import play.api.test.FakeRequest
import uk.co.telegraph.utils.server.TestActorSystemAndMaterializer
import uk.co.telegraph.utils.server.metrics.{EndpointTimer, TimerMetricsFilter}

import scala.concurrent.Future

class TimerMetricsFilterSpec extends FreeSpec with Matchers with TestActorSystemAndMaterializer with OneInstancePerTest {

  val registry: MetricRegistry = new MetricRegistry()
  val timer: Timer = registry.timer("testTimer")

  "The timers metrics filter" - {
    "Times requests when method and path match" in {
      val endpointTimer: EndpointTimer = EndpointTimer("GET", "/stuff" == _, timer)
      val filter = new TimerMetricsFilter(Seq(endpointTimer))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.successful(Results.Ok))(request)) { _ =>
        timer.getCount shouldBe 1
      }
    }

    "Ignores requests when method or path do not match" in {
      val postTimer: EndpointTimer = EndpointTimer("POST", "/stuff" == _, timer)
      val otherEndpointTimer: EndpointTimer = EndpointTimer("GET", "/otherStuff" == _, timer)
      val filter = new TimerMetricsFilter(Seq(postTimer, otherEndpointTimer))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.successful(Results.Ok))(request)) { _ =>
        timer.getCount shouldBe 0
      }
    }

    "Can match part of the path" in {
      val endpointTimer: EndpointTimer = EndpointTimer("GET", path => path.contains("/stuff"), timer)
      val filter = new TimerMetricsFilter(Seq(endpointTimer))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff/andMore/Things")

      whenReady(filter.apply(_ => Future.successful(Results.Ok))(request)) { _ =>
        timer.getCount shouldBe 1
      }
    }

    "Only starts the first matching timer" in {
      val otherTimer = registry.timer("otherTimer")
      val postTimer: EndpointTimer = EndpointTimer("GET", "/stuff" == _, timer)
      val otherEndpointTimer: EndpointTimer = EndpointTimer("GET", "/stuff" == _, otherTimer)
      val filter = new TimerMetricsFilter(Seq(postTimer, otherEndpointTimer))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.successful(Results.Ok))(request)) { _ =>
        timer.getCount shouldBe 1
        otherTimer.getCount shouldBe 0
      }
    }

    "Times execution of the downstream operations" in {
      val endpointTimer: EndpointTimer = EndpointTimer("GET", "/stuff" == _, timer)
      val filter = new TimerMetricsFilter(Seq(endpointTimer))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.successful {
        Thread.sleep(2000)
        Results.Ok
      })(request)) { _ =>
        timer.getCount shouldBe 1
        val maxTimedInSeconds: Double = timer.getSnapshot.getMax / 1000000000
        val minTimedInSeconds: Double = timer.getSnapshot.getMin / 1000000000
        withClue(s"Max: $maxTimedInSeconds, Min: $minTimedInSeconds should both be > 2 second") {
          maxTimedInSeconds >= 2 shouldBe true
          minTimedInSeconds >= 2 shouldBe true
        }
      }
    }

    "Stops the timer on exceptional results too" in {
      val endpointTimer: EndpointTimer = EndpointTimer("GET", "/stuff" == _, timer)
      val filter = new TimerMetricsFilter(Seq(endpointTimer))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.failed(new RuntimeException("ouch")))(request).failed) { _ =>
        timer.getCount shouldBe 1
      }
    }
  }
}

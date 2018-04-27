package uk.co.telegraph.travel.metrics

import com.codahale.metrics.{Counter, MetricRegistry}
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import play.api.mvc.Results
import play.api.test.FakeRequest
import uk.co.telegraph.utils.server.TestActorSystemAndMaterializer
import uk.co.telegraph.utils.server.metrics.{CounterMetricsFilter, EndpointCounter, ExceptionCounter}

import scala.concurrent.Future

class CounterMetricsFilterSpec extends FreeSpec with Matchers with TestActorSystemAndMaterializer with OneInstancePerTest {
  val registry: MetricRegistry = new MetricRegistry()
  val counter: Counter = registry.counter("testTimer")

  "The counters metrics filter" - {
    "counts requests when method, path and result match" in {
      val endpointCounter: EndpointCounter = EndpointCounter("GET", "/stuff" == _, _.header.status == 200, counter, "")
      val filter = new CounterMetricsFilter(Seq(endpointCounter))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.successful(Results.Ok))(request)) { _ =>
        counter.getCount shouldBe 1
      }
    }

    "Ignores requests when method or path do not match" in {
      val postTimer: EndpointCounter = EndpointCounter("POST", "/stuff" == _, _.header.status == 200, counter, "")
      val otherEndpointCounter: EndpointCounter = EndpointCounter("GET", "/otherStuff" == _, _.header.status == 200, counter, "")
      val failureCounter: EndpointCounter = EndpointCounter("GET", "/stuff" == _, _.header.status == 500, counter, "")
      val filter = new CounterMetricsFilter(Seq(postTimer, otherEndpointCounter, failureCounter))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.successful(Results.Ok))(request)) { _ =>
        counter.getCount shouldBe 0
      }
    }

    "Can match part of the path" in {
      val endpointCounter: EndpointCounter = EndpointCounter("GET", path => path.contains("/stuff"), _.header.status == 200, counter, "")
      val filter = new CounterMetricsFilter(Seq(endpointCounter))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff/andMore/Things")

      whenReady(filter.apply(_ => Future.successful(Results.Ok))(request)) { _ =>
        counter.getCount shouldBe 1
      }
    }

    "Increments all counters matching" in {
      val otherCounter = registry.counter("otherCounter")
      val postTimer: EndpointCounter = EndpointCounter("GET", "/stuff" == _, _.header.status == 200, counter, "")
      val otherEndpointCounter: EndpointCounter = EndpointCounter("GET", "/stuff" == _, _.header.status == 200, otherCounter, "")
      val filter = new CounterMetricsFilter(Seq(postTimer, otherEndpointCounter))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.successful(Results.Ok))(request)) { _ =>
        counter.getCount shouldBe 1
        otherCounter.getCount shouldBe 1
      }
    }

    "Stops the counter on exceptional results too" in {
      val otherCounter = registry.counter("otherCounter")
      val endpointCounter: EndpointCounter = EndpointCounter("GET", "/stuff" == _, _.header.status == 200, counter, "")
      val exceptionCounter: EndpointCounter = ExceptionCounter("GET", "/stuff" == _, otherCounter)
      val filter = new CounterMetricsFilter(Seq(endpointCounter), Seq(exceptionCounter))(TestMaterializer, TestActorSystem.dispatcher)

      val request = FakeRequest("GET", "/stuff")

      whenReady(filter.apply(_ => Future.failed(new RuntimeException("ouch")))(request).failed) { _ =>
        counter.getCount shouldBe 0
        otherCounter.getCount shouldBe 1
      }
    }
  }
}

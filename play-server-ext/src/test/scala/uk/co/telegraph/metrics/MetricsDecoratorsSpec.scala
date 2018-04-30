package uk.co.telegraph.travel.metrics

import akka.actor.ActorSystem
import com.codahale.metrics.MetricRegistry
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}
import play.api.mvc._
import play.api.test.{FakeRequest, Helpers}
import uk.co.telegraph.utils.TestActorSystemAndMaterializer
import uk.co.telegraph.utils.server.metrics.MetricsDecorators

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class MetricsDecoratorsSpec extends FreeSpec with Matchers with TestActorSystemAndMaterializer with MockFactory {

  import TestActorSystem.dispatcher

  val metrics = new MetricRegistry

  class TestThing(downstream: TestDownstream, controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) with MetricsDecorators {
    val metricRegistry: MetricRegistry = metrics
    val system: ActorSystem = TestActorSystem

    def getProductBy(value: String): Action[AnyContent] = Action.async {
      countedAndTimed("test") {
        downstream
          .callDownstream(value)
          .map(Results.Ok(_))
          .recover {
            case _: NullPointerException => Results.Accepted("")
            case _: IllegalArgumentException => Results.NotFound("")
            case _: RuntimeException => Results.InternalServerError("")
          }
      }
    }

    def callDownstreamClient(value: String): Future[String] =
      withDownstreamCounterAndTimer("test") {
        downstream
          .callDownstream(value)
      }
  }

  val stubDownstream: TestDownstream = stub[TestDownstream]
  val testService = new TestThing(stubDownstream, Helpers.stubControllerComponents())

  val fakeGetRequest = FakeRequest("GET", "/some/path")

  "The metrics counted and timed decorator" - {
    "simple" - {
      "increases the success counter if the result is successful" in {
        stubDownstream.callDownstream _ when "test" returns successful("test")

        whenReady(testService.getProductBy("test")(fakeGetRequest)) { _ =>
          metrics.counter("test.success").getCount shouldBe 1
        }
      }

      "increases the success counter if the result is 202" in {
        stubDownstream.callDownstream _ when "test" returns failed(new NullPointerException(""))

        whenReady(testService.getProductBy("test")(fakeGetRequest)) { _ =>
          metrics.counter("test.success").getCount shouldBe 1
        }
      }


      "increases the warning counter if the result is 404" in {
        stubDownstream.callDownstream _ when "test" returns failed(new IllegalArgumentException(""))

        whenReady(testService.getProductBy("test")(fakeGetRequest)) { _ =>
          metrics.counter("test.warning").getCount shouldBe 1
        }
      }

      "increases the errors counter if the result is 5xx" in {
        stubDownstream.callDownstream _ when "test" returns failed(new RuntimeException("some message"))

        whenReady(testService.getProductBy("test")(fakeGetRequest)) { _ =>
          metrics.counter("test.error").getCount shouldBe 1
        }
      }

      "times the execution" in {
        stubDownstream.callDownstream _ when "test" returns Future {
          Thread.sleep(1000)
          "test"
        }

        whenReady(testService.getProductBy("test")(fakeGetRequest)) { _ =>
          val timer = metrics.timer("test.timings")
          val maxTimedInSeconds: Double = timer.getSnapshot.getMax / 1000000000
          val minTimedInSeconds: Double = timer.getSnapshot.getMin / 1000000000
          timer.getCount shouldBe 1
          withClue(s"Max: ${timer.getSnapshot.getMax}, Min: ${timer.getSnapshot.getMin} should both be > 1 second") {
            maxTimedInSeconds >= 1 shouldBe true
            minTimedInSeconds >= 1 shouldBe true
          }
        }
      }
    }

    "for downstream clients" - {
      "increases the success counter if the result is successful" in {
        stubDownstream.callDownstream _ when "test" returns successful("test")

        whenReady(testService.callDownstreamClient("test")) { _ =>
          metrics.counter("downstreams.test.success").getCount shouldBe 1
        }
      }

      "increases the errors counter if the result is 5xx" in {
        stubDownstream.callDownstream _ when "test" returns failed(new RuntimeException("some message"))

        whenReady(testService.callDownstreamClient("test").failed) { _ =>
          metrics.counter("downstreams.test.error").getCount shouldBe 1
        }
      }


      "times downstreams executions with different prefix" in {
        stubDownstream.callDownstream _ when "test" returns Future {
          Thread.sleep(1000)
          "test"
        }

        whenReady(testService.callDownstreamClient("test")) { _ =>
          val timer = metrics.timer("downstreams.test.timings")
          val maxTimedInSeconds: Double = timer.getSnapshot.getMax / 1000000000
          val minTimedInSeconds: Double = timer.getSnapshot.getMin / 1000000000
          timer.getCount shouldBe 1
          withClue(s"Max: ${timer.getSnapshot.getMax}, Min: ${timer.getSnapshot.getMin} should both be > 1 second") {
            maxTimedInSeconds >= 1 shouldBe true
            minTimedInSeconds >= 1 shouldBe true
          }
        }
      }
    }
  }
}

class TestDownstream {
  def callDownstream(value: String): Future[String] = ???
}

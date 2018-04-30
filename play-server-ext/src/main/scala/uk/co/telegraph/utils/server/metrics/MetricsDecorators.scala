package uk.co.telegraph.utils.server.metrics

import akka.actor.ActorSystem
import com.codahale.metrics.{MetricRegistry, Timer}
import play.api.mvc.Result

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MetricsDecorators {

  val metricRegistry: MetricRegistry
  val system:ActorSystem

  def countedAndTimed(metricName: String)(action: => Future[Result]): Future[Result] = {
    counted(metricName) {
      timed(metricName) {
        action
      }
    }
  }

  def counted(counterName: String)(res: Future[Result]): Future[Result] = {
    res.map((result: Result) => {
      val statusCode = result.header.status
      statusCode match {
        case x if x >= 200 && x < 300 => metricRegistry.counter(s"$counterName.success").inc()
        case x if x >= 300 && x < 500 => metricRegistry.counter(s"$counterName.warning").inc()
        case x if x >= 500            => metricRegistry.counter(s"$counterName.error").inc()
      }
      result
    }).recoverWith {
      case e: Throwable =>
        metricRegistry.counter(s"$counterName.error").inc()
        Future.failed(e)
    }
  }

  def timed(timerName: String)(action: => Future[Result]): Future[Result] = {
    val timer = metricRegistry.timer(s"$timerName.timings")
    timeAsyncBlockExecution(timer, action)
  }

  def withDownstreamCounterAndTimer[ANY](metricName: String)(anyBlock: => Future[ANY]): Future[ANY]= {
    withDownstreamCounter(metricName) {
      withDownstreamTimer(metricName) {
        anyBlock
      }
    }
  }

  def withDownstreamTimer[ANY](timerName: String)(anyBlock: => Future[ANY]): Future[ANY]= {
    val timer = metricRegistry.timer(s"downstreams.$timerName.timings")
    timeAsyncBlockExecution(timer, anyBlock)
  }

  def withDownstreamCounter[ANY](counterName: String)(asyncBlock: => Future[ANY]): Future[ANY]= {
    asyncBlock.map(
      result => {
        metricRegistry.counter(s"downstreams.$counterName.success").inc()
        result
      }
    ).recoverWith { case any: Throwable =>
      metricRegistry.counter(s"downstreams.$counterName.error").inc()
      Future.failed(any)
    }
  }

  private def timeAsyncBlockExecution[ANY](timer: Timer, asyncBlock: => Future[ANY]) = {
    val runningTimer = timer.time()
    asyncBlock.map(
      result => {
        runningTimer.stop()
        result
      }
    )
  }
}

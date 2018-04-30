package uk.co.telegraph.utils.server.metrics

import javax.inject.Inject

import akka.stream.Materializer
import com.codahale.metrics.Timer
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


class TimerMetricsFilter @Inject()(endpointTimers: Seq[EndpointTimer])(implicit val mat: Materializer, val ec: ExecutionContext)
  extends Filter {

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val timer: RunningTimer = endpointTimers.find(endpointTimer => endpointTimer.matches(requestHeader)) match {
      case Some(t)  => new DropwizardTimer(t.startTimer)
      case None     => NoopTimer
    }

    nextFilter(requestHeader).transform(
      (result: Result) => {
        timer.stop()
        result
      },
      exception => {
        timer.stop()
        exception
      }
    )
  }
}

class EndpointTimer(endpointRequestMatcher: EndpointRequestMatcher, timer: Timer) {
  def matches(requestHeader: RequestHeader): Boolean = endpointRequestMatcher.matches(requestHeader)

  def startTimer: Timer.Context = timer.time()
}

object EndpointTimer {
  def apply(method: String, pathMatching: (String) => Boolean, timer: Timer): EndpointTimer =
    new EndpointTimer(
      new EndpointRequestMatcher(method, pathMatching),
      timer)
}

trait RunningTimer {
  def stop(): Unit
}

class DropwizardTimer(timerContext: Timer.Context) extends RunningTimer {
  def stop(): Unit = timerContext.stop()
}

object NoopTimer extends RunningTimer {
  def stop(): Unit = {}
}

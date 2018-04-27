package uk.co.telegraph.utils.server.metrics

import javax.inject.Inject

import akka.stream.Materializer
import com.codahale.metrics.Counter
import play.api.http.HttpEntity
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


class CounterMetricsFilter @Inject()(
  onResultCounters    : Seq[EndpointCounter],
  oneExceptionCounters: Seq[EndpointCounter] = Seq()
)(implicit val mat: Materializer, val ec: ExecutionContext) extends Filter {

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    nextFilter(requestHeader).transform(
      (result: Result) => {
        onResultCounters.foreach(_.parse(requestHeader, result))
        result
      },
      exception => {
        oneExceptionCounters.foreach(_.parse(requestHeader, Result(new ResponseHeader(status = 500), HttpEntity.NoEntity )))
        exception
      }
    )
  }
}

class EndpointCounter(
  val endpointMatcher: EndpointRequestAndResultMatcher,
  val counter: Counter,
  val resourceName: String = ""
) {
  def parse(requestHeader: RequestHeader, result: Result): Unit =
    if (endpointMatcher.matches(requestHeader, result)) counter.inc()

  override def toString = s"Counter for ${endpointMatcher.method} $resourceName"
}

object EndpointCounter {
  def apply(
    method        : String,
    pathMatching  : String => Boolean,
    resultMatching: Result => Boolean,
    counter       : Counter,
    resourceName: String
  ) = new EndpointCounter(new EndpointRequestAndResultMatcher(method, pathMatching, resultMatching), counter, resourceName)
}

object ExceptionCounter {
  def apply(
    method         : String,
    pathMatching   : (String) => Boolean,
    counter        : Counter
  ): EndpointCounter =
    new EndpointCounter(new EndpointRequestAndResultMatcher(method, pathMatching, _ => true), counter)
}

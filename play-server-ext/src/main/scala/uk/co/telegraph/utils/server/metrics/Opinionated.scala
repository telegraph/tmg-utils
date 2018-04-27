package uk.co.telegraph.utils.server.metrics

import com.codahale.metrics.MetricRegistry

object Opinionated {

  def successWarnFailFor(
    method          : String,
    pathMatching    : (String) => Boolean,
    resourceName    : String,
    metricRegistry  : MetricRegistry
  ): Seq[EndpointCounter] =
    Seq(
      EndpointCounter(
        method,
        pathMatching,
        result => result.header.status == 200 || result.header.status == 201,
        metricRegistry.counter(s"$resourceName.$method.success"),
        resourceName
      ),
      EndpointCounter(
        method,
        pathMatching,
        result => result.header.status == 404,
        metricRegistry.counter(s"$resourceName.$method.warning"),
        resourceName
      ),
      EndpointCounter(
        method,
        pathMatching,
        result => result.header.status >= 500,
        metricRegistry.counter(s"$resourceName.$method.error"),
        resourceName
      )
    )

  def successWarnFailCountersFor(
    methods         : Seq[String],
    pathMatching    : (String) => Boolean,
    resourceName    : String,
    metricRegistry  : MetricRegistry
  ): Seq[EndpointCounter] =
    methods.flatMap(method => successWarnFailFor(method, pathMatching, resourceName, metricRegistry))

  def timerFor(
    method          : String,
    pathMatching    : (String) => Boolean,
    resourceName    : String,
    metricRegistry  : MetricRegistry
  ): EndpointTimer = EndpointTimer(method, pathMatching, metricRegistry.timer(s"$resourceName.timings"))
}

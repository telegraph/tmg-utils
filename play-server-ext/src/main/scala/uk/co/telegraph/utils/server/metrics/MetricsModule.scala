package uk.co.telegraph.utils.server.metrics

import java.util.concurrent.TimeUnit

import com.codahale.metrics.{ConsoleReporter, MetricFilter, MetricRegistry}
import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.Config
import play.api.{Configuration, Environment, Logger}

import scala.concurrent.duration._
import scala.language.postfixOps


class MetricsModule(
  environment: Environment,
  config     : Configuration
) extends AbstractModule {

  val metricsRegistry = new MetricRegistry()

  def configure(): Unit = {
    if (config.getOptional[Boolean]("metrics.graphiteReporter.enabled").contains(true)) {
      startGraphiteReporter(config.get[Config]("metrics.graphiteReporter"))
      reportRunning()
    }

    if (config.getOptional[Boolean]("metrics.consoleReporter.enabled").contains(true)) {
      startConsoleReporter()
    }
  }

  @Provides
  def metricsRegistryProvider(): MetricRegistry = metricsRegistry

  private def startGraphiteReporter(config: Config): Unit = {
    val hostname        : String    = stringOr(config, "hostname", "localhost")
    val port            : Int       = if (config.hasPath("port")) config.getInt("port") else 2003
    val prefix          : String    = buildMetricsPrefix(config)
    val reportFrequency : Duration  = if (config.hasPath("reportFrequency")) Duration(config.getString("reportFrequency")) else 5 seconds

    val graphite = new Graphite(hostname, port)
    val graphiteReporter = GraphiteReporter
      .forRegistry(metricsRegistry)
      .prefixedWith(prefix)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .filter(MetricFilter.ALL)
      .build(graphite)
    graphiteReporter.start(reportFrequency.length, reportFrequency.unit)

    Logger.info(s"Started graphite reporter, pointing to '$hostname:$port', with prefix '$prefix'. Will report every $reportFrequency")
  }

  private def buildMetricsPrefix(config: Config) = {
    val env             = stringOr(config, "env",     "local")
    val graphiteApiKey  = stringOr(config, "apiKey",  "noKey")
    val prefix          = stringOr(config, "prefix",  "someApp.example")

    s"$graphiteApiKey.$env.$prefix"
  }

  private def stringOr(config: Config, path: String, default: String) =
    if (config.hasPath(path)) config.getString(path) else default

  private def startConsoleReporter(): Unit = {
    val reporter = ConsoleReporter
      .forRegistry(metricsRegistry)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build

    reporter.start(1, TimeUnit.SECONDS)
  }

  private def reportRunning(): Unit = metricsRegistry.counter("running").inc()
}

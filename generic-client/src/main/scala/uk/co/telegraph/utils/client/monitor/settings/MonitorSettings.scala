package uk.co.telegraph.utils.client.monitor.settings

import com.typesafe.config.Config
import uk.co.telegraph.utils._

import scala.concurrent.duration.FiniteDuration

abstract class MonitorSettings {
  def delay        : FiniteDuration
  def interval     : FiniteDuration
  def clientTimeout: FiniteDuration
}

object MonitorSettings extends Settings[MonitorSettings]("app.monitoring"){

  private case class MonitorSettingsImpl
  (
    delay        : FiniteDuration,
    interval     : FiniteDuration,
    clientTimeout: FiniteDuration
  ) extends MonitorSettings

  override protected def fromSubConfig(root: Config, inner: Config): MonitorSettings = {
    val conf:Config = inner.withFallback(root getConfig prefix)

    MonitorSettingsImpl(
      delay         = conf get[FiniteDuration] "delay",
      interval      = conf get[FiniteDuration] "interval",
      clientTimeout = conf get[FiniteDuration] "client-timeout"
    )
  }
}

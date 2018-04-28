package uk.co.telegraph.utils.server.logging

import akka.event.Logging._

trait WithLogLevel {
  val level: LogLevel
}

trait LogsErrors extends WithLogLevel {
  override val level: LogLevel = ErrorLevel
}

trait LogsWarns extends WithLogLevel {
  override val level: LogLevel = WarningLevel
}

trait LogsInfos extends WithLogLevel {
  override val level: LogLevel = InfoLevel
}

trait LogsDebugs extends WithLogLevel {
  override val level: LogLevel = DebugLevel
}

package uk.co.telegraph.utils

import java.time.{Duration => JDuration}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

package object client {
  implicit def asFiniteDuration(d: JDuration): FiniteDuration =
    Duration.fromNanos(d.toNanos)
}

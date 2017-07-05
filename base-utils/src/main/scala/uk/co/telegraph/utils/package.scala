package uk.co.telegraph

import com.typesafe.config.Config

import scala.language.implicitConversions

package object utils {

  /**
    * Default System Clock
    */
  implicit val DefaultSystemClock = Clock

  /**
    * Implicitly converts a Config into ConfigExtension object.
    */
  implicit def toConfigExtensions(left:Config):ConfigExtensions =
    new ConfigExtensions(left)
}

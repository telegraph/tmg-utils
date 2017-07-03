package uk.co.telegraph.utils.client.http.impl

import com.typesafe.config.{Config, ConfigValue}

import scala.language.{implicitConversions, postfixOps}

package object settings {

  implicit def toConfigExtensions(left:Config): ConfigExtensions =
    new ConfigExtensions(left)

  implicit def mapConfigToString (obj:ConfigValue):String = obj.unwrapped().asInstanceOf[String]

  implicit def mapConfigValueToBoolean(obj:ConfigValue):Boolean = obj.unwrapped().asInstanceOf[Boolean]
}

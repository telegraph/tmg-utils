package uk.co.telegraph

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

import com.typesafe.config.Config
import uk.co.telegraph.utils.config.ConfigExtensionsImpl

import scala.language.implicitConversions

package object utils{

  /**
    * Default System Clock
    */
  implicit val DefaultSystemClock = Clock

  val DefaultDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val ShortDateTimeFormatter  : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

  implicit def stringToDateTime(dateTimeStr:String):ZonedDateTime = {
    LocalDateTime.parse(dateTimeStr, DefaultDateTimeFormatter).atZone(ZoneId.of("UTC"))
  }

  implicit def dateTimeToString(dateTime:ZonedDateTime):String = {
    dateTime.format(DefaultDateTimeFormatter)
  }

  implicit def toConfigExtensions(left:Config):ConfigExtensionsImpl = {
    new ConfigExtensionsImpl(left)
  }
}

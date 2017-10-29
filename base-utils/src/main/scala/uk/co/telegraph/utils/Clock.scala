package uk.co.telegraph.utils

import java.time.{LocalDateTime, ZonedDateTime}

import scala.language.implicitConversions

trait Clock {

  def now:LocalDateTime

  def nowWithZone:ZonedDateTime

  def nowEpoch:Long = {
    nowWithZone.toEpochSecond
  }
}

object Clock extends Clock{
  override def now: LocalDateTime =
    LocalDateTime.now()

  override def nowWithZone: ZonedDateTime =
    ZonedDateTime.now()

}

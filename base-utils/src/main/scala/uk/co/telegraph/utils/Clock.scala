package uk.co.telegraph.utils

import java.time.{LocalDateTime, ZonedDateTime}

trait Clock {

  def now:LocalDateTime

  def nowWithZone:ZonedDateTime

}

object Clock extends Clock{
  override def now: LocalDateTime =
    LocalDateTime.now()

  override def nowWithZone: ZonedDateTime =
    ZonedDateTime.now()
}

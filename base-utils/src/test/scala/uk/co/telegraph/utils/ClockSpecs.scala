package uk.co.telegraph.utils

import java.time.{ZoneOffset, ZonedDateTime}

import org.scalatest._
import Matchers._
import org.scalatest.FreeSpec

class ClockSpecs extends FreeSpec
{

  abstract class ConfigX

  val clock:Clock = DefaultSystemClock

  "Given the 'Clock'" - {
    "Should be possible to get the current LocalDateTime" in {
      val res = clock.now.toEpochSecond(ZoneOffset.UTC)
      (ZonedDateTime.now().toEpochSecond - res) should (be >= -3600L and be <= 3600L)
    }

    "Should be possible to get the current ZonedDateTime" in {
      val res = clock.nowWithZone.toEpochSecond
      ZonedDateTime.now().toEpochSecond - res shouldBe 0
    }

    "Should be possible to get the current Epoch" in {
      val res = clock.nowEpoch
      ZonedDateTime.now().toEpochSecond - res shouldBe 0
    }
  }
}

package uk.co.telegraph.utils.implicits.types

import org.scalatest.{FreeSpec, Matchers}
import scala.util.{Failure, Success}

class TryUtilsTest
  extends FreeSpec
  with Matchers
  with TryUtils {

  "TryUtilsTest" - {

    "implicitly convert a [[Failure]] to a [[Left]]" in {
      val throwable = new Throwable
      val input = Failure(throwable)
      val actual = input.toEither()
      val expected = Left(throwable)

      actual shouldBe expected
    }

    "implicitly convert a [[Success]] to a [[Right]]" in {
      val t = "string"
      val input = Success(t)
      val actual = input.toEither()
      val expected = Right(t)

      actual shouldBe expected
    }
  }
}

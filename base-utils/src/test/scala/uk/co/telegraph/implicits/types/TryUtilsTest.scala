package uk.co.telegraph.implicits.types

import org.scalatest.{FlatSpec, Matchers}
import uk.co.telegraph.utils.implicits.types.TryUtils

import scala.util.{Failure, Success}

class TryUtilsTest
  extends FlatSpec
  with Matchers
  with TryUtils {

  it should "implicitly convert a [[Failure]] to a [[Left]]" in {
    val throwable = new Throwable
    val input = Failure(throwable)
    val actual = input.toEither()
    val expected = Left(throwable)

    actual shouldBe expected
  }

  it should "implicitly convert a [[Success]] to a [[Right]]" in {
    val t = "string"
    val input = Success(t)
    val actual = input.toEither()
    val expected = Right(t)

    actual shouldBe expected
  }
}

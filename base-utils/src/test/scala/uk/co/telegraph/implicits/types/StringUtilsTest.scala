package uk.co.telegraph.implicits.types

import java.util.UUID
import org.scalatest.{Assertion, FreeSpec, Matchers}
import uk.co.telegraph.utils.implicits.types.StringUtils

class StringUtilsTest
  extends FreeSpec
    with Matchers
    with StringUtils {

  "StringUtilsTest"  - {

    "implicitly convert a [[String]] to a [[UUID]] with the given success function" in {

      def stringAsUuidHasFailed(t: Throwable): Assertion = assert(false)

      val input = "88a01bf3-8368-4ac0-b487-8d465414a797"
      input.asUuid(uuid => uuid.toString shouldBe input)(stringAsUuidHasFailed)
    }

    "implicitly convert a [[String]] to a [[UUID]] with the given failure function" in {
      val input = "this-is-an-invalid-uuid-string"

      def stringAsUuidIsSuccessful(uuid: UUID): Assertion = assert(false)
      def stringAsUuidHasFailed(t: Throwable): Assertion = assert(true)

      input.asUuid(stringAsUuidIsSuccessful)(stringAsUuidHasFailed)
    }
  }
}

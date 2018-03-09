package uk.co.telegraph.implicits.types

import org.scalatest.{FlatSpec, Matchers}
import uk.co.telegraph.utils.implicits.types.StringUtils

class StringUtilsTest
  extends FlatSpec
    with Matchers
    with StringUtils {

  it should "implicitly convert a [[String]] to a [[UUID]] with the given success function" in {
    val input = "88a01bf3-8368-4ac0-b487-8d465414a797"
    input.asUuid(uuid => uuid.toString shouldBe input)(assert(false))
  }

  it should "implicitly convert a [[String]] to a [[UUID]] with the given failure function" in {
    val input = "this-is-an-invalid-uuid-string"
    input.asUuid(_ => assert(false))(assert(true))
  }
}

package uk.co.telegraph.utils.server.serializers

import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats, MappingException}
import org.scalatest.{FunSpec, Matchers}
import uk.co.telegraph.utils.server.exceptions.ErrorCode

class ErrorCodeSerializerTest extends FunSpec with Matchers {

  implicit val formats:Formats = DefaultFormats + ErrorCodeSerializer

  describe("Given the Error Code Serializer"){
    it("I should be able to deserialize an Error code"){
      read[Error]("""{"errorCode":"ERR20000"}""") shouldBe Error(ErrorCode("ERR20000"))
    }

    it("I should get an error if an invalid valid occurs"){
      intercept[MappingException]{
        read[Error]("""{"errorCode":20000}""")
      }
    }
  }
}

case class Error(errorCode:ErrorCode)

object ErrorCodeSerializerTest{



}

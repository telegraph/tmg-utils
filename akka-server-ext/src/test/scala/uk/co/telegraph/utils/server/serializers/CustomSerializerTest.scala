package uk.co.telegraph.utils.server.serializers

import org.json4s._
import org.json4s.jackson.Serialization._
import org.scalatest.{FunSpec, Matchers}
import uk.co.telegraph.utils.server._
import uk.co.telegraph.utils.server.model.FailurePayload

class CustomSerializerTest extends FunSpec with Matchers {

  implicit val formats:Formats = DefaultFormats + StatusCodeSerializer + ErrorCodeSerializer

  describe("Given the status Code Serializer"){
    it("I should be able to serialize an object with a status code"){

      val jsonStr = write(SampleFailure)

      jsonStr shouldBe SampleResponseStr
    }

    it("I should be able to deserialize an object with a status code"){
      val obj:FailurePayload = read[FailurePayload](SampleResponseStr)
      obj shouldBe SampleFailure
    }
  }
}


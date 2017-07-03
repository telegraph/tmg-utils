package uk.co.telegraph.utils.server.model

import org.json4s.DefaultFormats
import org.scalatest.{FunSpec, Matchers}
import uk.co.telegraph.utils.server._

class ResponseMsgTest extends FunSpec with Matchers {

  implicit val format = DefaultFormats

  describe("Given ResponseMsg object, "){
    it("Only data side should be marshalled"){
      SampleResponse.toString shouldBe SampleResponseStr
    }

    it("I should be able to generate a ResponseMsg given an Exception"){
      val response = ResponseMsg(SampleServerError)
      val responseData = response.data.asInstanceOf[FailurePayload]

      response.statusCode shouldBe SampleServerError.statusCode
      responseData.cause.message shouldBe SampleServerError.message
      responseData.cause.errorCode shouldBe SampleServerError.errorCode
      responseData.cause.exception shouldBe Some("TestException")
    }
  }
}

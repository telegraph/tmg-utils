package uk.co.telegraph.utils.server.model

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.{Conflict, InternalServerError}
import org.scalatest.{FunSpec, Matchers}
import uk.co.telegraph.utils.server._
import uk.co.telegraph.utils.server.exceptions.ErrorCode
import uk.co.telegraph.utils.server.exceptions.ErrorCodes._
import FailurePayloadTest._

class FailurePayloadTest extends FunSpec with Matchers{

  describe("Given the 'FailurePayload' object, "){

    it("unknown exceptions should have 'UnknownError' code"){
      val result = FailurePayload( new IllegalArgumentException("test") )

      result.statusCode shouldBe InternalServerError
      result.cause.message   shouldBe "test"
      result.cause.errorCode shouldBe UnknownErrorCode
      result.cause.exception shouldBe Some("IllegalArgumentException")
    }

    it("known exceptions should have a defined error code"){
      val result = FailurePayload( SampleServerError )

      result.statusCode shouldBe Conflict
      result.cause.message   shouldBe "Sample Service Error"
      result.cause.errorCode shouldBe ErrorCode("ERR20001")
      result.cause.exception shouldBe Some("TestException")
    }

    it("unknown StatusCodes should be mapped to UnknownError"){
      val result = FailurePayload( Conflict )

      result.statusCode      shouldBe Conflict
      result.cause.message   shouldBe Conflict.defaultMessage
      result.cause.errorCode shouldBe UnknownErrorCode
      result.cause.exception shouldBe None
    }

    it("unknown StatusCodes should be mapped to defined to ErrorCodes if a mapping function is defined"){
      implicit val SampleErrorMapper:PartialFunction[StatusCode, ErrorCode] = {
        case Conflict => SampleErrorCode
      }

      val result = FailurePayload( Conflict )

      result.statusCode      shouldBe Conflict
      result.cause.message   shouldBe Conflict.defaultMessage
      result.cause.errorCode shouldBe SampleErrorCode
      result.cause.exception shouldBe None
    }
  }
}

object FailurePayloadTest{
  val SampleErrorCode = ErrorCode("ERR55555")

}

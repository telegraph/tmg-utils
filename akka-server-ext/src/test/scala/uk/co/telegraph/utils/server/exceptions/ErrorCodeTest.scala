package uk.co.telegraph.utils.server.exceptions

import org.scalatest.{FunSpec, Matchers}

class ErrorCodeTest extends FunSpec with Matchers {

  describe("Given the 'ErrorCode' object, "){
    it("It should thrown an exception if the format is not valid"){
      val result = intercept[AssertionError]{
        ErrorCode("ERRxptoa")
      }
      result.getMessage shouldBe "assertion failed: The error code should match the pattern 'ERRxxxxx'"
    }

    it("I should be able to create a valid error if I follow the syntax"){
      val result = ErrorCode("ERR12345")
      result.code shouldBe "ERR12345"
    }
  }
}

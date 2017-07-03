package uk.co.telegraph.utils

import akka.http.scaladsl.model.StatusCodes
import uk.co.telegraph.utils.server.exceptions.{ErrorCode, ServerException}
import uk.co.telegraph.utils.server.model.{FailurePayload, ResponseMsg}

package object server {

  case class TestException(message:String) extends ServerException(message, null){
    val statusCode = StatusCodes.Conflict
    val errorCode  = ErrorCode("ERR20001")
  }
  val SampleServerError = TestException("Sample Service Error")
  val SampleFailure     = FailurePayload(SampleServerError)
  val SampleResponse:ResponseMsg = SampleFailure
  val SampleResponseStr:String = """{"statusCode":409,"cause":{"message":"Sample Service Error","exception":"TestException","errorCode":"ERR20001"}}"""
}

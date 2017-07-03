package uk.co.telegraph.utils.server.exceptions

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{StatusCode, StatusCodes}

import scala.language.implicitConversions
import scala.util.matching.Regex

case class ErrorCode(code:String){
  assert( new Regex("ERR\\d{5}").pattern.matcher(code).matches(), "The error code should match the pattern 'ERRxxxxx'" )
}

object ErrorCodes {
  // System Status Codes
  val BadRequestErrorCode     = ErrorCode("ERR10001")
  val UnauthorizedErrorCode   = ErrorCode("ERR10002")
  val GatewayTimeoutErrorCode = ErrorCode("ERR10003")
  val InternalServerErrorCode = ErrorCode("ERR10004")

  val UnknownErrorCode        = ErrorCode("ERR99999")

  def DefaultStatusMapper:PartialFunction[StatusCode, ErrorCode] = {
    case BadRequest => BadRequestErrorCode
    case Unauthorized => UnauthorizedErrorCode
    case GatewayTimeout => GatewayTimeoutErrorCode
    case InternalServerError => InternalServerErrorCode
    case _ => UnknownErrorCode
  }
}

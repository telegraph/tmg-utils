package uk.co.telegraph.utils.server.exceptions

import akka.http.scaladsl.model.StatusCodes.InternalServerError

import MonitorException._

case class MonitorException(message:String, cause:Throwable) extends ServerException(message, cause) {
  override val statusCode = MonitoringStatusCode
  override val errorCode = MonitoringErrorCode
}

object MonitorException {
  val MonitoringStatusCode = InternalServerError
  val MonitoringErrorCode = ErrorCode("ERR10005")
}

package uk.co.telegraph.utils.server.exceptions

import akka.http.scaladsl.model.StatusCode

abstract class ServerException(message:String, cause:Throwable) extends RuntimeException(message, cause) {
  val statusCode:StatusCode
  val errorCode :ErrorCode
}

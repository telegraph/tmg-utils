package uk.co.telegraph.utils.server.exceptions

abstract class ServerException(message:String, cause:Throwable)
  extends RuntimeException(message, cause)

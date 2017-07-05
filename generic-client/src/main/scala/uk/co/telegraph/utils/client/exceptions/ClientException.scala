package uk.co.telegraph.utils.client.exceptions

abstract class ClientException(message:String, cause:Throwable) extends RuntimeException(message, cause)

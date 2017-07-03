package uk.co.telegraph.utils.client.http.exceptions

abstract class HttpClientException(message:String, cause:Throwable) extends RuntimeException(message, cause)
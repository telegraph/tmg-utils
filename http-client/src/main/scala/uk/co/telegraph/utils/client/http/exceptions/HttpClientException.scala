package uk.co.telegraph.utils.client.http.exceptions

import uk.co.telegraph.utils.client.exceptions.ClientException

abstract class HttpClientException
(
  message: String,
  cause  : Throwable
) extends ClientException(message, cause)

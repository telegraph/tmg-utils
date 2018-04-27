package uk.co.telegraph.utils.server.metrics

import play.api.mvc._


class EndpointRequestMatcher(
  method        : String,
  pathMatching  : (String) => Boolean
) {
  def matches(requestHeader: RequestHeader): Boolean =
    requestHeader.method == method && pathMatching(requestHeader.uri)
}

class EndpointRequestAndResultMatcher(
  val method         : String,
  val pathMatching   : (String) => Boolean,
  val resultMatching : (Result) => Boolean
) extends EndpointRequestMatcher(method, pathMatching)
{
  def matches(requestHeader: RequestHeader, result: Result): Boolean =
    super.matches(requestHeader) && resultMatching(result)
}



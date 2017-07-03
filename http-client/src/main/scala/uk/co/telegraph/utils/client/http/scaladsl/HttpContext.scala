package uk.co.telegraph.utils.client.http.scaladsl

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

case class HttpContext(request: HttpRequest, response: HttpResponse)

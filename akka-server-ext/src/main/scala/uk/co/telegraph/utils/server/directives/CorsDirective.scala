package uk.co.telegraph.utils.server.directives

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.{Directives, Route}

import scala.language.implicitConversions

trait CorsDirective extends Directives{

  import CorsDirective._

  /**
   * Allow All Domains
   */
  def allowAllCORS(routes: => Route): Route = {
    withCORS( HttpOriginRange.* )(routes)
  }

  /**
   * Allow Some Origins
   */
  def allowOriginsCORS(origin:String*)(routes: => Route): Route = {
    val httpOrigin = origin.map(HttpOrigin(_))
    withCORS(HttpOriginRange(httpOrigin: _*))(routes)
  }

  /**
   * Wrap the route with CORS
   */
  private def withCORS( origins: HttpOriginRange )(routes: => Route ): Route = {
    val originHeader = `Access-Control-Allow-Origin`(origins)
    respondWithHeaders( originHeader :: CORSHeaders ) {
      routes ~ options {
        complete(OK)
      }
    }
  }
}

object CorsDirective extends CorsDirective{

  private val CORSHeaders = List(
    `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT, HttpMethods.DELETE, HttpMethods.OPTIONS),
    `Access-Control-Allow-Headers`(
      Authorization.name,
      Origin.name,
      Accept.name,
      Host.name,
      `Content-Type`.name,
      `Accept-Encoding`.name,
      `Accept-Language`.name,
      `User-Agent`.name
    ),
    `Access-Control-Allow-Credentials`(true)
  )
}
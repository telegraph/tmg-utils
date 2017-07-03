package uk.co.telegraph.utils.server.filters

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import play.api.Configuration
import play.api.mvc.Results._
import play.api.mvc.{Filter, RequestHeader, Result}
import uk.co.telegraph.utils.server.filters.AuthFilter._

import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class AuthFilter @Inject()(config:Configuration)(implicit val mat:Materializer)
  extends Filter
{
  private val securePaths:Set[String] = config.getOptional[Seq[String]]("app.auth.paths")
    .map(_.toSet)
    .getOrElse(DefaultPaths)

  private val authToken = {
    val username:String = config.getOptional[String]("app.auth.user").getOrElse(DefaultUsername)
    val password:String = config.getOptional[String]("app.auth.pwd" ).getOrElse(DefaultPassword)

    "Basic " + Base64.getEncoder.encodeToString( s"$username:$password".getBytes(UTF_8) )
  }

  def apply(f: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    if (securePaths.exists(request.path endsWith _)) {
      if( !request.headers.get("Authorization").contains(authToken) ) {
        return UnauthorizedResponse
      }
    }
    f(request)
  }
}

object AuthFilter {
  val DefaultUsername      = "admin"
  val DefaultPassword      = "admin"
  val DefaultPaths         = Set("/admin")
  val UnauthorizedResponse: Future[Result] = successful(Unauthorized.withHeaders("WWW-Authenticate" -> "Basic realm=Unauthorized"))
}

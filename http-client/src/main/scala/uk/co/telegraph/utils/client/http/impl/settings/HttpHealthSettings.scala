package uk.co.telegraph.utils.client.http.impl.settings

import akka.http.scaladsl.model.HttpMethods.HEAD
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import com.typesafe.config.Config
import uk.co.telegraph.utils._

abstract class HttpHealthSettings {
  def method: HttpMethod
  def path: String
}

object HttpHealthSettings extends Settings[HttpHealthSettings]("tmg.http.client.health"){

  private case class HttpHealthSettingsImpl
  (
    method:HttpMethod,
    path:String
  ) extends HttpHealthSettings

  protected def fromSubConfig(root: Config, inner: Config): HttpHealthSettings = {
    val conf:Config = inner.withFallback(root getConfig prefix )

    HttpHealthSettingsImpl(
      method = conf.getOption[String]("method").flatMap{ x => HttpMethods.getForKey(x.toUpperCase) } getOrElse HEAD,
      path   = conf.get[String]("path")
    )
  }
}

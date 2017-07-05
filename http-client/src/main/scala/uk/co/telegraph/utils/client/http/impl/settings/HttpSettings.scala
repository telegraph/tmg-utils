package uk.co.telegraph.utils.client.http.impl.settings

import java.net.URL

import akka.http.scaladsl.model.HttpMethods.HEAD
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import com.typesafe.config.Config

import scala.language.{implicitConversions, postfixOps}
import uk.co.telegraph.utils._

sealed abstract class HttpSettings {
  def port          : Int
  def host          : String
  def baseUrl       : String
  def parallelism   : Int
  def isSecure      : Boolean
  def defaultHeaders: Map[String, String]
  def health        : HttpHealthSettings
  def connectionPool: ConnectionPoolSettings
}

object HttpSettings extends Settings[HttpSettings]("tmg.http.client"){
  val AkkaDefaultConnectionPoolConfig = "akka.http.host-connection-pool"

  private case class HttpSettingsImpl(
    port             : Int,
    host             : String,
    baseUrl          : String,
    parallelism      : Int,
    isSecure         : Boolean,
    defaultHeaders   : Map[String, String],
    health           : HttpHealthSettings,
    connectionPool   : ConnectionPoolSettings
  ) extends HttpSettings

  protected def fromSubConfig(root:Config, inner:Config): HttpSettings ={
    val conf:Config = inner.withFallback(root getConfig prefix)
    val url:URL     = new URL(conf getString "baseUrl")

    HttpSettingsImpl(
      port              = Option(url.getPort).filter( _ != -1).getOrElse(url.getDefaultPort),
      host              = url.getHost,
      baseUrl           = conf  getString  "baseUrl",
      defaultHeaders    = conf  getMap     "headers",
      parallelism       = conf  getInt     "parallelism",
      isSecure          = (conf getBoolean "secure") || url.getProtocol == "https",
      health            = HttpHealthSettings    (root, inner tryGetConfig "health"),
      connectionPool    = ConnectionPoolSettings{
        val connectionPoolConf = (conf.tryGetConfig("host-connection-pool") prefixKeysWith AkkaDefaultConnectionPoolConfig).withFallback(root)
        connectionPoolConf
      }
    )
  }

  implicit private def toHttpMethod(method:String): HttpMethod =
    HttpMethods.getForKey(method.toUpperCase).getOrElse(HEAD)
}

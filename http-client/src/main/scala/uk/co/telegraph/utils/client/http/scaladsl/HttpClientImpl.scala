package uk.co.telegraph.utils.client.http.scaladsl

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.Config
import uk.co.telegraph.utils.client.http.impl.HttpClient
import uk.co.telegraph.utils.client.http.impl.settings.HttpSettings

class HttpClientImpl(configPath:String)(implicit val system:ActorSystem, val materializer: Materializer) extends HttpClient {
  lazy val endpointConfig: Config = system.settings.config.getConfig(configPath)
  lazy val settings: HttpSettings = HttpSettings(endpointConfig)
}

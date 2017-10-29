package uk.co.telegraph.utils.client.http.scaladsl

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.Config
import uk.co.telegraph.utils.client.http.impl.HttpClient
import uk.co.telegraph.utils.client.http.impl.settings.HttpSettings

class HttpClientImpl(config:Config)(implicit val system:ActorSystem, val materializer: Materializer)
  extends HttpClient
{

  def this(configPath:String)(implicit system:ActorSystem, materializer: Materializer) = {
    this(system.settings.config.getConfig(configPath))
  }

  lazy val endpointConfig: Config = config
  lazy val settings: HttpSettings = HttpSettings(system.settings.config, endpointConfig)
}


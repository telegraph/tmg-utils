package uk.co.telegraph.utils.client.http.impl.settings

import akka.actor.ActorSystem
import com.typesafe.config.Config

private [settings] abstract class Settings[T](protected val prefix:String){

  def apply(inner:Config)(implicit system:ActorSystem):T = apply(system.settings.config, inner)

  def apply(root:Config, inner:Config):T = fromSubConfig(root, inner)

  protected def fromSubConfig(root:Config, inner:Config): T
}

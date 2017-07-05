package uk.co.telegraph.utils

import akka.actor.ActorSystem
import com.typesafe.config.Config

abstract class Settings[T](protected val prefix:String){

  def apply()(implicit system:ActorSystem):T = apply(system.settings.config)

  def apply(inner:Config)(implicit system:ActorSystem):T = apply(system.settings.config, inner)

  def apply(root:Config, inner:Config):T = fromSubConfig(root, inner)

  protected def fromSubConfig(root:Config, inner:Config): T
}

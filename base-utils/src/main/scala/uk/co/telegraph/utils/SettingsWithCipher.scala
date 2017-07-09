package uk.co.telegraph.utils

import akka.actor.ActorSystem
import com.typesafe.config.Config
import uk.co.telegraph.utils.cipher.Decrypter

abstract class SettingsWithCipher[T](protected val prefix:String){

  def apply()(implicit system:ActorSystem, decrypter: Decrypter):T = apply(system.settings.config)

  def apply(inner:Config)(implicit system:ActorSystem, decrypter: Decrypter):T = apply(system.settings.config, inner)

  def apply(root:Config, inner:Config)(implicit decrypter: Decrypter):T = fromSubConfig(root, inner)

  protected def fromSubConfig(root:Config, inner:Config)(implicit decrypter: Decrypter): T
}



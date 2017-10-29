package uk.co.telegraph.utils.settings

import com.typesafe.config.Config
import uk.co.telegraph.utils.cipher.{Decrypter, ZeroDecrypter}

abstract class Settings[T](protected val defaultPrefix:String){

  def apply(root:Config, inner:Config)(implicit decrypter: Decrypter = ZeroDecrypter):T =
    fromSubConfig(root, inner)

  protected def fromSubConfig(root:Config, inner:Config): T
}

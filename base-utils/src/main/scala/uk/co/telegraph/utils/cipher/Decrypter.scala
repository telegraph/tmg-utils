package uk.co.telegraph.utils.cipher

import java.nio.ByteBuffer
import java.nio.ByteBuffer.wrap
import java.util.Base64.getDecoder

import uk.co.telegraph.utils.cipher.Decrypter.Cipher

trait Decrypter {

  def decrypt(base64Cipher:Cipher):String = {
    val result = decipher( wrap(getDecoder.decode(base64Cipher) ))

    new String(result.array)
  }

  protected def decipher(buffer:ByteBuffer):ByteBuffer
}

object Decrypter{
  type Cipher = String
}

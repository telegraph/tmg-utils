package uk.co.telegraph.utils.cipher

import java.nio.ByteBuffer

import org.scalamock.function.StubFunction1
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}
import uk.co.telegraph.utils.cipher.DecrypterSpec._

class DecrypterSpec
  extends FreeSpec
  with Matchers
  with MockFactory
{

  val decipherFnc: StubFunction1[ByteBuffer, ByteBuffer] = stubFunction[ByteBuffer, ByteBuffer]
  val decrypter:Decrypter = new Base64Decrypter{
    def decipher(buffer: ByteBuffer): ByteBuffer = decipherFnc(buffer)
  }

  "Given 'Base64Decrypter'" - {
    "It should only decrypt Base64 cyphers" in {
      decipherFnc when * onCall identity[ByteBuffer] _

      decrypter.decrypt(DecipherSecret) should include ("""username: "user1"""")
      decrypter.decrypt(DecipherSecret) should include ("""password: "pass2"""")
    }
  }

  "Given 'ZeroDecrypter' " - {
    "It should return always the same valye" in {
      ZeroDecrypter.decrypt(DecipherSecret) shouldBe DecipherSecret
    }
  }
}

object DecrypterSpec {
  val DecipherSecret = "ew0KICB1c2VybmFtZTogInVzZXIxIg0KICBwYXNzd29yZDogInBhc3MyIg0KfQ=="
}

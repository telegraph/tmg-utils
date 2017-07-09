package uk.co.telegraph.utils.config

import java.net.URL

import com.typesafe.config.{Config, ConfigFactory}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import uk.co.telegraph.utils._
import uk.co.telegraph.utils.cipher.Decrypter
import uk.co.telegraph.utils.cipher.Decrypter.Cipher
import uk.co.telegraph.utils.config.ConfigExtensionsSpec._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

class ConfigExtensionsSpec
  extends FreeSpec
  with Matchers
  with MockFactory
  with OneInstancePerTest
{

  implicit val decypher: Decrypter = stub[Decrypter]

  "Given a 'ConfigSample' object"- {
    "It should be possible to extract an url" in {
      ConfigSample.get[URL]("app.client.baseUrl") shouldBe new URL("http://www.test.com:1999")
    }
    "It should be possible to extract an Integer" in {
      ConfigSample.get[Int]("app.client.maxResults") shouldBe 10
    }
    "It should be possible to extract a Double" in {
      ConfigSample.get[Double]("app.client.defaultPrice") shouldBe 10.5
    }
    "It should be possible to extract a FiniteDuration" in {
      ConfigSample.get[FiniteDuration]("app.client.timeout") shouldBe (10 seconds)
    }
    "It should be possible to extract a Boolean" in {
      ConfigSample.get[Boolean]("app.client.isSecure") shouldBe true
    }
    "It should be possible to extract an Option" in {
      ConfigSample.getOption[Int]("app.client.connections") shouldBe Some(9)
      ConfigSample.getOption[Int]("app.client.retry-connections") shouldBe None
    }
    "It should be possible to extract a Try" in {
      ConfigSample.getTry[Int]("app.client.connections") shouldBe Success(9)
      ConfigSample.getTry[Int]("app.client.retry-connections").isFailure shouldBe true
    }
    "It should be possible to extract a Map[String,String]" in {
      ConfigSample.toMap[String]("app.client.map") shouldBe Map("param1" -> "value1", "param2" -> "value2", "param3" -> "value3")
    }
    "It should be possible to extract a Configuration" in {
      ConfigSample.getConfigOrEmpty("app.client.map") shouldBe SubConfigSample
      ConfigSample.getConfigOrEmpty("app.client.plain-text") shouldBe ConfigFactory.empty()
    }

    "It should be possible to prefix config keys" in {
      val newConfig = ConfigSample.getConfig("app.client").prefixKeysWith("app.test.client")

      newConfig.get[URL]("app.test.client.baseUrl") shouldBe new URL("http://www.test.com:1999")
      newConfig.get[Int]("app.test.client.maxResults") shouldBe 10
      newConfig.get[Double]("app.test.client.defaultPrice") shouldBe 10.5
      newConfig.get[FiniteDuration]("app.test.client.timeout") shouldBe (10 seconds)
      newConfig.getOption[Int]("app.test.client.connections") shouldBe Some(9)
      newConfig.getTry[Int]("app.test.client.connections") shouldBe Success(9)
      newConfig.toMap[String]("app.test.client.map") shouldBe Map("param1" -> "value1", "param2" -> "value2", "param3" -> "value3")
      newConfig.getConfigOrEmpty("app.test.client.map") shouldBe SubConfigSample
    }

    "it should be possible to get an encrypted value" in {
      decypher.decrypt _ when DecipherParamRequest returns DecipherParamResult

      ConfigSample.getEncrypted[String]("app.client.password") shouldBe "secret-password"
    }

    "it should be possible to get an encrypted config" in {
      decypher.decrypt _ when DecipherConfigRequest returns DecipherConfigResult

      val config = ConfigSample.getEncryptedConfig("app.client.secrets")

      config.get[String]("username") shouldBe "user1"
      config.get[String]("password") shouldBe "pass2"
    }
  }
}


object ConfigExtensionsSpec{

  val ConfigSample: Config = ConfigFactory.parseString(
    """
      |app {
      |  client {
      |    baseUrl     : "http://www.test.com:1999"
      |    maxResults  : 10
      |    defaultPrice: 10.5
      |    timeout     : 10 seconds
      |    connections :  9
      |    isSecure    : true
      |    map {
      |      param1: "value1"
      |      param2: "value2"
      |      param3: "value3"
      |    }
      |    password: "dGhpcy1pcy1hLXRlc3Q="
      |    secrets : "ew0KICB1c2VybmFtZTogInVzZXIxIg0KICBwYXNzd29yZDogInBhc3MyIg0KfQ=="
      |  }
      |}
    """.stripMargin)

  val SubConfigSample: Config = ConfigFactory.parseString(
    s"""
       |{
       |  param1: "value1"
       |  param2: "value2"
       |  param3: "value3"
       |}
     """.stripMargin)

  val DecipherParamRequest:Cipher = "dGhpcy1pcy1hLXRlc3Q="
  val DecipherParamResult :String = "secret-password"

  val DecipherConfigRequest:Cipher = "ew0KICB1c2VybmFtZTogInVzZXIxIg0KICBwYXNzd29yZDogInBhc3MyIg0KfQ=="
  val DecipherConfigResult :String = """{
     |  username: "user1"
     |  password: "pass2"
     |}
   """.stripMargin
}

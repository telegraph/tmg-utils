package uk.co.telegraph.utils.client.http.impl.settings

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.{HEAD, OPTIONS}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

import scala.language.postfixOps
import scala.concurrent.duration._

class HttpSettingsTest extends FreeSpec with Matchers with BeforeAndAfterAll {

  import HttpSettingsTest._

  override protected def afterAll(): Unit = {
    TestActorSystem.terminate()
  }

  "Given the 'HttpSettings', " - {

    "I should be able to map a valid config to HttpSettings" in {
      val settings = HttpSettings(SampleConfig)

      settings.baseUrl           shouldBe "http://test-service:21123/sample-service"
      settings.health.method     shouldBe OPTIONS
      settings.health.path       shouldBe "/ping"
      settings.defaultHeaders    shouldBe Map("x-auth" -> "some-key")
      settings.isSecure          shouldBe false
      settings.host              shouldBe "test-service"
      settings.port              shouldBe 21123
      settings.parallelism       shouldBe 10
      settings.connectionPool.connectionSettings.idleTimeout       shouldBe (180 seconds)
      settings.connectionPool.connectionSettings.connectingTimeout shouldBe   (5 seconds)
    }

    "I should be able to use default settings if some fields are not present" in {
      val settings = HttpSettings(SampleConfigWithoutSomeFields)

      settings.baseUrl        shouldBe "http://test-service:21123/sample-service"
      settings.health.method  shouldBe HEAD
      settings.health.path    shouldBe "/health"
      settings.defaultHeaders shouldBe Map.empty
      settings.isSecure       shouldBe false
      settings.host           shouldBe "test-service"
      settings.port           shouldBe 21123
      settings.parallelism    shouldBe 5
    }
//
//    "If the HTTP Method does not exist It should fallback to 'HEAD'" in {
//      val result:HttpMethod = "TEST"
//      result shouldBe HEAD
//    }

    "I should get a secure connection when using 'https'" in {
      val settings = HttpSettings(SampleConfigWithSSL)

      settings.baseUrl        shouldBe "https://test-service:21123/sample-service"
      settings.isSecure       shouldBe true
      settings.port           shouldBe 21123
    }

    "I should get port 80 when using 'http' protocol and not setting the port in the Url" in {
      val settings = HttpSettings(SampleConfigWithoutPort)

      settings.baseUrl        shouldBe "http://test-service/sample-service"
      settings.isSecure       shouldBe false
      settings.port           shouldBe 80
    }

    "I should get port 443 when using 'https' protocol and not setting the port in the Url" in {
      val settings = HttpSettings(SampleConfigSSLWithoutPort)

      settings.baseUrl        shouldBe "https://test-service/sample-service"
      settings.isSecure       shouldBe true
      settings.port           shouldBe 443
    }
  }
}

object HttpSettingsTest{
  implicit val TestActorSystem = ActorSystem()

  val Config                       : Config = ConfigFactory.load("application.tst.conf")
  val SampleConfig                 : Config = Config.getConfig("app.sample-1")
  val SampleConfigWithoutSomeFields: Config = Config.getConfig("app.sample-2")
  val SampleConfigWithSSL          : Config = Config.getConfig("app.sample-3")
  val SampleConfigWithoutPort      : Config = Config.getConfig("app.sample-4")
  val SampleConfigSSLWithoutPort   : Config = Config.getConfig("app.sample-5")
}

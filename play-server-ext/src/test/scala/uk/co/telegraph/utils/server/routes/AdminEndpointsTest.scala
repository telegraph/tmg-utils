package uk.co.telegraph.utils.server.routes

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest._
import play.api.Configuration
import play.api.http.{MimeTypes, Status}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, await, contentAsString, stubControllerComponents}
import uk.co.telegraph.utils.server.routes.AdminEndpointsTest._

import scala.concurrent.duration._
import scala.language.postfixOps

class AdminEndpointsTest
  extends FreeSpec
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
{
  val endpoint = new AdminEndpoints(Configuration(ConfigTest), stubControllerComponents())

  implicit val TimeoutTest:Timeout = 3 seconds

  "Given the admin endpoint," - {

    "I should be able to get a JSON version of my config" in {
      val request = FakeRequest(GET, "/admin")
      val response = endpoint.getConfig.apply(request)

      val content = contentAsString(response)
      val result = await(response)

      result.body.contentType shouldBe Some(MimeTypes.JSON)
      result.header.status shouldBe Status.OK
      content shouldBe """{"app":{"param1":"value1","param2":["value21","value22"],"param3":"30 seconds"}}"""
    }
  }
}

object AdminEndpointsTest{
  val ConfigTest: Config = ConfigFactory.parseString(
    """
      |app {
      |   param1: "value1"
      |   param2: [
      |     "value21"
      |     "value22"
      |   ]
      |   param3: 30 seconds
      |}
    """.stripMargin)
  implicit val ActorSystemTest = ActorSystem("sample-system", ConfigTest)
  implicit val MaterializerTest = ActorMaterializer()
}

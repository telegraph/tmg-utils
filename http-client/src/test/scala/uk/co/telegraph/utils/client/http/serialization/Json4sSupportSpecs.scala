package uk.co.telegraph.utils.client.http.serialization

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.core.io.JsonEOFException
import org.json4s.{DefaultFormats, MappingException}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import uk.co.telegraph.utils.client.http.serialization.Json4sSupportSpecs._

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class Json4sSupportSpecs extends FreeSpec
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll
{
  import ActorSystemTest.dispatcher
  import Json4sSupport._
  implicit val formats = DefaultFormats

  override protected def afterAll(): Unit = {
    ActorMaterializerTest.shutdown()
    ActorSystemTest.terminate()
  }


  "Given Json4sSupport," -{
    "I should be able to Marshal" - {
      "an objects" in {
        whenReady(Marshal(SampleCard).to[model.MessageEntity].flatMap(_.toStrict(10 seconds) )){ res =>
          res.contentType     shouldBe `application/json`
          res.data.utf8String shouldBe SampleCardJson
        }
      }
    }

    "I should be able to UnMarshal" - {
      "a Json payload" in {
        val entity:HttpEntity = HttpEntity(
          contentType = `application/json`,
          string      =  SampleCardJson
        )
        whenReady(Unmarshal(entity).to[Card]){ res =>
          res shouldBe SampleCard
        }
      }

      "an Empty Payload and get an exception" in {
        val entity:HttpEntity = HttpEntity(
          contentType = `application/json`,
          string      =  ""
        )
        whenReady(Unmarshal(entity).to[Card].failed){ ex =>
          ex shouldBe Unmarshaller.NoContentException
        }
      }

      "an Invalid Payload and get an exception" in {
        val entity:HttpEntity = HttpEntity(
          contentType = `application/json`,
          string      =  """{"name":"special-card","picture":{"width":10,"height":20}"""
        )
        whenReady(Unmarshal(entity).to[Card].failed){ ex =>
          ex shouldBe a [MappingException]
        }
      }
    }
  }
}

object Json4sSupportSpecs{

  implicit val ActorSystemTest = ActorSystem()
  implicit val ActorMaterializerTest = ActorMaterializer()
  case class Picture(width:Int, height:Int)
  case class Card(name:String, picture:Picture)

  val SampleCard = Card(
    name     = "special-card",
    picture  = Picture(
      width  = 10,
      height = 20
    )
  )

  val SampleCardJson = """{"name":"special-card","picture":{"width":10,"height":20}}"""
}

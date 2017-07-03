package uk.co.telegraph.utils.client.http.serialization

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{Marshal, Marshalling, ToEntityMarshaller}
import akka.http.scaladsl.model
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, MessageEntity}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import org.json4s.DefaultFormats
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, Matchers}
import uk.co.telegraph.utils.client.http.serialization.Json4sSupportTest._

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class Json4sSupportTest extends TestKit(ActorSystemTest)
  with FunSpecLike
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

  def encoder[A]( obj:A )(implicit marshaller:ToEntityMarshaller[A]): Future[List[Marshalling[MessageEntity]]] = {
    marshaller(obj)
  }

  describe("Given Json4sSupport,"){
    it("I should be able to Marshal objects"){
      val entity:HttpEntity = HttpEntity(
        contentType = `application/json`,
        string = SampleCardJson
      )
      whenReady(Unmarshal(entity).to[Card]){ res =>
        res shouldBe SampleCard
      }
    }

    it("I should be able to UnMarshal Json payload"){

      val result = Source
        .fromFuture(Marshal(SampleCard).to[model.MessageEntity])
        .flatMapConcat( x => x.dataBytes )
        .map(_.utf8String)
        .runWith(Sink.head)
      whenReady(result){ res =>
        res shouldBe SampleCardJson
      }
    }

  }
}

object Json4sSupportTest{

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
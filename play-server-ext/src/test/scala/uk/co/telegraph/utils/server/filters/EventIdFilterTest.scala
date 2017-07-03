package uk.co.telegraph.utils.server.filters

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HttpEntity
import play.api.mvc.{Filter, RequestHeader, ResponseHeader, Result}
import play.api.test.FakeRequest
import uk.co.telegraph.utils.server.filters.EventIdFilter.EventIdHeader

import scala.concurrent.Future
import scala.concurrent.Future.successful

class EventIdFilterTest
  extends FunSpec
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with OneInstancePerTest
  with ScalaFutures
{

  import EventIdFilterTest._

  val filter:Filter = new EventIdFilter()(ActorMaterializerTest)
  val mapper:RequestHeader => Future[Result] = request => {
    successful(Result(
      header = ResponseHeader(
        status = 200,
        headers = request.headers.toSimpleMap
      ),
      body = HttpEntity.NoEntity
    ))
  }

  override protected def afterAll(): Unit = {
    ActorMaterializerTest.shutdown()
    ActorSystemTest.terminate()
  }

  describe("Given the EventIdFilter, "){
    it("I should be able to inject a x-event-id value in the header"){
      val request = FakeRequest("Get", "/health")

      whenReady(filter(mapper)(request)){ res =>
        res.header.headers.get(EventIdHeader) should not be None
      }
    }

    it("If it already exists, I should use it"){
      val request = FakeRequest("Get", "/health")
        .withHeaders( EventIdHeader -> EventId)

      whenReady(filter(mapper)(request)){ res =>
        res.header.headers(EventIdHeader) shouldBe EventId
      }
    }
  }
}

object EventIdFilterTest{

  val EventId = "11111111-1111-1111-1111-111111111111"

  implicit val ActorSystemTest = ActorSystem("test-system")
  implicit val ActorMaterializerTest = ActorMaterializer()


}

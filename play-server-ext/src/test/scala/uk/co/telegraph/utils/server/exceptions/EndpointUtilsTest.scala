package uk.co.telegraph.utils.server.exceptions

import java.util.UUID
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.Json
import uk.co.telegraph.utils.error.FailureResponse
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EndpointUtilsTest
  extends FreeSpec
    with Matchers
    with EndpointUtils
    with ScalaFutures {

  "EndpointUtils" - {

    "errorHandler" - {

      "handle [[Throwable]] with [[NotFound]] to [[Result.NotFound]] containing [[ErrorResponse]]" in {
        case class A() extends Throwable with NotFound
        val t = A()
        val input = Future.failed(t)
        val uuid = UUID.randomUUID()
        val actual = input.recover(errorHandler(Some(uuid)))
        val err = FailureResponse(t)
        val expected = NotFound(
          Json.toJson(
            err.copy(message = s"${err.message} ${uuid.toString}")
          )
        )

        whenReady(actual)(_ shouldBe expected)
      }

      "handle [[Throwable]] with [[InternalServerError]] to [[Result.InternalServerError]] containing [[ErrorResponse]]" in {
        case object A extends Throwable with InternalServerError
        val input = Future.failed(A)
        val uuid = UUID.randomUUID()
        val actual = input.recover(errorHandler(Some(uuid)))
        val err = FailureResponse(A)
        val expected = InternalServerError(
          Json.toJson(
            err.copy(message = s"${err.message} ${uuid.toString}")
          )
        )

        whenReady(actual)(_ shouldBe expected)
      }

      "handle [[Throwable]] with [[BadRequest]] to [[Result.BadRequest]] containing [[ErrorResponse]]" in {
        case object A extends Throwable with BadRequest
        val input = Future.failed(A)
        val uuid = UUID.randomUUID()
        val actual = input.recover(errorHandler(Some(uuid)))
        val err = FailureResponse(A)
        val expected = BadRequest(
          Json.toJson(
            err.copy(message = s"${err.message} ${uuid.toString}")
          )
        )

        whenReady(actual)(_ shouldBe expected)
      }

      "handle [[Throwable]] to [[Result.InternalServerError]] containing [[ErrorResponse]]" in {
        case object A extends Throwable
        val input = Future.failed(A)
        val uuid = UUID.randomUUID()
        val actual = input.recover(errorHandler(Some(uuid)))
        val err = FailureResponse(A)
        val expected = InternalServerError(
          Json.toJson(
            err.copy(message = s"${err.message} ${uuid.toString}")
          )
        )

        whenReady(actual)(_ shouldBe expected)
      }
    }
  }
}

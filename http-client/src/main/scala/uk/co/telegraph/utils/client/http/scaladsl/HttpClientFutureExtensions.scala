package uk.co.telegraph.utils.client.http.scaladsl

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import akka.stream.Materializer
import uk.co.telegraph.utils.client.http.exceptions.{HttpInvalidStatusException, HttpRawDataException, HttpUnmarshallingException}
import uk.co.telegraph.utils.client.http.scaladsl.HttpClientFutureExtensions._

import scala.concurrent.Future.{failed, successful}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

private [scaladsl] case class HttpClientFutureExtensions(left:Future[HttpContext])(implicit ec:ExecutionContext, mat:Materializer){

  /**
    * Operator to can be used to filter by status
    */
  def filterByStatus(scVar:StatusCode*):Future[HttpContext] = {
    val validStatus = Option(scVar).filter(_.nonEmpty).getOrElse( DefaultStatus )

    left.flatMap({
      case ctx if validStatus.contains(ctx.response.status) => left
      case ctx => left.ignorePayload
        .flatMap{ _ => failed(HttpInvalidStatusException(ctx.request, ctx.response)) }
    })
  }

  /**
    * This operator can be used to unmarshal the response
    */
  def unmarshalTo[T](implicit um: FromEntityUnmarshaller[T]): Future[T] = {
    left.flatMap{ ctx => Unmarshal(ctx.response.entity).to[T]
      .recoverWith{
        case ex:Throwable =>
          left.ignorePayload
          failed(HttpUnmarshallingException(ctx.request, ctx.response, ex))
      }
    }
  }

  /**
    * Used to get content as raw string
    */
  def rawData(implicit timeout:FiniteDuration):Future[String] = {
    left.flatMap { ctx => ctx.response.entity.toStrict(timeout)
      .map(_.data.utf8String)
      .recoverWith {
        case ex: Throwable => left.ignorePayload.flatMap{ _ =>
          failed(HttpRawDataException(ctx.request, ctx.response, ex))
        }
      }
    }
  }

  /**
    * Used to consume a the requested payload
    */
  def ignorePayload:Future[HttpContext] = {
    for{
      ctx <- left
      _ <- Try{ctx.response.discardEntityBytes().future()} getOrElse EmptyFuture
    } yield ctx
  }
}

object HttpClientFutureExtensions{
  val EmptyFuture  : Future[Unit]    = successful(())
  val DefaultStatus: Seq[StatusCode] = Seq(OK)
}

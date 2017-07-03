package uk.co.telegraph.utils.client.http.scaladsl

import akka.NotUsed
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal}
import akka.stream.Materializer
import akka.stream.scaladsl.Source.fromFuture
import akka.stream.scaladsl.{Flow, Source}
import uk.co.telegraph.utils.client.http.exceptions.{HttpInvalidStatusException, HttpUnmarshallingException}

private [scaladsl] case class HttpClientFlowExtensions[In](left:Flow[In, HttpContext, NotUsed]){
  private val DefaultStatus = Seq(StatusCodes.OK)

  /**
    * Operator to can be used to filter by status
    */
  def filterByStatus(scVar:StatusCode*)(implicit mat: Materializer):Flow[In, HttpContext, NotUsed] = {
    val validStatus = Option(scVar).filter(_.nonEmpty).getOrElse( DefaultStatus )

    left.flatMapConcat({
      case ctx if validStatus.contains(ctx.response.status) =>
        Source.single(ctx)
      case ctx =>
        fromFuture{
          ctx.response.entity.discardBytes().future()
        }.flatMapConcat( _ =>
          Source.failed(HttpInvalidStatusException(ctx.request, ctx.response))
        )
    })
  }

  /**
    * This operator can be used to unmarshal the response
    */
  def unmarshalTo[T](implicit um: FromEntityUnmarshaller[T], mat: Materializer): Flow[In, T, NotUsed] = {
    import mat.executionContext

    left.flatMapConcat{ctx => fromFuture(Unmarshal(ctx.response.entity).to[T]).mapError{
        case ex:Throwable => HttpUnmarshallingException(ctx.request, ctx.response, ex)
      }
    }
  }

  /**
    * Used to consume a the requested payload
    */
  def ignorePayload(implicit mat: Materializer):Flow[In, HttpContext, NotUsed] = {
    left.flatMapConcat( ctx => {
      fromFuture(ctx.response.entity.discardBytes().future())
        .map( _ => ctx)
    })
  }
}

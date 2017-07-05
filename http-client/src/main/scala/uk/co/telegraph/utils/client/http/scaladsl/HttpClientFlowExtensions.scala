package uk.co.telegraph.utils.client.http.scaladsl

import akka.NotUsed
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.stream.Materializer
import akka.stream.scaladsl.Flow

import scala.concurrent.Future.successful
import scala.concurrent.duration.FiniteDuration

private [scaladsl] case class HttpClientFlowExtensions[In](left:Flow[In, HttpContext, NotUsed])
  (implicit mat:Materializer)
{
  import mat.executionContext
  private val parallelism:Int = 5

  /**
    * Operator to can be used to filter by status
    */
  def filterByStatus(scVar:StatusCode*):Flow[In, HttpContext, NotUsed] = {
    left.mapAsync(parallelism){ ctx => successful(ctx).filterByStatus(scVar:_*) }
  }

  /**
    * This operator can be used to unmarshal the response
    */
  def unmarshalTo[T](implicit um: FromEntityUnmarshaller[T]): Flow[In, T, NotUsed] = {
    left.mapAsync(parallelism){ ctx => successful(ctx).unmarshalTo[T] }
  }

  /**
    * Used to get content as raw string
    */
  def rawData(implicit timeout:FiniteDuration):Flow[In, String, NotUsed] = {
    left.mapAsync(parallelism){ ctx => successful(ctx).rawData }
  }

  /**
    * Used to consume a the requested payload
    */
  def ignorePayload:Flow[In, HttpContext, NotUsed] = {
    left.mapAsync(parallelism){ ctx => successful(ctx).ignorePayload }
  }
}

package uk.co.telegraph.utils.server.directives

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.json4s.Formats
import uk.co.telegraph.utils.server.model.ResponseMsg

import scala.language.implicitConversions
import scala.util.{Failure, Success}

trait FlowDirectives extends Directives{

  def completeWithFlow[I]( input:I, flow:Flow[I, ResponseMsg, NotUsed])(implicit system:ActorSystem, materializer:Materializer, format:Formats):Route = {
    val fResponse = Source.single(input).via(flow).runWith(Sink.head)
    onComplete(fResponse){
      case Success(response) => response
      case Failure(ex) =>
        system.log.error(ex, s"Unhandled Exception: ${ex.getMessage}")
        ResponseMsg(ex)
    }
  }

  implicit def responseToRoute(response:ResponseMsg)(implicit format:Formats):Route = {
    complete(HttpResponse(
      status = response.statusCode,
      entity = HttpEntity(`application/json`, response.toString)
    ))
  }
}


object FlowDirectives extends FlowDirectives
package uk.co.telegraph.utils.server.filters

import java.util.UUID.randomUUID
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import play.api.mvc._

import scala.concurrent.Future

import EventIdFilter._

@Singleton
class EventIdFilter @Inject()(implicit val mat:Materializer) extends Filter{

  override def apply(f: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    if ( request.headers.get(EventIdHeader).isDefined ){
      return f(request)
    }

    val newHeaders = request.headers.add((EventIdHeader, randomUUID().toString))
    f(request.withHeaders( newHeaders ))
  }
}

object EventIdFilter{
  val EventIdHeader = "x-event-id"
}

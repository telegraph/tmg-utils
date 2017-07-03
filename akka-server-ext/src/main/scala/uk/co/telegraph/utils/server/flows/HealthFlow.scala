package uk.co.telegraph.utils.server.flows

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source.fromFuture
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.monitor.Monitor
import uk.co.telegraph.utils.server.exceptions.MonitorException
import uk.co.telegraph.utils.server.model.{HealthDto, ResponseMsg}

import scala.concurrent.Future
import scala.language.postfixOps

trait HealthFlow {

  implicit val materializer:ActorMaterializer
  implicit val system:ActorSystem

  val clients:Seq[GenericClient]
  val logger:LoggingAdapter

  lazy val endpointConfig:Config  = system.settings.config
  lazy val serviceName   :String  = endpointConfig.getString("app.name")
  lazy val serviceVersion:String  = endpointConfig.getString("app.version")
  lazy val monitor       :Monitor = Monitor(clients)

  val healthFlow:Flow[Boolean, ResponseMsg, NotUsed] = Flow[Boolean]
    .flatMapConcat( cached => fromFuture(monitor.queryHealth(freshData = !cached)) )
    .map( monitor => HealthDto(
      name    = serviceName,
      version = serviceVersion,
      cached  = monitor.cached,
      clients = monitor.clients
    ))
    .recover({
      case error =>
        logger.error(error, s"Fail to get Clients Health Status - ${error.getMessage}")
        MonitorException(s"Fail to get Clients Health Status - ${error.getMessage}", error)
    })
    .map( ResponseMsg(_) )


  def health(cached:Boolean):Future[ResponseMsg] =
    Source.single(cached).via(healthFlow).runWith(Sink.head)
}

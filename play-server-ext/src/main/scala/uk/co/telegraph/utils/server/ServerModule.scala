package uk.co.telegraph.utils.server

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides}
import com.google.inject.Key.get
import com.google.inject.multibindings.Multibinder.newSetBinder
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSFilter
import uk.co.telegraph.utils.server.filters.{AuthFilter, EventIdFilter}
import java.util.{Set => JSet}

import scala.language.postfixOps
import scala.collection.convert.WrapAsScala._
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.monitor.Monitor

class ServerModule extends AbstractModule {
  override def configure(): Unit = {
    val filterBinder = newSetBinder(binder(), get(classOf[EssentialFilter]))
    filterBinder.addBinding().to(classOf[CORSFilter])
    filterBinder.addBinding().to(classOf[EventIdFilter])
    filterBinder.addBinding().to(classOf[AuthFilter])
  }

  @Provides
  def monitorProvider(clientsSet:JSet[GenericClient])(implicit system:ActorSystem): Monitor = {
    Monitor(clientsSet.toSeq)
  }
}

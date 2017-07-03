package uk.co.telegraph.utils.server.directives

import java.util.UUID

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.directives.HeaderDirectives

/**
  * Created: rodriguesa 
  * Date   : 21/02/2017
  * Project: usage-api-service
  */
trait UuidDirectives extends HeaderDirectives{

  private val EventIdHeader = "x-event-id"

  def withEventId:Directive1[String] = {
    optionalHeaderValueByName(EventIdHeader)
      .map({
        case Some(eventId) => eventId
        case None          => UUID.randomUUID().toString
      })
  }


}

object UuidDirectives extends UuidDirectives

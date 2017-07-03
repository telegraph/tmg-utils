package uk.co.telegraph.utils.server.directives

import akka.event.Logging.{InfoLevel, LogLevel}
import akka.http.scaladsl.server.{Directive0, Directives}
import akka.http.scaladsl.server.directives.LoggingMagnet

trait LogDirectives extends Directives{

  protected def logFnc[T](logLevel: LogLevel = InfoLevel)(implicit eventId:String):LoggingMagnet[T => Unit] =
    LoggingMagnet( log => request => {
//      log.setMDC(Map("eventId" -> eventId))
      log.log(logLevel, request.toString)
//      log.clearMDC()
    })

  def logRoute(implicit eventId:String):Directive0 = {
    logRequest(logFnc()).tflatMap( _  => logResult(logFnc()) )
  }
}

object LogDirectives extends LogDirectives
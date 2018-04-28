package uk.co.telegraph.utils.server.logging

import akka.actor.ActorSystem
import akka.event.Logging._
import play.api.Logger

import scala.concurrent.Future
import scala.language.implicitConversions

trait FutureErrorLog {
  this: {val system: ActorSystem} =>

  import system.dispatcher

  class PotentialFailure[ANY](future: Future[ANY]) {
    def mapExceptionsTo[InException](exceptionMapper: InException => Throwable with WithLogLevel)(implicit exceptionManifest: Manifest[InException]): Future[ANY] = {
      future.recoverWith {
        case ex: InException =>
          try {
            failWith(exceptionMapper(ex))
          } catch {
            case _: Throwable => Future.failed(ex)
          }
      }
    }
  }

  implicit def toPotentialFailure[A](future: Future[A]): PotentialFailure[A] = {
    new PotentialFailure[A](future)
  }

  private def failWith[ANY](exception: Throwable with WithLogLevel): Future[ANY] = {
    exception.level match {
      case ErrorLevel   ⇒ Logger.error(exception.getMessage, exception)
      case WarningLevel ⇒ Logger.warn (exception.getMessage, exception)
      case InfoLevel    ⇒ Logger.info (exception.getMessage, exception)
      case DebugLevel   ⇒ Logger.debug(exception.getMessage, exception)
    }
    Future.failed(exception)
  }
}

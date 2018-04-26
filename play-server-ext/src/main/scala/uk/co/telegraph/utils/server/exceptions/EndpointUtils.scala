package uk.co.telegraph.utils.server.exceptions

import java.util.UUID

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import uk.co.telegraph.utils.error.{FailureCause, FailureResponse}
import scala.concurrent.{ExecutionContext, Future}

trait EndpointUtils
  extends Results {

  implicit lazy val failureCauseFmt = Json.format[FailureCause]
  implicit lazy val failureResponseFmt = Json.format[FailureResponse]

  /**
    * Map your [[ExceptionResult]] leaf nodes to a [[Result]]
    * or any [[Throwable]]
    *
    * Default is InternalServerError
    */
  final def errorHandler(uuid: Option[UUID] = None): PartialFunction[Throwable, Result] = {
    case t: NotFound => {
      val uuidStr = " " + uuid.map(_.toString).getOrElse("")
      Logger.error(s"NotFound$uuidStr".trim, t)
      Logger.error("Cause", t.getCause)
      val err = FailureResponse(t)
      NotFound(
        Json.toJson(
          err.copy(message = s"${err.message}$uuidStr")
        )
      )
    }
    case t: InternalServerError => {
      val uuidStr = " " + uuid.map(_.toString).getOrElse("")
      Logger.error(s"InternalServerError$uuidStr".trim, t)
      Logger.error("Cause", t.getCause)
      val err = FailureResponse(t)
      InternalServerError(
        Json.toJson(
          err.copy(message = s"${err.message}$uuidStr")
        )
      )
    }
    case t: BadRequest => {
      val uuidStr = " " + uuid.map(_.toString).getOrElse("")
      Logger.error(s"BadRequest$uuidStr".trim, t)
      Logger.error("Cause", t.getCause)
      val err = FailureResponse(t)
      BadRequest(
        Json.toJson(
          err.copy(message = s"${err.message}$uuidStr")
        )
      )
    }
    case t: Throwable => {
      val uuidStr = " " + uuid.map(_.toString).getOrElse("")
      Logger.error(s"InternalServerError$uuidStr".trim, t)
      Logger.error("Cause", t.getCause)
      val err = FailureResponse(t)
      InternalServerError(
        Json.toJson(
          err.copy(message = s"${err.message}$uuidStr")
        )
      )
    }
  }

  /**
    * @param fResult
    */
  implicit class AsyncServiceErrorHandler(fResult: Future[Result]) {

    /**
      * @param ec the execution context of the [[Future]]
      * @return a recovered [[Future]] using the [[errorHandler]]
      */
    def handleErrors(uuid: Option[UUID] = None)(implicit ec: ExecutionContext): Future[Result] = fResult.recover(errorHandler(uuid))
  }
}

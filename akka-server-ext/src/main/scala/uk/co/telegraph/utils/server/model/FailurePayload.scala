package uk.co.telegraph.utils.server.model



import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import uk.co.telegraph.utils.server.exceptions.ErrorCodes.{DefaultStatusMapper, UnknownErrorCode}
import uk.co.telegraph.utils.server.exceptions.{ErrorCode, ErrorCodes, ServerException}

case class Cause(
  message   :String,
  exception :Option[String] = None,
  errorCode :ErrorCode
)

case class FailurePayload(
  statusCode: StatusCode,
  cause:Cause
)

object FailurePayload{

  // Map StatusCode => ErrorCode
  private def statusToErrorCode(statusCode: StatusCode)(implicit f:PartialFunction[StatusCode, ErrorCode]) = {
    (f orElse DefaultStatusMapper).apply(statusCode)
  }

  // Map Exception => StatusCode
  private def exceptionToStatusCode:Throwable => StatusCode = {
    case ex:ServerException => ex.statusCode
    case _ => InternalServerError
  }

  // Map Exception => ErrorCode
  private def exceptionToErrorCode:Throwable => ErrorCode = {
    case ex:ServerException => ex.errorCode
    case _ => UnknownErrorCode
  }

  // The implicit partial function allow us to customize the Matching function
  def apply(statusCode:StatusCode)(implicit f:PartialFunction[StatusCode, ErrorCode] = DefaultStatusMapper ):FailurePayload =
    FailurePayload(
      statusCode  = statusCode,
      cause       = Cause(
        message   = statusCode.defaultMessage,
        errorCode = statusToErrorCode(statusCode)
      )
    )

  def apply(ex:Throwable): FailurePayload =
    FailurePayload(
      statusCode  = exceptionToStatusCode(ex),
      cause       = Cause(
        message   = ex.getMessage,
        exception = Some(ex.getClass.getSimpleName),
        errorCode = exceptionToErrorCode(ex)
      )
    )
}
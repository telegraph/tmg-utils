package uk.co.telegraph.utils.server.exceptions

trait ExceptionResult

trait NotFound
  extends ExceptionResult

trait InternalServerError
  extends ExceptionResult

trait BadRequest
  extends ExceptionResult

case object NoCause
  extends Exception
    with InternalServerError

case class MalformedConfiguration(
 msg: String,
 cause: Throwable = NoCause
) extends Exception(msg, cause)
  with InternalServerError

case class MalformedObject[T](
 str: String = "",
 obj: T = null,
 cause: Throwable = NoCause
) extends Exception(
  s"The object is incorrectly formed $str${if(obj != null) " " + obj else ""}",
  cause
) with InternalServerError

case class DataNotFound(
 str: String = "",
 cause: Throwable = NoCause
) extends Exception(
  s"The object is not found $str",
  cause
) with NotFound
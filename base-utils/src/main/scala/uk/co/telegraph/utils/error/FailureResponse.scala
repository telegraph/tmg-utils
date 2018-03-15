package uk.co.telegraph.utils.error


case class FailureResponse(
  message: String,
  error: String,
  causes: Seq[FailureCause]
)

case class FailureCause(
  message: String,
  error: String
)

object FailureResponse {

  private def toFailureCause(t: Throwable): FailureCause = {
    FailureCause(
      message = t.getMessage,
      error = t.getClass.getSimpleName
    )
  }

  def apply(t: Throwable): FailureResponse = {
    FailureResponse(
      message = t.getMessage,
      error = t.getClass.getSimpleName,
      causes = {
        Iterator.iterate(t.getCause)(_.getCause)
          .takeWhile(Option(_).nonEmpty)
          .map(toFailureCause)
          .toList
      }
    )
  }
}

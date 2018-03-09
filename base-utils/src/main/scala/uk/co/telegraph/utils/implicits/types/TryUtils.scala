package uk.co.telegraph.utils.implicits.types

trait TryUtils {
  /**
    * @param tri the [[scala.util.Try]] for operation
    */
  implicit class RichTry[T](tri: util.Try[T]) {

    /**
      * @return an Either which will be a
      *         [[Left]] if the [[scala.util.Try]] is a [[scala.util.Failure]]
      *         [[Right]] if the [[scala.util.Try]] is a [[scala.util.Success]]
      */
    def toEither(): Either[Throwable, T] = {
      if(tri.isFailure) {
        Left(tri.failed.get)
      } else {
        Right(tri.get)
      }
    }
  }
}


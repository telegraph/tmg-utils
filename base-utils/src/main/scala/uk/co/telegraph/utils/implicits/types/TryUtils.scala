package uk.co.telegraph.utils.implicits.types

import scala.util.Try

trait TryUtils {
  /**
    * @param tri the [[scala.util.Try]] for operation
    */
  implicit class RichTry[T](tri: Try[T]) {

    /**
      * @return an Either which will be a
      *         [[Left]] if the [[scala.util.Try]] is a [[scala.util.Failure]]
      *         [[Right]] if the [[scala.util.Try]] is a [[scala.util.Success]]
      */
    def toEither(): Either[Throwable, T] = {
      tri.map(Right(_)).recover { case t: Throwable => Left(t) }.get
    }
  }
}


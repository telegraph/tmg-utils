package uk.co.telegraph.utils.implicits.types
import java.util.UUID

import scala.util.{Failure, Success, Try}

trait StringUtils {
  /**
    * @param str the [[String]] for operation
    */
  implicit class RichString(str: String)  {

    /**
      * @param success a function which takes the UUID and produces a [[T]]
      * @param failure a function which will produce a [[T]] on failure of the UUID conversion
      * @tparam T the resulting type
      * @return [[T]]
      */
    def asUuid[T](success: UUID => T)(failure: Throwable => T): T = {
      Try(UUID.fromString(str)) match {
        case Success(s) => success(s)
        case Failure(t) => failure(t)
      }
    }
  }
}

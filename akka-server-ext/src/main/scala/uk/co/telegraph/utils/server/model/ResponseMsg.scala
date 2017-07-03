package uk.co.telegraph.utils.server.model

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.OK
import org.json4s.Formats
import org.json4s.Extraction.decompose
import org.json4s.jackson.JsonMethods._
import uk.co.telegraph.utils.server.serializers.{ErrorCodeSerializer, StatusCodeSerializer}

sealed trait ResponseMsg{
  type Type
  val statusCode:StatusCode
  val data:Type

  def toString()(implicit formats:Formats): String = {
    compact(render(decompose(data)(formats + StatusCodeSerializer + ErrorCodeSerializer)))
  }
}

object ResponseMsg{

  def apply(error:Throwable):ResponseMsg = {
    FailurePayload(error)
  }

  def apply[U](outgoingMsg:U, status: StatusCode = OK):ResponseMsg = {
    outgoingMsg match {
      case err:Throwable => ResponseMsg(err)
      case _             => new ResponseMsg {
        type Type      = U
        val data       = outgoingMsg
        val statusCode = status
      }
    }
  }
}
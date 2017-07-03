package uk.co.telegraph.utils.server.serializers

import akka.http.scaladsl.model.StatusCode
import org.json4s.JsonAST.JInt
import org.json4s.{Formats, JValue, MappingException, Serializer, TypeInfo}


object StatusCodeSerializer extends CustomSerializer[StatusCode] {

  val CustomClass = classOf[StatusCode]

  def unmarshall:JValue => StatusCode = {
      case JInt(statusCode) => StatusCode.int2StatusCode(statusCode.toInt)
      case other => throw new MappingException("Can't convert " + other + " to 'StatusCode'")
  }

  def marshall:StatusCode => JValue =
    status => JInt(status.intValue())
}


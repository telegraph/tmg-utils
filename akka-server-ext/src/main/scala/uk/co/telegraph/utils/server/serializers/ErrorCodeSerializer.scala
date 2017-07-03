package uk.co.telegraph.utils.server.serializers

import org.json4s.{JValue, MappingException}
import org.json4s.JsonAST.JString
import uk.co.telegraph.utils.server.exceptions.ErrorCode

object ErrorCodeSerializer extends CustomSerializer[ErrorCode]{

  override val CustomClass = classOf[ErrorCode]

  override def unmarshall: (JValue) => ErrorCode = {
    case JString(code) => ErrorCode(code)
    case other => throw new MappingException("Can't convert " + other + " to 'ErrorCode'")
  }

  override def marshall: (ErrorCode) => JString = {
    value => JString(value.code)
  }
}

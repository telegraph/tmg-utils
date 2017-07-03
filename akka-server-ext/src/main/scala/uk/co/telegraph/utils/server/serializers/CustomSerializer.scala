package uk.co.telegraph.utils.server.serializers

import org.json4s.reflect.TypeInfo
import org.json4s.{Formats, JValue, Serializer}

trait CustomSerializer[U] extends Serializer[U]{

  val CustomClass:Class[U]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), U] = {
    case (TypeInfo(CustomClass, _), json) =>
      unmarshall(json)
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case m if CustomClass.isAssignableFrom(m.getClass) =>
      marshall(m.asInstanceOf[U])
  }

  def unmarshall:JValue => U
  def marshall  :U => JValue
}

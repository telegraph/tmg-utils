package uk.co.telegraph.utils.server.serializers

import org.json4s.Extraction._
import org.json4s._

trait FieldRenameSerializer[U] extends CustomSerializer[U]{

  implicit val formats:Formats = DefaultFormats

  type Source = String
  type Target = String

  val nameMapper:Map[Source, Target]

  def unmarshallerFnc:PartialFunction[JField, JField] = {
    nameMapper.map( x => FieldSerializer.renameFrom(x._1, x._2) ).reduce(_ orElse _)
  }

  def marshallerFnc:PartialFunction[JField, JField] = {
    nameMapper.map( x => FieldSerializer.renameFrom(x._2, x._1) ).reduce(_ orElse _)
  }

  override def unmarshall:JValue => U = {
    implicit val m:Manifest[U] = Manifest.classType(CustomClass)
    json => (json transformField unmarshallerFnc).extract[U]
  }

  override def marshall:U => JValue =
    data => decompose(data) transformField marshallerFnc
}

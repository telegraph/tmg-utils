package uk.co.telegraph.utils.server.models

import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}
import uk.co.telegraph.utils.client.models.ClientDetails
import uk.co.telegraph.utils.server.models.HealthEnum.HealthType

object HealthEnum extends Enumeration {
  type HealthType = Value
  val Healthy   = Value("Healthy")
  val Unhealthy = Value("Unhealthy")
}

case class HealthDto
(
  name   : String,
  version: String,
  status : HealthType,
  cached : Boolean,
  clients: Seq[ClientDetails]
)

object HealthDto {

  implicit val Serializer:Formats = DefaultFormats + new EnumNameSerializer(HealthEnum)

  private def toServiceStatus(clients:Seq[ClientDetails]):HealthType =
    Option(HealthEnum.Healthy)
      .filter(_ => clients.forall(_.status < 400))
      .getOrElse(HealthEnum.Unhealthy)

  def apply( name:String, version:String, cached:Boolean, clients:Seq[ClientDetails] ):HealthDto = {
    HealthDto(
      name    = name,
      version = version,
      cached  = cached,
      status  = toServiceStatus(clients),
      clients = clients
    )
  }
}


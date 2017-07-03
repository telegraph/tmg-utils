package uk.co.telegraph.utils.server.model

import uk.co.telegraph.utils.client.models.ClientDetails
import uk.co.telegraph.utils.server.model.HealthEnum.HealthType

object HealthEnum extends Enumeration {
  type HealthType = Value
  val Healthy = Value("Healthy")
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

  private def toServiceStatus(clients:Seq[ClientDetails]):HealthType =
    Option(HealthEnum.Healthy)
      .filter(_ => clients.forall(_.status < 400))
      .getOrElse(HealthEnum.Unhealthy)

  def apply( name:String, version:String, cached:Boolean, clients:Seq[ClientDetails]):HealthDto = {
    HealthDto(
      name    = name,
      version = version,
      cached  = cached,
      status  = toServiceStatus(clients),
      clients = clients
    )
  }
}

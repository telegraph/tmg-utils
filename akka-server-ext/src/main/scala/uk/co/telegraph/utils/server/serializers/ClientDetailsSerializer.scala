package uk.co.telegraph.utils.server.serializers

import org.json4s.{DefaultFormats, Formats, JValue}
import org.json4s.JsonAST.JString
import uk.co.telegraph.utils.client.models.{ClientDetails, ClientFailureDetails}
import uk.co.telegraph.utils.server.model.{HealthDto, HealthEnum}
import uk.co.telegraph.utils.server.model.HealthEnum.HealthType

object ClientDetailsSerializer extends FieldRenameSerializer[ClientDetails] {
  val CustomClass = classOf[ClientDetails]

  override val nameMapper:Map[Source, Target] = Map(
    "name"          -> "name",
    "status"        -> "status",
    "date-time"     -> "date-time",
    "date-time-str" -> "date-time-str",
    "configs"       -> "configs",
    "command"       -> "command",
    "cause"         -> "cause"
  )
}

object ClientFailureDetailsSerializer extends FieldRenameSerializer[ClientFailureDetails]{
  val CustomClass = classOf[ClientFailureDetails]

  override val nameMapper:Map[Source, Target] = Map(
    "type"    -> "type",
    "message" -> "message"
  )
}

object HealthTypeSerializer extends CustomSerializer[HealthType]{
  val CustomClass = classOf[HealthType]

  override def unmarshall: (JValue) => HealthType =
    throw new UnsupportedOperationException("Not implemented")

  override def marshall: (HealthType) => JValue = {
    case HealthEnum.Healthy   => JString("healthy")
    case HealthEnum.Unhealthy => JString("unhealthy")
  }
}

object HealthDtoSerializer extends FieldRenameSerializer[HealthDto]{


  override implicit val formats:Formats =
    DefaultFormats                 +
    ClientDetailsSerializer        +
    ClientFailureDetailsSerializer +
    HealthTypeSerializer

  val CustomClass = classOf[HealthDto]

  override val nameMapper:Map[Source, Target] = Map(
    "name"    -> "name",
    "version" -> "version",
    "status"  -> "status",
    "cached"  -> "cached",
    "clients" -> "clients"
  )
}

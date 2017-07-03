package uk.co.telegraph.utils.client.models

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.typesafe.config.Config

import scala.collection.convert.WrapAsScala._

case class ClientFailureDetails( `type`:String, `message`:String )

case class ClientDetails(
  name: String,
  status: Int,
  `date-time`: Long,
  `date-time-str`: String,
  configs: Map[String, String],
  command: String,
  cause: Option[ClientFailureDetails] = None
)

object ClientDetails {

  val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx")

  private val extractConfig:Config => Map[String, String] =
    (config) => config.entrySet()
      .map   ( item => item.getKey -> config.getString(item.getKey))
      .toMap


  def apply(status: Int, config:Config):ClientDetails = {
    ClientDetails(status, config, ZonedDateTime.now())
  }

  def apply(status: Int, config:Config, cause: Throwable):ClientDetails = {
    ClientDetails(status, config, ZonedDateTime.now(), cause)
  }

  def apply(status: Int, config:Config, dateTime:ZonedDateTime):ClientDetails = {
    toClientDetails(status, config, dateTime, None)
  }

  def apply(status: Int, config:Config, dateTime:ZonedDateTime, cause: Throwable):ClientDetails = {
    toClientDetails(status, config, dateTime, Some(cause))
  }

  private def toClientDetails(status: Int, config:Config, dateTime:ZonedDateTime, causeOpt: Option[Throwable]):ClientDetails = {
    val configMap = extractConfig(config)
    val name = configMap.getOrElse("name", "unknown endpoint")
    val command = configMap.getOrElse("ping", "unknown ping command")
    val configFiltered = configMap.filter({
      case ("ping", _)|("name", _) => false
      case _ => true
    })

    ClientDetails(
      name            = name,
      status          = status,
      `date-time`     = dateTime.toEpochSecond,
      `date-time-str` = dateTime.format(dateTimeFormat),
      configs         = configFiltered,
      command         = command,
      cause           = causeOpt.map( t => ClientFailureDetails(`type` = t.getClass.getSimpleName, `message` = t.getMessage) )
    )
  }
}

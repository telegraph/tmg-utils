package uk.co.telegraph.utils.client.models

import java.time.{ZoneId, ZonedDateTime}

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.convert.WrapAsJava._
import scala.language.postfixOps

class ClientDetailsSpec extends FreeSpec with Matchers {

  import ClientDetailsSpec._

  "Given the ClientDetails Test, " - {
    "I should be able to create a ClientDetails without cause object if no exception is used " in {
      val details = ClientDetails(200, SampleConfig, SampleDate)
      details shouldBe ExpectedClientSuccess
    }

    "I should be able to create a ClientDetails with cause object if exception is used " in {
      val details = ClientDetails(404, SampleConfig, SampleDate, new RuntimeException("Sample Exception"))
      details shouldBe ExpectedClientFailure
    }

    "I should be able to create a ClientDetails just with a status and a config" in {
      val details = ClientDetails(200, SampleConfig)
      details.status  shouldBe ExpectedClientSuccess.status
      details.name    shouldBe ExpectedClientSuccess.name
      details.configs shouldBe ExpectedClientSuccess.configs
      details.command shouldBe ExpectedClientSuccess.command
    }

    "I should be able to create a ClientDetails just with a status, a config and exceptions" in {
      val details = ClientDetails(404, SampleConfig, new RuntimeException("Sample Exception"))
      details.status  shouldBe ExpectedClientFailure.status
      details.name    shouldBe ExpectedClientFailure.name
      details.configs shouldBe ExpectedClientFailure.configs
      details.command shouldBe ExpectedClientFailure.command
      details.cause   shouldBe ExpectedClientFailure.cause
    }
  }
}

object ClientDetailsSpec{
  val SampleServiceName    = "test-service"
  val SampleServiceCommand = "curl -m HEADER http://test-service.com"
  val SampleEndpoint       = "http://test-service.com"
  val SampleConfig: Config = ConfigFactory.parseMap(Map(
    "name"     -> SampleServiceName,
    "ping"     -> SampleServiceCommand,
    "endpoint" -> SampleEndpoint
  ))

  val SampleDate: ZonedDateTime = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))

  val ExpectedClientSuccess = ClientDetails(
    name            = "test-service",
    status          = 200,
    `date-time`     = SampleDate.toEpochSecond,
    `date-time-str` = "2017-01-01T00:00:00.000+00:00",
    configs         = Map(
      "endpoint" -> SampleEndpoint
    ),
    command        = "curl -m HEADER http://test-service.com"
  )

  val ExpectedClientFailure = ClientDetails(
    name            = "test-service",
    status          = 404,
    `date-time`     = SampleDate.toEpochSecond,
    `date-time-str` = "2017-01-01T00:00:00.000+00:00",
    configs = Map(
      "endpoint" -> SampleEndpoint
    ),
    command         = "curl -m HEADER http://test-service.com",
    cause           = Some(ClientFailureDetails(
      `type`        = "RuntimeException",
      message       = "Sample Exception"
    ))
  )
}

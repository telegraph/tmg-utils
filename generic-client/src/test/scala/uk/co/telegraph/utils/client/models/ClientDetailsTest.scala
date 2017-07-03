package uk.co.telegraph.utils.client.models

import java.time.{ZoneId, ZonedDateTime}

import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}

import scala.collection.convert.WrapAsJava._
import scala.language.postfixOps

class ClientDetailsTest extends FunSpec with Matchers {

  import ClientDetailsTest._

  describe("Given the ClientDetails Test, "){
    it("I should be able to create a ClientDetails without cause object if no exception is used "){
      val details = ClientDetails(200, SampleConfig, SampleDate)
      details shouldBe ExpectedClientSuccess
    }

    it("I should be able to create a ClientDetails with cause object if exception is used "){
      val details = ClientDetails(404, SampleConfig, SampleDate, new RuntimeException("Sample Exception"))
      details shouldBe ExpectedClientFailure
    }

    it("I should be able to create a ClientDetails just with a status and a config"){
      val details = ClientDetails(200, SampleConfig)
      details.status  shouldBe ExpectedClientSuccess.status
      details.name    shouldBe ExpectedClientSuccess.name
      details.configs shouldBe ExpectedClientSuccess.configs
      details.command shouldBe ExpectedClientSuccess.command
    }

    it("I should be able to create a ClientDetails just with a status, a config and exceptions"){
      val details = ClientDetails(404, SampleConfig, new RuntimeException("Sample Exception"))
      details.status  shouldBe ExpectedClientFailure.status
      details.name    shouldBe ExpectedClientFailure.name
      details.configs shouldBe ExpectedClientFailure.configs
      details.command shouldBe ExpectedClientFailure.command
      details.cause   shouldBe ExpectedClientFailure.cause
    }
  }
}

object ClientDetailsTest{
  val SampleServiceName    = "test-service"
  val SampleServiceCommand = "curl -m HEADER http://test-service.com"
  val SampleEndpoint       = "http://test-service.com"
  val SampleConfig = ConfigFactory.parseMap(Map(
    "name"     -> SampleServiceName,
    "ping"     -> SampleServiceCommand,
    "endpoint" -> SampleEndpoint
  ))

  val SampleDate = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))

  val ExpectedClientSuccess = ClientDetails(
    name = "test-service",
    status = 200,
    `date-time` = SampleDate.toEpochSecond,
    `date-time-str` = "2017-01-01T00:00:00.000+00:00",
    configs = Map(
      "endpoint" -> SampleEndpoint
    ),
    command = "curl -m HEADER http://test-service.com"
  )

  val ExpectedClientFailure = ClientDetails(
    name = "test-service",
    status = 404,
    `date-time` = SampleDate.toEpochSecond,
    `date-time-str` = "2017-01-01T00:00:00.000+00:00",
    configs = Map(
      "endpoint" -> SampleEndpoint
    ),
    command = "curl -m HEADER http://test-service.com",
    cause = Some(ClientFailureDetails(
      `type` = "RuntimeException",
      message = "Sample Exception"
    ))
  )
}

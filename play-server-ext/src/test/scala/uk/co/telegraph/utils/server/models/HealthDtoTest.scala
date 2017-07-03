package uk.co.telegraph.utils.server.models

import java.time.{ZoneId, ZonedDateTime}

import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}
import uk.co.telegraph.utils.client.models.ClientDetails
import org.json4s.jackson.Serialization._

import scala.collection.convert.WrapAsJava._
import HealthDtoTest._

import scala.io.Source

class HealthDtoTest
  extends FunSpec
  with Matchers
{

  import HealthDto.Serializer

  describe("Given the HealthDto model"){
    it("I should be able to marshal an object"){
      write(SampleHealthHealthyObj) shouldBe sampleHealthHealthyPayload
      write(SampleHealthUnhealthyObj) shouldBe sampleHealthUnhealthyPayload
    }

    it("I should be able to unmarshal an object"){
      read[HealthDto](sampleHealthHealthyPayload) shouldBe SampleHealthHealthyObj
      read[HealthDto](sampleHealthUnhealthyPayload) shouldBe SampleHealthUnhealthyObj
    }
  }
}

object HealthDtoTest{

  val SampleDateTime      = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
  val SampleClientSuccess = ClientDetails(
    status   = 200,
    config   = ConfigFactory.parseMap(Map(
      "name"    -> "client-1",
      "ping"    -> "curl -X HEAD http://teste.com/client-1",
      "baseUrl" -> "http://teste.com/client-1"
    )),
    dateTime = SampleDateTime
  )
  val SampleClientFailure = ClientDetails(
    status   = 404,
    config   = ConfigFactory.parseMap(Map(
      "name"    -> "client-2",
      "ping"    -> "curl -X HEAD http://teste.com/client-2",
      "baseUrl" -> "http://teste.com/client-2"
    )),
    dateTime = SampleDateTime,
    cause = new RuntimeException("Sample-failure")
  )

  val SampleHealthHealthyObj = HealthDto(
    name    = "test-srv",
    version = "1.0.0-SNAPSHOT",
    cached  = true,
    clients = Seq(SampleClientSuccess)
  )

  val SampleHealthUnhealthyObj = HealthDto(
    name    = "test-srv",
    version = "1.0.0-SNAPSHOT",
    cached  = false,
    clients = Seq(
      SampleClientSuccess,
      SampleClientFailure
    )
  )

  val sampleHealthHealthyPayload = Source.fromInputStream(getClass.getResourceAsStream("/payloads/health-status.json"))
    .getLines()
    .map(_.trim)
    .mkString

  val sampleHealthUnhealthyPayload = Source.fromInputStream(getClass.getResourceAsStream("/payloads/unhealth-status.json"))
    .getLines()
    .map(_.trim)
    .mkString
}

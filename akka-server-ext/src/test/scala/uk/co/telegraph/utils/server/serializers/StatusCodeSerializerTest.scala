package uk.co.telegraph.utils.server.serializers

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.OK
import org.json4s.{DefaultFormats, Formats, MappingException, jackson}
import org.scalatest.{FunSpec, Matchers}

class StatusCodeSerializerTest extends FunSpec with Matchers {

  import StatusCodeSerializerTest._

  implicit val format:Formats = DefaultFormats + StatusCodeSerializer

  describe("Given 'StatusCodeSerializer', "){
    it("I should get an error when unmarshalling an invalid statusCode"){
      val result = intercept[MappingException]{
        jackson.Serialization.read[Report](SampleReportInvalid)
      }

      result.getMessage shouldBe
        "No usable value for statusCode\nCan't convert JString(200) to 'StatusCode'"
          .stripMargin
    }

    it("I should be able to unmarshal a status Code"){
      val result = jackson.Serialization.read[Report](SampleReport)

      result shouldBe SampleReportObj
    }

    it("I should be able to marshal a statusCode"){
      val result = jackson.Serialization.write(SampleReportObj)
      result shouldBe SampleReport
    }
  }
}

object StatusCodeSerializerTest{

  case class Report(statusCode: StatusCode)

  val SampleReportInvalid = """{"statusCode":"200"}"""
  val SampleReport = """{"statusCode":200}"""

  val SampleReportObj = Report(OK)
}

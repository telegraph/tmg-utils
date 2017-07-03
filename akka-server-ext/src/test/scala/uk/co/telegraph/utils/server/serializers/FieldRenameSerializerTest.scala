package uk.co.telegraph.utils.server.serializers

import org.json4s._
import org.json4s.Extraction._
import org.json4s.jackson.JsonMethods._

import org.scalatest.{FunSpec, Matchers}
import uk.co.telegraph.utils.server.serializers.FieldRenameSerializerTest._

class FieldRenameSerializerTest extends FunSpec with Matchers {

  implicit val format:Formats = DefaultFormats + TestSerializer

  describe("Given a FieldRenameSerializer, "){

    it("I should be able to rename during serialization"){
      val result = compact(decompose(SampleLocation))

      result shouldBe SampleLocationStr
    }

    it("I should be able to rename during deserialization"){
      val result = jackson.Serialization.read[Location](SampleLocationStr)

      result shouldBe SampleLocation
    }
  }
}

object FieldRenameSerializerTest{

  case class Location(id:String, label:String, latitude:Double, longitude:Double)

  object TestSerializer extends FieldRenameSerializer[Location]{
    override val nameMapper = Map(
      "long" -> "longitude",
      "lat" -> "latitude"
    )
    override val CustomClass = classOf[Location]
  }

  val SampleLocation = Location("1234", "unknown", 24.0, 34.9)
  val SampleLocationStr = """{"id":"1234","label":"unknown","lat":24.0,"long":34.9}"""
}

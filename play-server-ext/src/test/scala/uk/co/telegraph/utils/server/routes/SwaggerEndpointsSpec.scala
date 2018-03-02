package uk.co.telegraph.utils.server.routes

import java.io.File

import org.scalatest.{FreeSpec, Matchers}
import play.mvc.Http.MimeTypes

class SwaggerEndpointsSpec
  extends FreeSpec
    with Matchers
{
  "Given the 'SwaggerEndpoints' routes, " - {
    "when retrieving the swagger file, it should be the expected one" in {
      val swaggerFile = SwaggerEndpoints.getFile()

      swaggerFile shouldBe a[Some[File]]
      swaggerFile.get.getPath shouldBe s"${SwaggerEndpoints.resourceFolder}/specs/swagger.json"
    }
    "when retrieving the mimetype of yaml file, it should be 'text/x-yaml'" in {
      val swaggerFile = new File(s"${SwaggerEndpoints.resourceFolder}/specs/swagger.yaml")

      val mimeType = SwaggerEndpoints.getMimeType(swaggerFile)

      mimeType shouldBe "text/x-yaml"
    }
    "when retrieving the mimetype of json file, it should be 'application/json'" in {
      val swaggerFile = new File(s"${SwaggerEndpoints.resourceFolder}/specs/swagger.json")

      val mimeType = SwaggerEndpoints.getMimeType(swaggerFile)

      mimeType shouldBe MimeTypes.JSON
    }
  }
}

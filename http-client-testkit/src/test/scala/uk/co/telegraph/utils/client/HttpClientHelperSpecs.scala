package uk.co.telegraph.utils.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.Config
import org.scalamock.matchers.ArgThat
import org.scalamock.scalatest.MockFactory
import uk.co.telegraph.utils.client.http.impl.HttpClient
import uk.co.telegraph.utils.client.http.impl.settings.HttpSettings
import uk.co.telegraph.utils.client.http.scaladsl.HttpContext

trait HttpClientHelperSpecs { this:MockFactory =>

  trait HttpConnector {
    def doRequest(httpRequest: HttpRequest): HttpResponse
  }

  lazy val HttpConnectorMock: HttpConnector = stub[HttpConnector]

  class HttpClientMock(configPath:String)(implicit val system: ActorSystem, val materializer: Materializer) extends HttpClient
  {
    lazy val endpointConfig: Config = system.settings.config.getConfig(configPath)
    lazy val settings = HttpSettings(system.settings.config, endpointConfig)

    override lazy val httpClientFlow: Flow[HttpRequest, HttpContext, NotUsed] = Flow[HttpRequest].map( httpRequest => {
      val httpResponse = HttpConnectorMock.doRequest(httpRequest)
      require(httpResponse != null, s"It was not possible to find a mocked request for $httpRequest")
      HttpContext(httpRequest, httpResponse)
    })
  }

  def withRequestMatching(expectedRequest:HttpRequest): ArgThat[HttpRequest] = {
    argThat[HttpRequest]( actual => {
      if( actual != expectedRequest ){
        println(s"Request sent did not match expected:" +
          s"\nExpected: $expectedRequest" +
          s"\nActual  : $actual"
        )
        false
      }else {
        true
      }
    })
  }
}

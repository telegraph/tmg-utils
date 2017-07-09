package uk.co.telegraph.utils.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.Config
import org.scalamock.scalatest.MockFactory
import uk.co.telegraph.utils.client.http.impl.HttpClient
import uk.co.telegraph.utils.client.http.impl.settings.HttpSettings
import uk.co.telegraph.utils.client.http.scaladsl.HttpContext

trait HttpClientHelper { this:MockFactory =>

  trait HttpConnector {
    def doRequest(httpRequest: HttpRequest): HttpResponse
  }

  lazy val HttpConnectorMock: HttpConnector = stub[HttpConnector]

  class HttpClientMock(configPath:String)(implicit val system: ActorSystem, val materializer: Materializer) extends HttpClient
  {
    lazy val endpointConfig: Config = system.settings.config.getConfig(configPath)
    lazy val settings = HttpSettings(endpointConfig)


    override lazy val httpClientFlow: Flow[HttpRequest, HttpContext, NotUsed] = Flow[HttpRequest].map(x =>{
      val resp = HttpConnectorMock.doRequest(x)
      assert( x != null, "Should not be null" )
      assert( resp != null, "Should not be null" )
      HttpContext(x, resp)
    })
  }
}

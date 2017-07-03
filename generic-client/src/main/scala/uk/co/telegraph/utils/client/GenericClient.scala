package uk.co.telegraph.utils.client

import uk.co.telegraph.utils.client.models.ClientDetails

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait GenericClient {
  def getDetails(implicit timeout:FiniteDuration = 5 seconds):Future[ClientDetails]
}

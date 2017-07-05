package uk.co.telegraph.utils.client

import uk.co.telegraph.utils.client.models.ClientDetails

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import GenericClient._

trait GenericClient {
  def getDetails(implicit timeout:FiniteDuration = DefaultTimeout):Future[ClientDetails]
}

object GenericClient {
  val DefaultTimeout: FiniteDuration = 5 seconds
}

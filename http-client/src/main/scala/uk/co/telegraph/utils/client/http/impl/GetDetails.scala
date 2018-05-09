package uk.co.telegraph.utils.client.http.impl

import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.models.ClientDetails
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

/**
  * A quick and easy helper, to reduce the overriding of getDetails.
  */
trait GetDetails { self: GenericClient =>

  protected val httpClient: SimpleHttpClient

  override def getDetails(implicit timeout: FiniteDuration): Future[ClientDetails] = httpClient.getDetails
}

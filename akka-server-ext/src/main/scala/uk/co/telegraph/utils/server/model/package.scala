package uk.co.telegraph.utils.server

import akka.http.scaladsl.model.StatusCodes.{BadRequest, GatewayTimeout, InternalServerError, Unauthorized}

import scala.language.implicitConversions

package object model {

  implicit def toFailureResponse( failurePayload: FailurePayload ):ResponseMsg =
    ResponseMsg(failurePayload, failurePayload.statusCode )

  val BadRequestResponse         :ResponseMsg = FailurePayload(BadRequest)
  val UnauthorizedResponse       :ResponseMsg = FailurePayload(Unauthorized)
  val GatewayTimeoutResponse     :ResponseMsg = FailurePayload(GatewayTimeout)
  val InternalServerErrorResponse:ResponseMsg = FailurePayload(InternalServerError)
}

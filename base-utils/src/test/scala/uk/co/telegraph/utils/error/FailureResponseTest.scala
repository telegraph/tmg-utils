package uk.co.telegraph.utils.error

import org.scalatest.{FreeSpec, Matchers}

class FailureResponseTest
  extends FreeSpec
    with Matchers {

  "FailureResponse" - {

    "construct from a [[Throwable]] with no causes" in {
      case class A() extends Throwable
      val t = A()
      val actual = FailureResponse.apply(t)
      val expected = FailureResponse(
        message = t.getMessage,
        error = t.getClass.getSimpleName,
        causes = List.empty
      )

      actual shouldBe expected
    }

    "construct from a [[Throwable]] with one cause" in {
      val msg = "msg"
      val causeMsg = "causeMsg"
      val cause = new Exception(causeMsg)
      case class A() extends Exception(msg, cause)
      val t = A()
      val actual = FailureResponse.apply(t)
      val expected = FailureResponse(
        message = t.getMessage,
        error = t.getClass.getSimpleName,
        causes = List(
          FailureCause(
            message = cause.getMessage,
            error = cause.getClass.getSimpleName
          )
        )
      )

      actual shouldBe expected
    }

    "construct from a [[Throwable]] with multiple causes" in {
      val msg = "msg"
      val causeMsg2 = "causeMsg2"
      val cause2 = new Exception(causeMsg2)
      val causeMsg1 = "causeMsg"
      val cause1 = new Exception(causeMsg1, cause2)
      case class A() extends Exception(msg, cause1)
      val t = A()
      val actual = FailureResponse.apply(t)
      val expected = FailureResponse(
        message = t.getMessage,
        error = t.getClass.getSimpleName,
        causes = List(
          FailureCause(
            message = cause1.getMessage,
            error = cause1.getClass.getSimpleName
          ),
          FailureCause(
            message = cause2.getMessage,
            error = cause2.getClass.getSimpleName
          )
        )
      )

      actual shouldBe expected
    }
  }
}

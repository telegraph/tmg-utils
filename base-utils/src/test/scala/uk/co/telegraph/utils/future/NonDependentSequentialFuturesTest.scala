package uk.co.telegraph.utils.future

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class NonDependentSequentialFuturesTest
  extends FreeSpec
    with Matchers
    with ScalaFutures
    with NonDependentSequentialFutures {

  "NonDependentSequentialFutures" - {

    "Not fail when one or more of the Futures fail, but reserve results" in {
      val happy1 = "happy 1"
      val happy2 = "happy 2"
      val happy3 = "happy 3"
      val happy4 = "happy 4"
      val ohnoes1 = new RuntimeException("oh noes 1")

      val input = Seq(
        Future.successful(happy1),
        Future.failed(ohnoes1),
        Future.successful(happy2),
        Future.successful(happy3),
        Future.successful(happy4)
      )
      val expected = Seq(
        Right(happy1),
        Left(ohnoes1),
        Right(happy2),
        Right(happy3),
        Right(happy4)
      )

      whenReady(nonDependentSequentialFutures(input))(_ shouldBe expected)
    }

    "Not fail when no Futures fail, reserve results" in {
      val happy1 = "happy 1"
      val happy2 = "happy 2"
      val happy3 = "happy 3"
      val happy4 = "happy 4"

      val input = Seq(
        Future.successful {
          Thread.sleep(110)
          println("this should print first")
          happy1
        },
        Future.successful {
          Thread.sleep(220)
          println("this should print second")
          happy2
        },
        Future.successful {
          Thread.sleep(330)
          println("this should print third")
          happy3
        },
        Future.successful {
          Thread.sleep(440)
          println("this should print forth")
          happy4
        }
      )
      val expected = Seq(
        Right(happy1),
        Right(happy2),
        Right(happy3),
        Right(happy4)
      )

      whenReady(nonDependentSequentialFutures(input))(_ shouldBe expected)
    }
  }
}

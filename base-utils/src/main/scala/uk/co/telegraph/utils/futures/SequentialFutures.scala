package uk.co.telegraph.utils.futures

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait SequentialFutures {
  implicit val executionContext: ExecutionContext

  def sequentialFutures[A, B](items: Seq[A], limit: Int)(fn: Seq[A] => Future[B]): Future[List[B]] = {
    val base = Future.successful(mutable.ListBuffer.empty[B])
    items
      .grouped(limit)
      .foldLeft(base) {
        (futureRes, group) =>
          futureRes.flatMap {
            res => {
              fn(group).map(y => res += y)
            }
          }
      }
      .map(_.toList)
  }

}

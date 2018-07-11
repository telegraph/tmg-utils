package uk.co.telegraph.utils.future

import scala.concurrent.{ExecutionContext, Future}


trait NonDependentSequentialFutures {

  def nonDependentSequentialFutures[T](
    toProcess: Iterable[Future[T]],
    processed: Future[Iterable[Either[Throwable, T]]] = Future.successful(Nil)
  )(
    implicit ec: ExecutionContext
  ): Future[Iterable[Either[Throwable, T]]] = {
    if (toProcess.isEmpty) {
      processed
    } else {
      val futureToProcess = toProcess.head
      val newlyProcessed = futureToProcess.map(Right.apply).recover { case t: Throwable => Left(t) }
      nonDependentSequentialFutures(
        toProcess = toProcess.tail,
        processed = processed.flatMap { r: Iterable[Either[Throwable, T]] =>
          newlyProcessed.map(x => r ++ Iterable(x))
        }
      )
    }
  }
}

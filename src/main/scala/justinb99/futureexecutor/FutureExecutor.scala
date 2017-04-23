package justinb99.futureexecutor

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executor, Executors, ThreadPoolExecutor}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by justin on 4/20/17.
  */
case class FutureExecutorStats(numberOfQueuedFutures: Int,
                               numberOfExecutingFutures: Int)

trait FutureExecutor {

  protected val executor: Executor
  lazy protected implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(executor)

  private[futureexecutor] val numberOfQueuedFutures = new AtomicInteger()
  private[futureexecutor] val numberOfExecutingFutures = new AtomicInteger()

  def stats: FutureExecutorStats = {
    FutureExecutorStats(
      numberOfQueuedFutures = numberOfQueuedFutures.get,
      numberOfExecutingFutures = numberOfExecutingFutures.get
    )
  }

  def future[T](futureBody: => T): Future[T] = {
    numberOfQueuedFutures.incrementAndGet()
    Future {
      executeFutureBody(0, { _: Int => futureBody})
    }
  }

  def map[T, U](futureToMap: Future[T])(futureBody: T => U): Future[U] = {
    numberOfQueuedFutures.incrementAndGet()
    futureToMap.map { valueToMap =>
      executeFutureBody(valueToMap, futureBody)
    }
  }

  protected def executeFutureBody[T, U](arg: T, futureBody: T => U): U = {
    numberOfQueuedFutures.decrementAndGet()
    numberOfExecutingFutures.incrementAndGet()
    val result = futureBody(arg)
    numberOfExecutingFutures.decrementAndGet()
    result
  }

}

trait FixedThreadPool {
  self: FutureExecutor =>

  val numberOfThreads: Int
  lazy protected val executor = Executors.newFixedThreadPool(numberOfThreads)

}

object FutureExecutor {

  def apply(nThreads: Int): FutureExecutor = {
    new FutureExecutor with FixedThreadPool {
      override val numberOfThreads = nThreads
    }
  }
}

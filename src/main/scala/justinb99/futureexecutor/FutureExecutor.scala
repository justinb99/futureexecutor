package justinb99.futureexecutor

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executor, Executors, ThreadPoolExecutor}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by justin on 4/20/17.
  */
case class FutureExecutorStats(numberOfQueuedFutures: Int,
                               numberOfExecutingFutures: Int)

trait FutureExecutor {

  private [futureexecutor] val executor: Executor
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

object FutureExecutor {

  def apply(nThreads: Int): FutureExecutor = {
    ForkJoinFutureExecutor(nThreads)
  }
}

trait ForkJoinThreadPool {
  self: FutureExecutor =>

  val numberOfThreads: Int
  private [futureexecutor] lazy val executor = new ForkJoinPool(numberOfThreads)
}

object ForkJoinFutureExecutor {

  def apply(nThreads: Int): FutureExecutor = {
    new FutureExecutor with ForkJoinThreadPool {
      val numberOfThreads = nThreads
    }
  }

}

trait FixedThreadPool {
  self: FutureExecutor =>

  val numberOfThreads: Int
  private [futureexecutor] lazy val executor = Executors.newFixedThreadPool(numberOfThreads)

}

object FixedFutureExecutor {

  def apply(nThreads: Int): FutureExecutor = {
    new FutureExecutor with FixedThreadPool {
      override val numberOfThreads = nThreads
    }
  }

}


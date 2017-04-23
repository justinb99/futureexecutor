package justinb99.futureexecutor

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by justin on 4/20/17.
  */
trait FutureExecutor {

  private [futureexecutor] val executorService: ExecutorService
  lazy protected implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

  private[futureexecutor] val numberOfQueuedFutures = new AtomicInteger()
  private[futureexecutor] val numberOfExecutingFutures = new AtomicInteger()
  private[futureexecutor] val executionTimeMillis = new AtomicLong()
  private[futureexecutor] val numberOfCompletedFutures = new AtomicInteger()

  def stats: FutureExecutorStats = {
    FutureExecutorStats(
      numberOfQueuedFutures = numberOfQueuedFutures.get,
      numberOfExecutingFutures = numberOfExecutingFutures.get,
      executionTimeMillis = executionTimeMillis.get,
      numberOfCompletedFutures = numberOfCompletedFutures.get
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

    val startMillis = System.currentTimeMillis()
    val result = futureBody(arg)
    val endMillis = System.currentTimeMillis()
    executionTimeMillis.addAndGet(endMillis - startMillis)

    numberOfExecutingFutures.decrementAndGet()
    numberOfCompletedFutures.incrementAndGet()

    result
  }

  def shutdown(): Unit = {
    executorService.shutdown()
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
  private [futureexecutor] lazy val executorService = new ForkJoinPool(numberOfThreads)

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
  private [futureexecutor] lazy val executorService = Executors.newFixedThreadPool(numberOfThreads)

}

object FixedFutureExecutor {

  def apply(nThreads: Int): FutureExecutor = {
    new FutureExecutor with FixedThreadPool {
      override val numberOfThreads = nThreads
    }
  }

}


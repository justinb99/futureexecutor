package justinb99.futureexecutor

import java.util.concurrent.{ExecutorService, ThreadPoolExecutor}

import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{Await, Future}

/**
  * Created by justin on 4/20/17.
  */
class FutureExecutorTest extends FlatSpec with Matchers {

  "FutureExecutor" should "construct with a Fork-Join thread pool" in {
    val futureExecutor = ForkJoinFutureExecutor(1)
    futureExecutor shouldBe a[ForkJoinThreadPool]

    val forkJoinThreadPool = futureExecutor.asInstanceOf[ForkJoinThreadPool]
    forkJoinThreadPool.numberOfThreads shouldBe 1
    forkJoinThreadPool.executorService shouldBe a[ForkJoinPool]
  }

  it should "construct with a Fork-Join thread pool by default" in {
    val futureExecutor = FutureExecutor(1)
    futureExecutor shouldBe a[ForkJoinThreadPool]
  }

  it should "construct with a Fixed thread pool" in {
    val futureExecutor = FixedFutureExecutor(1)
    futureExecutor shouldBe a[FixedThreadPool]

    val fixedThreadPool = futureExecutor.asInstanceOf[FixedThreadPool]
    fixedThreadPool.numberOfThreads shouldBe 1
    fixedThreadPool.executorService shouldBe a[ThreadPoolExecutor]
  }

  it should "create and run a future" in {
    val expectedValue = 55
    val futureExecutor = FutureExecutor(1)

    val futureValue = futureExecutor.future {
      Thread.sleep(100)
      expectedValue
    }

    val result = Await.result(futureValue, 1 second)
    result shouldBe expectedValue
  }

  it should "map a future" in {
    val futureExecutor = FutureExecutor(1)

    val futureValue = futureExecutor.future {
      Thread.sleep(100)
      55
    }

    val mappedFutureValue = futureExecutor.map(futureValue) { intValue =>
      intValue.toString
    }

    val result = Await.result(mappedFutureValue, 1 second)
    result shouldBe "55"
  }

  def testExecutingFutureTracked[T](futureExecutor: FutureExecutor, executingFuture: Future[T]): T = {
    //Wait for the future to start
    Thread.sleep(100)

    futureExecutor.numberOfQueuedFutures.get shouldBe 0
    futureExecutor.numberOfExecutingFutures.get shouldBe 1

    val result = Await.result(executingFuture, 1 second)

    futureExecutor.numberOfExecutingFutures.get shouldBe 0

    result
  }

  it should "track executing futures" in {
    val futureExecutor = new FutureExecutor with FixedThreadPool {
      val numberOfThreads = 1
    }

    val directFuture = futureExecutor.future {
      Thread.sleep(500)
      1
    }

    testExecutingFutureTracked(futureExecutor, directFuture)

    val mappedFuture = futureExecutor.map(directFuture) { value =>
      Thread.sleep(500)
      value + 1
    }

    val mappedResult = testExecutingFutureTracked(futureExecutor, mappedFuture)

    mappedResult shouldBe 2
  }

  it should "track queued futures" in {
    val futureExecutor = new FutureExecutor with FixedThreadPool {
      val numberOfThreads = 1
    }

    val longFuture = futureExecutor.future {
      Thread.sleep(500)
      1
    }

    Thread.sleep(100)
    futureExecutor.numberOfExecutingFutures.get shouldBe 1
    futureExecutor.numberOfQueuedFutures.get shouldBe 0

    val queuedFuture1 = futureExecutor.future {
      2
    }

    futureExecutor.numberOfQueuedFutures.get shouldBe 1

    val queuedFuture2 = futureExecutor.map(queuedFuture1) { value =>
      3
    }

    futureExecutor.numberOfQueuedFutures.get shouldBe 2

    Await.result(queuedFuture2, 1 second) shouldBe 3

    futureExecutor.numberOfQueuedFutures.get shouldBe 0
    futureExecutor.numberOfExecutingFutures.get shouldBe 0
  }

  it should "return stats" in {
    val futureExecutor = FutureExecutor(1)

    futureExecutor.numberOfExecutingFutures.set(1)
    futureExecutor.numberOfQueuedFutures.set(2)
    futureExecutor.executionTimeMillis.set(3)
    futureExecutor.numberOfCompletedFutures.set(4)
    futureExecutor.numberOfFailedFutures.set(5)

    val stats = futureExecutor.stats
    stats.numberOfExecutingFutures shouldBe 1
    stats.numberOfQueuedFutures shouldBe 2
    stats.executionTimeMillis shouldBe 3l
    stats.numberOfCompletedFutures shouldBe 4
    stats.numberOfFailedFutures shouldBe 5
  }

  it should "shutdown the ExecutorService" in {
    val mockExecutorService = MockitoSugar.mock[ExecutorService]
    val futureExecutor = new FutureExecutor {
      override private[futureexecutor] val executorService = mockExecutorService
    }

    futureExecutor.shutdown()

    verify(mockExecutorService).shutdown()
  }

  it should "time Executions and track completed futures" in {
    val futureExecutor = FutureExecutor(1)
    val future1 = futureExecutor future {
      Thread.sleep(300)
      1
    }

    Await.result(future1, 1 second)

    futureExecutor.executionTimeMillis.get shouldBe >=(300l)
    futureExecutor.numberOfCompletedFutures.get shouldBe 1

    val future2 = futureExecutor.map(future1) { val1 =>
      Thread.sleep(500)
      2
    }

    Await.result(future2, 1 second)

    futureExecutor.executionTimeMillis.get shouldBe >=(800l)
    futureExecutor.numberOfCompletedFutures.get shouldBe 2
  }

  it should "track stats when an exception occurs" in {
    class FutureExecutorTestException extends RuntimeException
    val futureExecutor = FutureExecutor(1)

    val futureException = futureExecutor future {
      Thread.sleep(300)
      throw new FutureExecutorTestException
    }

    Await.ready(futureException, 1 second)

    futureException.value.get.failed.get shouldBe a [FutureExecutorTestException]

    futureExecutor.executionTimeMillis.get shouldBe >=(300l)
    futureExecutor.numberOfCompletedFutures.get shouldBe 0
    futureExecutor.numberOfFailedFutures.get shouldBe 1
    futureExecutor.numberOfExecutingFutures.get shouldBe 0
  }

}
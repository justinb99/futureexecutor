package justinb99.futureexecutor

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by justin on 4/20/17.
  */
class FutureExecutorTest extends FlatSpec with Matchers {

  "FutureExecutor" should "create and run a future" in {
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

    val stats = futureExecutor.stats
    stats.numberOfExecutingFutures shouldBe 1
    stats.numberOfQueuedFutures shouldBe 2
  }
}
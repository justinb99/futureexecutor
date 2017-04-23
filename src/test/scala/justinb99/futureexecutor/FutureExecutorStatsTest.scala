package justinb99.futureexecutor

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by justin on 4/23/17.
  */
class FutureExecutorStatsTest extends FlatSpec with Matchers {

  val testStats = FutureExecutorStats(
    numberOfQueuedFutures = 100,
    numberOfExecutingFutures = 4,
    executionTimeMillis = 326238l, //5 min, 26 sec, 238 ms
    numberOfCompletedFutures = 10
  )

  "FutureExecutorStats" should "return a friendly execution time" in {
    testStats.executionTime shouldBe "5 minutes, 26 seconds and 238 milliseconds"
  }

  it should "calculate average execution time" in {
    testStats.averageExecutionTimeMillis shouldBe 32623l
    testStats.averageExecutionTime shouldBe "32 seconds and 623 milliseconds"
  }

}

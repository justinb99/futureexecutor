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
    numberOfCompletedFutures = 10,
    numberOfFailedFutures = 11
  )

  val expectedExecutionTime = "5 minutes, 26 seconds and 238 milliseconds"

  "FutureExecutorStats" should "return a friendly execution time" in {
    testStats.executionTime shouldBe expectedExecutionTime
  }

  val expectedAvgTime = "32 seconds and 623 milliseconds"

  it should "calculate average execution time" in {
    testStats.averageExecutionTimeMillis shouldBe 32623l
    testStats.averageExecutionTime shouldBe expectedAvgTime
  }

  it should "convert to a JSON string" in {
    val formatted = testStats.toString
    val expectedFormatted =
      s"""|{
          |  "numberOfQueuedFutures": 100,
          |  "numberOfExecutingFutures": 4,
          |  "executionTimeMillis": 326238,
          |  "numberOfCompletedFutures": 10,
          |  "numberOfFailedFutures": 11,
          |  "executionTime": "$expectedExecutionTime",
          |  "averageExecutionTimeMillis": 32623,
          |  "averageExecutionTime": "$expectedAvgTime"
          |}""".stripMargin
    formatted shouldBe expectedFormatted
  }

}

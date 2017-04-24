package justinb99.futureexecutor

import org.joda.time.Duration
import org.joda.time.format.PeriodFormat

/**
  * Created by justin on 4/20/17.
  */
case class FutureExecutorStats(numberOfQueuedFutures: Int,
                               numberOfExecutingFutures: Int,
                               executionTimeMillis: Long,
                               numberOfCompletedFutures: Int,
                               numberOfFailedFutures: Int)
{
  val executionTime: String = FormattedDuration(executionTimeMillis)

  val averageExecutionTimeMillis = executionTimeMillis / numberOfCompletedFutures.toLong
  val averageExecutionTime: String = FormattedDuration(averageExecutionTimeMillis)

  //TODO: convert this to use json4s
  override def toString: String = {
    s"""|{
        |  "numberOfQueuedFutures": $numberOfQueuedFutures,
        |  "numberOfExecutingFutures": $numberOfExecutingFutures,
        |  "executionTimeMillis": $executionTimeMillis,
        |  "numberOfCompletedFutures": $numberOfCompletedFutures,
        |  "numberOfFailedFutures": $numberOfFailedFutures,
        |  "executionTime": "$executionTime",
        |  "averageExecutionTimeMillis": $averageExecutionTimeMillis,
        |  "averageExecutionTime": "$averageExecutionTime"
        |}""".stripMargin
  }
}

object FormattedDuration {

  def apply(millis: Long): String = {
    PeriodFormat.getDefault.print(new Duration(millis).toPeriod())
  }

}

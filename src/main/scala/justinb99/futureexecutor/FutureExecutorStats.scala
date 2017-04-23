package justinb99.futureexecutor

import org.joda.time.Duration
import org.joda.time.format.PeriodFormat

/**
  * Created by justin on 4/20/17.
  */
case class FutureExecutorStats(numberOfQueuedFutures: Int,
                               numberOfExecutingFutures: Int,
                               executionTimeMillis: Long,
                               numberOfCompletedFutures: Int)
{
  val executionTime: String = FormattedDuration(executionTimeMillis)

  val averageExecutionTimeMillis = executionTimeMillis / numberOfCompletedFutures.toLong
  val averageExecutionTime: String = FormattedDuration(averageExecutionTimeMillis)
}

object FormattedDuration {

  def apply(millis: Long): String = {
    PeriodFormat.getDefault.print(new Duration(millis).toPeriod())
  }

}

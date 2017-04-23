package justinb99.futureexecutor

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by justin on 4/23/17.
  */
object SampleApp {

  val numbersCalculatedCount = new AtomicInteger

  val numberOfThreads = 4
  val numbersToCompute = (1 to 50000000)
  val chunkSize = numbersToCompute.size / numberOfThreads

  def main(args: Array[String]): Unit = {

    val futureExecutor = FutureExecutor(numberOfThreads)

    try {

      println(s"Calculating the square root of ${numbersToCompute.size} numbers in $numberOfThreads threads, $chunkSize numbers per thread.")

      val chunksToProcess = numbersToCompute.grouped(chunkSize)

      val futures = chunksToProcess.map { chunk =>
        futureExecutor.future {
          calculateSquareRoots(chunk)
        }
      }

      futures.foreach { future =>
        Await.result(future, Duration.Inf)
      }

      val stats = futureExecutor.stats

      println(s"All computations complete:\n$stats")

    } finally {

      futureExecutor.shutdown()
    }

  }

  def calculateSquareRoots(numbers: Seq[Int]): Seq[Double] = {
    numbers.map { number =>
      val currentCount = numbersCalculatedCount.incrementAndGet()
      if (currentCount % 100000 == 0)
        println(s"Calculating number $currentCount of ${numbersToCompute.size}")

      Math.sqrt(number)
    }
  }

}

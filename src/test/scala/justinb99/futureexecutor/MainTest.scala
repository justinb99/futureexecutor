package justinb99.futureexecutor

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by justin on 4/19/17.
  */
class MainTest extends FlatSpec with Matchers {

  "FutureExecutor" should "return a constant" in {
    Main.five shouldBe 5
  }

}

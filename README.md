# Future Executor
Easily execute Scala Futures with metrics

## Usage
Create a Future Executor, specifying the number of threads
```scala
val futureExecutor = FutureExecutor(4)
```

Execute some async code and get a future back
```scala
val futureInt: Future[Int] = futureExecutor future {
  val result = 1234 //Some complex operation
  result
}
```

Map the results of a future
```scala
val futureString: Future[String] = futureExecutor.map(futureInt) { value =>
    value.toString
}
```

Get the execution metrics
```scala
val stats: FutureExecutorStats = futureExecutor.stats
```

See the "SampleApp" for more examples (or run ./SampleApp.sh)

## Execution Models
By default, a Fork-Join Thread Pool is used
```scala
val futureExecutor = FutureExecutor(4)
```
and
```scala
val futureExecutor = ForkJoinFutureExecutor(4)
```
are equivalent.

A Fixed Thread Pool can also be used
```scala
val futureExecutor = FixedFutureExecutor(4)
```

The flexible execution models are accomplished via mixins and can be worked with directly
```scala
val futureExecutor = new FutureExecutor with FixedThreadPool {
  override val numberOfThreads = 100
}
```

## Compilation
This project is built with Maven.  SBT support will be added in a future release.
```
mvn clean install
```
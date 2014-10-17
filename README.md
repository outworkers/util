# util[![Build Status](https://travis-ci.org/websudos/util.svg?branch=develop)](https://travis-ci.org/websudos/util)

The latest available version of the util library is ```val UtilVersion = 0.2.4```. This library is only deployed to our managed Maven repository, 
available at ```http://maven.websudos.co.uk```. It is publicly available.


### Integrating the util library

The full list of available modules is:

```scala

libraryDependencies ++= Seq(
  "com.websudos" %% "util-aws" % UtilVersion,
  "com.websudos" %% "util-core" % UtilVersion,
  "com.websudos" %% "util-http" % UtilVersion,
  "com.websudos" %% "util-lift" % UtilVersion,
  "com.websudos" %% "util-parsers" % UtilVersion,
  "com.websudos" %% "util-testing" % UtilVersion,
  "com.websudos" %% "util-zookeeper" % UtilVersion
)
```

The util library is designed to wrap common functionality in all our frameworks and offer it at the convenience of a dependency. Anything that will be useful
 long term to a great number of people belongs in these modules, to avoid duplication and help make our devs aware they can simply use what already exists.
 
 
 
 


### util-testing ###

The testing module features the ```AsyncAssertionsHelper```, which builds on top of ScalaTest to offer simple asynchronous assertions. We use this pattern 
heavily throughout the Websudos ecosystem of projects, from internal to DSL modules and so forth. Asynchronous testing generally offers a considerable 
performance gain in code.

The async assertions module features a dual API, so you can call the same methods on both ```scala.concurrent.Future``` and ```com.twitter.util.Future```. 
The underlying mechanism will create an async ```Waiter```, that will wait for the future to complete within the given ```PatienceConfiguration```. The 
awaiting is done asynchronously and the assertions are invoked and evaluated once the future in question has returned a result.

```scala
import com.websudos.util.testing.AsyncAssertionsHelper._

class MyTests extends FlatSuite with Matchers {

  "The async computation" should "return 0 on completion" in {
    val f: Future[Int] = .. // Pretend this is a Future just like any other future.
    f.successful {
      res => {
        res shouldEqual 0
      }
    }
  }
  
  "This async computation" should "fail by design" in {
    val f: Future[Unit] = ..
    
    // You don't even need to do anything more than failure at this stage.
    // If the Future fails, the test will succeed, as this method is used when you "expect a failure".
    // You can however perform assertions on the error returned.
    f.failing {
      err => {
      }
    }
  }
  
  "This async computation" should "fail with a specific error" in {
      val f: Future[Unit] = ..
      f.failingWith[NumberFormatException] {
        err => {
        }
      }
    }

}
```

You can directly customise the ```timeout``` of all ```Waiters``` using the ScalaTest specific time span implementations and interval configurations.


```scala
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

implicit val timeout: PatienceConfiguration.Timeout = timeout(20 seconds)

```

Summary:

- The dependency you need is ```"com.websudos" %% "util-testing" % UtilVersion```.
- You have to import ```com.websudos.util.testing.AsyncAssertionsHelper._```.
- You have three main assertion methods, ```successful```, ```failing```, and ```failingWith```.
- You can configure the timeout of waiters with ```implicit val timeout: PatienceConfiguration.Timeout = timeout(20 seconds)```.
- The default timeout value is ```1 second```.

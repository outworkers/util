# util[![Build Status](https://travis-ci.org/websudos/util.svg?branch=develop)](https://travis-ci.org/websudos/util)

The latest available version of the util library is ```val UtilVersion = 0.3.12```. This library is only deployed to our managed Maven repository,
available at ```http://maven.websudos.co.uk```. It is publicly available.

 
![Util](http://websudos.com/images/work/util.png "Websudos Util")
 
### Table of contents ###

<ol>
  <li><a href="#integrating-the-util-library">Integrating the util library</a></li>
  <li>
    <p><a href="#util-testing">util-testing</a></p>
    <ul>
      <li><a href="#asynchronous-assertions">Asynchronous assertions</a></li>
    </ul>
  </li>
  <li>
    <p><a href="#util-parsers">util-parsers</a></p>
    <ul>
      <li><a href="#option-parsers">Option parsers</a></li>
      <li><a href="#applicative-parsers">Applicative parsers</a></li>
    </ul>
  </li>
  <li><a href="#contributors">Copyright</a></li>
  <li><a href="#style-guidelines">Scala Style Guidelines</a></li>
  <li><a href="#git-flow">Git Flow</a></li>
  <li><a href="#contributing">Contributing</a></li>
  <li><a href="#copyright">Copyright</a></li>
</ol>


### Integrating the util library ###
<a href="#table-of-contents">Back to top</a>


The util library is designed to wrap common functionality in all our frameworks and offer it at the convenience of a dependency. Anything that will be useful
 long term to a great number of people belongs in these modules, to avoid duplication and help make our devs aware they can simply use what already exists.

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

### util-testing ###
<a href="#table-of-contents>Back to top</a>

The testing module features the ```AsyncAssertionsHelper```, which builds on top of ScalaTest to offer simple asynchronous assertions. We use this pattern 
heavily throughout the Websudos ecosystem of projects, from internal to DSL modules and so forth. Asynchronous testing generally offers a considerable 
performance gain in code.


### Asynchronous assertions ###
<a href="#table-of-contents>Back to top</a>


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


### util-parsers ###
<a href="#table-of-contents">Back to top</a>

The parser module features an easy to use and integrate set of ScalaZ Applicative based parsers, with an ```Option``` based parser variant. It allows us to 
seamlessly deal with validation chains at REST API level or whenever validation is involved. Whether it's monadic composition of options or chaining of 
applicative functors to obtain a "correct" chain, the parser module is designed to offer an all-you-can-eat buffet of mini parsers that can be easily 
composed to suit any validation needs. 

Each parser comes in three distinct flavours, a ```ValidationNel``` parser that parsers the end type from a ```String``` and returns the type itself, 
a parser that parses an end result from an ```Option[String]``` and parserOpt variant that returns an ```Option[T]``` instead of a ```ValidationNel[String, 
T]```, which allows for Monadic composition, where you need to "short-circuit" evaluation and validation, instead of computing the full chain by chaining 
applicatives.

To illustrate the above, the ```int``` parser, designed to parse a ```scala.Int``` value from a ```String```, packages the following three signatures:

```scala
  final def intOpt(str: String)

  final def int(str: String): ValidationNel[String, Int]

  final def int(str: Option[String]): ValidationNel[String, Int]
```

The full list of available parsers is:


| Name            | Input type                | Output type                       |
| --------------- |---------------------------| --------------------------------- |
| int             | String|Option[String]     | ValidationNel[String, Int]        |
| intOpt          | String|Option[String]     | Option[Int]                       |
| long            | String|Option[String]     | ValidationNel[String, Long]       |
| longOpt         | String|Option[String]     | Option[Long]                      |
| double          | String|Option[String]     | ValidationNel[String, Double]     |
| doubleOpt       | String|Option[String]     | Option[Double]                    |
| float           | String|Option[String]     | ValidationNel[String, Float]      |
| floatOpt        | String|Option[String]     | Option[Float]                     |
| uuid            | String|Option[String]     | ValidationNel[String, UUID]       |
| uuidOpt         | String|Option[String]     | Option[java.util.UUID]            |
| email           | String|Option[String]     | ValidationNel[String, String]     |
| emailOpt        | String|Option[String]     | Option[String]                    |
| timestamp       | String|Option[String]     | ValidationNel[String, org.joda.time.DateTime]   |
| timestampOpt    | String|Option[String]     | Option[org.joda.time.DateTime]                  |
| date            | String|Option[String]     | ValidationNel[String, org.joda.time.DateTime]   |
| dateOpt         | String|Option[String]     | Option[org.joda.time.DateTime]                  |



### Option parsers ###
<a href="#table-of-contents">Back to top</a>

Option parsers are designed for chains where you want to short-circuit and exit to result as soon a parser fails. This short-circuit behaviour is the default
 ```flatMap``` behaviour of an ```Option```, as soon as an ```Option``` is ```None``` the chain breaks. Unlike applicatives, 
 the evaluation sequence of options will be escaped and you cannot for instance return an error for every parser that couldn't validate. Instead, 
 you will only get the first error in the sequence.
 
An example of how to use ```Option``` parsers might be:

```scala

import com.websudos.util.parsers._

object Test {
  def optionalParsing(email: String, age: String): Unit = {
    for {
      validEmail <- emailOpt(email)
      validAge <- intOpt(age)
    } yield s"This person can be reached at $validEmail and is $validAge years old"
  }
}

```


### Applicative parsers ###
<a href="#table-of-contents">Back to top</a>

To illustrate the basic usage of applicative parsers and how to chain them, have a look below.

```scala

import scalaz._
import scalaz.Scalaz._
import com.websudos.util.parsers._

object Test {
  
  def registerUser(str: String, age: String): Unit = {
    (email(str) |@| int(age)) {
      (validEmail, validAge) => {
      }
    }.fold {
      // .. 
    }
  }
  
}
```


### Contributors
<a href="#table-of-contents">Back to top</a>

- Flavian Alexandru @alexflav23
- Jens Halm @jenshalm
- Bartosz Jankiewicz @bjankie1


<a id="copyright">Copyright</a>
===============================
<a href="#table-of-contents">Back to top</a>

Copyright (c) 2014 websudos.


<a id="contributing">Contributing to util</a>
==============================================
<a href="#table-of-contents">Back to top</a>

Contributions are most welcome! Don't forget to add your name and GitHub handle to the list of contributors.


<a id="git-flow">Using GitFlow</a>
==================================
<a href="#table-of-contents">Back to top</a>

To contribute, simply submit a "Pull request" via GitHub.

We use GitFlow as a branching model and SemVer for versioning.

- When you submit a "Pull request" we require all changes to be squashed.
- We never merge more than one commit at a time. All the n commits on your feature branch must be squashed.
- We won't look at the pull request until Travis CI says the tests pass, make sure tests go well.

<a id="style-guidelines">Scala Style Guidelines</a>
===================================================
<a href="#table-of-contents">Back to top</a>

In spirit, we follow the [Twitter Scala Style Guidelines](http://twitter.github.io/effectivescala/).
We will reject your pull request if it doesn't meet code standards, but we'll happily give you a hand to get it right. Morpheus is even using ScalaStyle to 
build, which means your build will also fail if your code doesn't comply with the style rules.

Some of the things that will make us seriously frown:

- Blocking when you don't have to. It just makes our eyes hurt when we see useless blocking.
- Testing should be thread safe and fully async, use ```ParallelTestExecution``` if you want to show off.
- Writing tests should use the pre-existing tools.
- Use the common patterns you already see here, we've done a lot of work to make it easy.
- Don't randomly import stuff. We are very big on alphabetized clean imports.
- Morpheus uses ScalaStyle during Travis CI runs to guarantee you are complying with our guidelines. Since breaking the rules will result in a failed build, 
please take the time to read through the guidelines beforehand.



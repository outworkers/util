# util[![Build Status](https://travis-ci.org/outworkers/util.svg?branch=develop)](https://travis-ci.org/outworkers/util) [![Coverage Status](https://coveralls.io/repos/github/outworkers/util/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/util?branch=develop) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/util-lift_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/util-lift_2.11)   [ ![Bintray](https://api.bintray.com/packages/websudos/oss-releases/util/images/download.svg) ](https://bintray.com/websudos/oss-releases/util/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/util_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/util_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/util?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

This library is available on Maven Central and on our public Bintray repository, found at: `https://dl.bintray.com/websudos/oss-releases/`.

It is publicly available, for both Scala 2.10.x and Scala 2.11.x. Check the badges at the top of this README for the
latest version of `util` available. The badges are automatically updated when a new version is out, this readme is not.

![Util](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/util.png "Outworkers Util")
 
### Table of contents ###

<ol>
  <li><a href="#integrating-the-util-library">Integrating the util library</a></li>
  
  <li>
    <p><a href="#util-http">util-http</a></p>
    <ul>
      <li><a href="#option-parsers">Option parsers</a></li>
      <li><a href="#applicative-parsers">Applicative parsers</a></li>
    </ul>
  </li>

  <li>
    <p><a href="#util-parsers">util-parsers</a></p>
    <ul>
      <li><a href="#option-parsers">Option parsers</a></li>
      <li><a href="#applicative-parsers">Applicative parsers</a></li>
      </ul>
  </li>
  
  <li>
      <p><a href="#util-parsers">util-parsers-cats</a></p>
      <ul>
        <li><a href="#option-parsers">Option parsers</a></li>
        <li><a href="#applicative-parsers">Applicative parsers</a></li>
        </ul>
    </li>
    
  <li>
    <p><a href="#util-testing">util-testing</a></p>
    <ul>
      <li><a href="#async-assertions">Async assertions</a></li>
      <li><a href="#data-sampling">Data sampling</a></li>
      <li><a href="#automated-sampling">Automated sampling</a></li>
    </ul>
  </li>

  <li>
    <p><a href="#util-zookeeper">util-zookeeper</a></p>
    <ul>
        <li><a href="#zookeeperinstance">ZooKeeperInstance</a></li>
        <li><a href="#zookeeperconf>ZooKeeperConf</a></li>
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
  "com.outworkers" %% "util-http" % Versions.util,
  "com.outworkers" %% "util-lift" % Versions.util,
  "com.outworkers" %% "util-domain" % Versions.util,
  "com.outworkers" %% "util-parsers" % Versions.util,
  "com.outworkers" %% "util-parsers-cats" % Versions.util,
  "com.outworkers" %% "util-validators" % Versions.util,
  "com.outworkers" %% "util-play" % Versions.util,
  "com.outworkers" %% "util-urls" % Versions.util,
  "com.outworkers" %% "util-testing" % Versions.util
)
```


### util-testing ###
<a href="#table-of-contents">Back to top</a>

The testing module features the ```AsyncAssertionsHelper```, which builds on top of ScalaTest to offer simple asynchronous assertions. We use this pattern 
heavily throughout the Outworkers ecosystem of projects, from internal to DSL modules and so forth. Asynchronous testing generally offers a considerable 
performance gain in code.


### Async assertions ###
<a href="#table-of-contents">Back to top</a>


The async assertions module features a dual API, so you can call the same methods on both ```scala.concurrent.Future``` and ```com.twitter.util.Future```. 
The underlying mechanism will create an async ```Waiter```, that will wait for the future to complete within the given ```PatienceConfiguration```. The 
awaiting is done asynchronously and the assertions are invoked and evaluated once the future in question has returned a result.

```scala
import com.outworkers.util.testing._

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

- The dependency you need is ```"com.outworkers" %% "util-testing" % UtilVersion```.
- You have to import ```com.outworkers.util.testing._```.
- You have three main assertion methods, ```successful```, ```failing```, and ```failingWith```.
- You can configure the timeout of waiters with ```implicit val timeout: PatienceConfiguration.Timeout = timeout(20 seconds)```.
- The default timeout value is ```1 second```.


### Data sampling ###
<a href="#table-of-contents">Back to top</a>

This is a very common pattern we use in our testing and it's very easy to interchange this generation with something like ScalaCheck. The idea is very simple, you use type classes to define ways to sample a given type.
After you define such a one-time sampling type class instance, you have access to several methods that will allow you to generate test data.

It's useful to define such typeclass instances inside package objects, as they will be "invisibly" imported in to the scope you need them to. This is often really neat, albeit potentially confusing for novice Scala users.


```scala

import com.outworkers.util.testing._

@sample case class MyAwesomeClass(name: String, age: Int, email: String)
```

You may notice this pattern is already available in better libraries such as ScalaMock and we are not trying to provide an alternative to ScalaMock or compete with it in any way. Our typeclass generator approach only becomes very useful where you really care about very specific properties of the data.
For instance, you may want to get a user with a valid email address, or you may use the underlying factories to get a name that reassembles the name of a real person, and so on.

It's also useful when you want to define specific ways in which hierarchies of classes are composed together into a sample. If generation for the sake of generation is all you care about, then ScalaMock with its macro based approach is a far superior product simply because there's no typing effort involved.


### Automated sampling

<a href="#table-of-contents">Back to top</a>

One interesting thing that happens when using the `@sample` annotation is that using `gen` immediately after it will basically
give you an instance of your `case class` with the fields appropiately pre-filled, and some of the basic scenarios are also name aware.

What this means is that we try to make the data feel "real" with respect to what it should be. Let's take the below example:

```scala
@sample case class User(
  id: UUID,
  firstName: String,
  lastName: String,
  email: String
)
```
This is interesting and common enough. What's more interesting is the output of `gen`.

```scala

val user = gen[User]

Console.println(user.trace())

/**
User(
  id = 6be8914c-4274-40ee-83f5-334131246fd8
  firstName = Lindsey
  lastName = Craft
  email = rparker@hotma1l.us
)
*/

```

So as you can see, the fields have been appropriately pre-filled. The email is a valid email, and the first and last name look like first and last names. For
anything that's in the default generation domain, including dates and country codes and much more, we have the ability to produce automated
appropriate values.

During the macro expansion phase, we check the annotation targets and try to infer the "natural" value based on the field name and type. So
if your field name is either "email" or "emailAddress" or anything similar enough, you will get an "email" back.


### Generating data

There are multiple methods available, allowing you to generate more than just the type:
 
- ```gen[T]```, used to generate a single instance of T.
- ```gen[X, Y]```, used to generate a tuple based on two samples.
- ```genOpt[T]```, convenience method that will give you back a ```Some[T](..)```.
- ```genList[T](limit)```, convenience method that will give you back a ```List[T]```. The numbers of items in the list is equal to the ```limit``` and has a default value of 5 if not specified.
- ```genMap[T]()```, convenience method that will give you back a ```Map[String, T]```.


There is also a default list of available generators for some default types, and to get to their value simply use the `value` method if the type is not a primitive. For things like ```EmailAddress```, the point of the extra class is obviously to distinguish the type during implicit resolution, but you don't need to use our abstraction at all, there will always be an easy way to get to the underlying generated primitives.

In the case of email addresses, you can use ```gen[EmailAddress].value```, which will correctly generate a valid ```EmailAddress``` but you can work directly with a ```String```.

- ```scala.Int```
- ```scala.Double```
- ```scala.Float```
- ```scala.Long```
- ```scala.String```
- ```scala.math.BigDecimal```
- ```scala.math.BigInt```
- ```java.util.Date```
- ```java.util.UUID```
- ```org.joda.time.DateTime```
- ```org.joda.time.LocalDate```
- ```com.outworkers.util.domain.Definitions.EmailAddress(value)```
- ```com.outworkers.util.domain.Definitions.FirstName(value)```
- ```com.outworkers.util.domain.Definitions.LastName(value)```
- ```com.outworkers.util.domain.Definitions.FullName(value)```
- ```com.outworkers.util.domain.Definitions.CountryCode(value)```
- ```com.outworkers.util.domain.Definitions.Country(value)```
- ```com.outworkers.util.domain.Definitions.City(value)```
- ```com.outworkers.util.domain.Definitions.ProgrammingLanguage(value)```
- ```com.outworkers.util.domain.Definitions.LoremIpsum(value)```


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

### Option parsers ###
<a href="#table-of-contents">Back to top</a>

The full list of optional parsers is:

| Type            | Input type                | Parser Output type                |
| --------------- |---------------------------| --------------------------------- |
| Int             | String\|Option[String]     | ValidationNel[String, Int]        |
| Long            | String\|Option[String]     | ValidationNel[String, Long]       |
| Double          | String\|Option[String]     | ValidationNel[String, Double]     |
| Float           | String\|Option[String]     | ValidationNel[String, Float]      |
| UUID            | String\|Option[String]     | ValidationNel[String, UUID]       |
| Email           | String\|Option[String]     | ValidationNel[String, String]     |
| DateTime        | String\|Option[String]     | ValidationNel[String, org.joda.time.DateTime]   |

Option parsers are designed for chains where you want to short-circuit and exit to result as soon a parser fails. This short-circuit behaviour is the default
 ```flatMap``` behaviour of an ```Option```, as soon as an ```Option``` is ```None``` the chain breaks. Unlike applicatives, 
 the evaluation sequence of options will be escaped and you cannot for instance return an error for every parser that couldn't validate. Instead, 
 you will only get the first error in the sequence.
 
An example of how to use ```Option``` parsers might be:

```scala

import com.outworkers.util.parsers._

object Test {
  def optionalParsing(email: String, age: String): Unit = {
    for {
      validEmail <- parseOpt[EmailAddress](email)
      validAge <- parseOpt[Int](age)
    } yield s"This person can be reached at $validEmail and is $validAge years old"
  }
}

```


### Applicative parsers ###
<a href="#table-of-contents">Back to top</a>

The full list of ScalaZ Validation based applicative parsers is:

| Type            | Input type                | Parser Output type                |
| --------------- |---------------------------| --------------------------------- |
| Int             | String\|Option[String]     | ValidationNel[String, Int]        |
| Long            | String\|Option[String]     | ValidationNel[String, Long]       |
| Double          | String\|Option[String]     | ValidationNel[String, Double]     |
| Float           | String\|Option[String]     | ValidationNel[String, Float]      |
| UUID            | String\|Option[String]     | ValidationNel[String, UUID]       |
| Email           | String\|Option[String]     | ValidationNel[String, String]     |
| DateTime        | String\|Option[String]     | ValidationNel[String, org.joda.time.DateTime]   |

To illustrate the basic usage of applicative parsers and how to chain them, have a look below.

```scala

import scalaz._
import scalaz.Scalaz._
import com.outworkers.util.parsers._

object Test {
  
  def registerUser(str: String, age: String): Unit = {
    (parse[EmailAddress](str) |@| parse[Int](age)) {
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

Copyright (c) 2014 - 2016 outworkers.

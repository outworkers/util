# util[![Build Status](https://travis-ci.org/outworkers/util.svg?branch=develop)](https://travis-ci.org/outworkers/util) [![Coverage Status](https://coveralls.io/repos/github/outworkers/util/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/util?branch=develop) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/util-lift_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/util-lift_2.11)   [ ![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/util/images/download.svg) ](https://bintray.com/outworkers/oss-releases/util-lift/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/util_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/util-lift_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/util?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 

This library is available on Maven Central and on our public Bintray repository, found at: `https://dl.bintray.com/outworkers/oss-releases/`.

It is publicly available, for both Scala 2.10.x and Scala 2.11.x. Check the badges at the top of this README for the
latest version of `util` available. The badges are automatically updated when a new version is out, this readme is not.

![Util](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/util.png "Outworkers Util")
 
### Table of contents

- [Integrating the util library](#integrating-the-util-library)
- [Parsers](#util-parsers)
    - [Optional parsers](#option-parsers)
    - [Applicative parsers](#applicative-parsers)
- [Testing](#util-testing)
    - [Automated data sampling](./samplers.md)
- [Contributors](#contributors)
- [Copyright](#copyright)

### Integrating the util library
<a href="#table-of-contents">Back to top</a>


The util library is designed to wrap common functionality in all our frameworks and offer it at the convenience of a dependency. Anything that will be useful
 long term to a great number of people belongs in these modules, to avoid duplication and help make our devs aware they can simply use what already exists.

The full list of available modules is:

```scala

libraryDependencies ++= Seq(
  "com.outworkers" %% "util-lift" % Versions.util,
  "com.outworkers" %% "util-domain" % Versions.util,
  "com.outworkers" %% "util-parsers" % Versions.util,
  "com.outworkers" %% "util-parsers-cats" % Versions.util,
  "com.outworkers" %% "util-validators" % Versions.util,
  "com.outworkers" %% "util-validators-cats" % Versions.util,
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


Summary:

- The dependency you need is ```"com.outworkers" %% "util-testing" % UtilVersion```.
- You have to import ```com.outworkers.util.testing._```.
- You have three main assertion methods, ```successful```, ```failing```, and ```failingWith```.
- You can configure the timeout of waiters with ```implicit val timeout: PatienceConfiguration.Timeout = timeout(20 seconds)```.
- The default timeout value is ```1 second```.


### util-parsers
<a href="#table-of-contents">Back to top</a>

The parser module features an easy to use and integrate set of ScalaZ Applicative based parsers, with an ```Option``` based parser variant. It allows us to 
seamlessly deal with validation chains at REST API level or whenever validation is involved. Whether it's monadic composition of options or chaining of 
applicative functors to obtain a "correct" chain, the parser module is designed to offer an all-you-can-eat buffet of mini parsers that can be easily 
composed to suit any validation needs. 

Each parser comes in three distinct flavours, a ```ValidationNel``` parser that parsers the end type from a ```String``` and returns the type itself, 
a parser that parses an end result from an ```Option[String]``` and parserOpt variant that returns an ```Option[T]``` instead of a ```ValidationNel[String, 
T]```, which allows for Monadic composition, where you need to "short-circuit" evaluation and validation, instead of computing the full chain by chaining 
applicatives.

### Option parsers
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

```tut:silent

import com.outworkers.util.parsers._

object Test {
  def optionalParsing(email: String, age: String): Option[String] = {
    for {
      validEmail <- parseOpt[EmailAddress](email)
      validAge <- parseOpt[Int](age)
    } yield s"This person can be reached at $validEmail and is $validAge years old"
  }
}

```


### Applicative parsers
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

```tut:silent

import scalaz._
import scalaz.Scalaz._
import com.outworkers.util.parsers._

case class UserToRegister(
  email: String,
  age: Int
)

object Test {
  
  def registerUser(str: String, age: String):  Validation[NonEmptyList[String], UserToRegister] = {
    (parse[EmailAddress](str) |@| parse[Int](age)) {
      (validEmail, validAge) => UserToRegister(validEmail.value, validAge)
    }
  }
  
}
```

### Contributors
<a href="#table-of-contents">Back to top</a>

- Flavian Alexandru @alexflav23
- Jens Halm @jenshalm
- Bartosz Jankiewicz @bjankie1


### Copyright
<a href="#table-of-contents">Back to top</a>

Copyright (c) 2014 - 2016 outworkers.

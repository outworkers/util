
#### Automated sampling

<a href="#table-of-contents">Back to top</a>

One interesting thing that happens when using the `gen` method is that you get an instance of your `case class` with the fields appropriately pre-filled, and some of the basic scenarios are also name aware.

What this means is that we try to make the data feel "real" with respect to what it should be. Let's take the below example:

```tut:silent

import java.util.UUID

case class User(
  id: UUID,
  firstName: String,
  lastName: String,
  email: String
)
```

This is interesting and common enough. What's more interesting is the output of `gen`.

```tut:silent
import com.outworkers.util.samplers._

object Examplers {
    val user = gen[User]
    
    user.trace()
    
    /**
    User(
      id = 6be8914c-4274-40ee-83f5-334131246fd8
      firstName = Lindsey
      lastName = Craft
      email = rparker@hotma1l.us
    )
    */
}

```

So as you can see, the fields have been appropriately pre-filled. The email is a valid email, and the first and last name look like first and last names. For
anything that's in the default generation domain, including dates and country codes and much more, we have the ability to produce automated
appropriate values.

During the macro expansion phase, we check the annotation targets and try to infer the "natural" value based on the field name and type. So
if your field name is either "email" or "emailAddress" or anything similar enough, you will get an "email" back.


It is also possible to generate deeply nested case classes.

```tut:silent

case class Address(
  postcode: String,
  firstLine: String,
  secondLine: String,
  thirdLine: String,
  city: String,
  country: String
)

case class GeoLocation(
  longitude: BigDecimal,
  latitude: BigDecimal
)

case class LocatedUser(
  geo: GeoLocation,
  address: Address,
  user: User
)

object GenerationExamples {
  val deeplyNested = gen[LocatedUser]
}

```

### Data sampling
<a href="#table-of-contents">Back to top</a>

This is a very common pattern we use in our testing and it's very easy to interchange this generation with something like ScalaCheck. The idea is very simple, you use type classes to define ways to sample a given type.
After you define such a one-time sampling type class instance, you have access to several methods that will allow you to generate test data.

It's useful to define such typeclass instances inside package objects, as they will be "invisibly" imported in to the scope you need them to. This is often really neat, albeit potentially confusing for novice Scala users.


```tut:silent

import com.outworkers.util.testing._

case class MyAwesomeClass(
  name: String,
  age: Int,
  email: String
)
```

You may notice this pattern is already available in better libraries such as ScalaMock and we are not trying to provide an alternative to ScalaMock or compete with it in any way. Our typeclass generator approach only becomes very useful where you really care about very specific properties of the data.
For instance, you may want to get a user with a valid email address, or you may use the underlying factories to get a name that reassembles the name of a real person, and so on.

It's also useful when you want to define specific ways in which hierarchies of classes are composed together into a sample. If generation for the sake of generation is all you care about, then ScalaMock is probably more robust.

#### Generating data

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

#### Creating custom samplers and adding new types

The automated sampling capability is a fairly simple but useful party trick. It relies
on the framework knowing how to generate basic things, such as `Int`, `Boolean`, `String`,
and so on, and the framework can then compose from these samplers to build them up
into any hierarchy of case classes you need.

But sometimes it will come short when it doesn't know how to generate a specific type. For example,
let's look at how we could deal with `java.sql.Date`, which has no implicit sample available by default.

```tut:silent

case class ExpansionExample(
  id: UUID,
  date: java.sql.Date
)
```

Let's try to write some tests around the sampler. All we need to do is create a sampler for `java.sql.Date`.

```tut:silent

import org.scalatest.{ FlatSpec, Matchers }
import java.time.{LocalDate, ZoneId}

class MyAwesomeSpec extends FlatSpec with Matchers {

  implicit val sqlDateSampler = new Sample[java.sql.Date] {
    override def sample: java.sql.Date = java.sql.Date.valueOf(LocalDate.now(ZoneId.of("UTC")))
  }
  
  "The samplers lib" should "automatically sample an instance of ExpansionExample" in {
    val instance = gen[ExpansionExample]
  }
}

```

Now, no matter how deeply nested in a case class structure the `java.sql.Date` is located inside a case class,
the framework is capable of finding it as long as it's available in the implicit scope where the `gen` method is called.


#### Working with options.

By default, whenever an `Option` type is found, the samplers lib will fluctuate the generated values
to match real world conditions, meaning both `Some` and `None` will be generated with a random frequency.

##### Always filling options

However, in practice, sometimes you might want to always fill any `Option` in a specific scope. This
is possible using an `implicit` import flag. This flag will get picked up by the `Sampler` macro,
and the generated options will always be `Some` in the scope where the flag is imported.

```tut:silent
import com.outworkers.util.samplers._
import java.util.UUID

case class Example(
  id: UUID,
  values: List[String],
  maybe: Option[String],
  testing: Option[List[String]]
)

object AppAlwaysFill {

  import com.outworkers.util.samplers.Options.alwaysFillOptions

  val sample = gen[Example]
  Console.println(sample.trace())  
}

```

##### Never filling options

In specific situations, the opposite behaviour might be desireable. This will behave exactly
like the above flag, except any `Option` generated will always be `None`.

This demonstrates how it's possible to achieve the different behaviours for the exact same case class,
meaning for some tests you can always fill, and for some never, by simply scoping the imported flag.

```tut:silent

object AppNeverFill {

  import com.outworkers.util.samplers.Options.neverFillOptions

  val sample = gen[Example]
  Console.println(sample.trace())  
}

```

#### Using empty samplers

In specific testing scenarios and by popular demand, the framework also makes it possible to generate "empty" case classes.
This means we pre-defined a notion of emptyness for any type we can.

Examples:

- Collections of all kinds will be generated as empty collections.
- Strings will be generated as the empty string `""`.
- Options will always be generated as `None`, etc.

To leverage this behaviour, the code and methods are different from `samplers`, as illustrated below.

```tut:silent

import com.outworkers.util.empty._

object GeneratingEmptyTypes {

  val emptyExample = void[Example]
}

```

##### Methods for empty samplers

Some of these methods duplicate otherwise available functionality, for the purpose
of being consistent with the methods available in the `sampler` package.

- ```void[T]```, used to generate a single instance of T.
- ```void[X, Y]```, used to generate a tuple based on two samples.
- ```voidOpt[X]```, generates an `Option.empty[X]`.
- ```voidMap[T]```, convenience method that will give you back an empty map.
package com.outworkers.util.catsparsers

import java.util.UUID
import org.scalacheck.Arbitrary
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import com.outworkers.util.testing._
import org.joda.time.DateTime

class ParsersPropertyTests extends FlatSpec with Matchers with OptionValues with GeneratorDrivenPropertyChecks {

  implicit val dateTimeGen = Sample.arbitrary[DateTime]
  implicit val uuidGen = Sample.arbitrary[UUID]

  def parserTest[T : Parser : Arbitrary](fn: T => String) = {
    forAll { value: T =>
      val parsed = parse[T](fn(value))
      parsed.isValid shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }

  it should "parse a long from a string value for every generated long" in {
    parserTest[Long](_.toString)
  }

  it should "parse a Boolean value from a string" in {
    parserTest[Boolean](_.toString)
  }

  it should "parse a Float value from a string" in {
    parserTest[Float](_.toString)
  }

  it should "parse an UUID value from a string" in {
    parserTest[UUID](_.toString)
  }

  it should "parse a Double value from a string" in {
    parserTest[Double](_.toString)
  }

  it should "parse an Int value from a string" in {
    parserTest[Int](_.toString)
  }

  it should "parse a DateTime from a valid millisecond string" in {
    parserTest[DateTime](_.getMillis.toString)
  }

  it should "parse a DateTime from a valid millisecond string using biparse" in {
    forAll { value: DateTime =>
      val parsed = biparse[String, DateTime](value.getMillis.toString)
      parsed.isValid shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }

}

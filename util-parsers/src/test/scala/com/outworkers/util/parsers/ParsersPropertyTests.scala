package com.outworkers.util.parsers

import java.util.UUID

import com.outworkers.util.testing._
import org.joda.time.DateTime
import org.scalacheck.Arbitrary
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Assertion, FlatSpec, Matchers, OptionValues}

class ParsersPropertyTests extends FlatSpec with Matchers with OptionValues with GeneratorDrivenPropertyChecks {

  implicit val dateTimeGen = Sample.arbitrary[DateTime]
  implicit val uuidGen = Sample.arbitrary[UUID]

  def parserOptTest[T : Parser : Arbitrary](fn: T => String): Assertion = {
    forAll { value: T =>
      val parsed = parse[T](Some(fn(value)))
      parsed.isSuccess shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }

  def validateOptTest[T : Parser : Arbitrary](fn: T => String): Assertion = {
    forAll { value: T =>
      val parsed = parse[T](Some(fn(value)))
      parsed.isSuccess shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }

  def parserTest[T : Parser : Arbitrary](fn: T => String): Assertion = {
    forAll { value: T =>
      val parsed = parse[T](fn(value))
      parsed.isSuccess shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }

  def validateTest[T : Parser : Arbitrary](fn: T => String): Assertion = {
    forAll { value: T =>
      val parsed = parse[T](fn(value))
      parsed.isSuccess shouldEqual true
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

  it should "validate a long from a string value for every generated long" in {
    validateTest[Long](_.toString)
  }

  it should "validate a Boolean value from a string" in {
    validateTest[Boolean](_.toString)
  }

  it should "validate a Float value from a string" in {
    validateTest[Float](_.toString)
  }

  it should "validate an UUID value from a string" in {
    validateTest[UUID](_.toString)
  }

  it should "validate a Double value from a string" in {
    validateTest[Double](_.toString)
  }

  it should "validate an Int value from a string" in {
    validateTest[Int](_.toString)
  }

  it should "validate a DateTime from a valid millisecond string" in {
    validateTest[DateTime](_.getMillis.toString)
  }

  it should "validate a Option[Boolean] value from a string" in {
    validateOptTest[Boolean](_.toString)
  }

  it should "validate a Option[Float] value from a string" in {
    validateOptTest[Float](_.toString)
  }

  it should "validate an Option[UUID] value from a string" in {
    validateOptTest[UUID](_.toString)
  }

  it should "validate a Option[Double] value from a string" in {
    validateOptTest[Double](_.toString)
  }

  it should "validate an Option[Int] value from a string" in {
    validateOptTest[Int](_.toString)
  }

  it should "validate a Option[DateTime] from a valid millisecond string" in {
    validateOptTest[DateTime](_.getMillis.toString)
  }

  it should "validate a Option[DateTime] from a valid millisecond string using biparse" in {
    forAll { value: DateTime =>
      val parsed = parse[DateTime](Some(value.getMillis.toString))
      parsed.isSuccess shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }

  it should "parse a Option[Boolean] value from a string" in {
    parserOptTest[Boolean](_.toString)
  }

  it should "parse a Option[Float] value from a string" in {
    parserOptTest[Float](_.toString)
  }

  it should "parse an Option[UUID] value from a string" in {
    parserOptTest[UUID](_.toString)
  }

  it should "parse a Option[Double] value from a string" in {
    parserOptTest[Double](_.toString)
  }

  it should "parse an Option[Int] value from a string" in {
    parserOptTest[Int](_.toString)
  }

  it should "parse a Option[DateTime] from a valid millisecond string" in {
    parserOptTest[DateTime](_.getMillis.toString)
  }

  it should "parse a Option[DateTime] from a valid millisecond string using biparse" in {
    forAll { value: DateTime =>
      val parsed = parse[DateTime](Some(value.getMillis.toString))
      parsed.isSuccess shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }
}

package com.outworkers.util.catsparsers

import org.scalatest.{FlatSpec, Matchers, OptionValues}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import com.outworkers.util.testing._
import org.joda.time.DateTime

class ParsersPropertyTests extends FlatSpec with Matchers with OptionValues with GeneratorDrivenPropertyChecks {

  implicit val dateTimeGen = Sample.arbitrary[DateTime]

  it should "parse a long from a string value for every generated long" in {
    forAll { value: Long =>
      val parsed = parse[Long](value.toString)
      parsed.isValid shouldEqual true

      parsed.toOption.value shouldEqual value
    }
  }

  it should "parse a DateTime from a valid millisecond string" in {
    forAll { value: DateTime =>
      val parsed = parse[DateTime](value.getMillis.toString)
      parsed.isValid shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }

  it should "parse a DateTime from a valid millisecond string using biparse" in {
    forAll { value: DateTime =>
      val parsed = biparse[String, DateTime](value.getMillis.toString)
      parsed.isValid shouldEqual true
      parsed.toOption.value shouldEqual value
    }
  }

}

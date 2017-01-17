package com.outworkers.util.catsparsers

import org.scalatest.{FlatSpec, Matchers, OptionValues}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class ParsersPropertyTests extends FlatSpec with Matchers with OptionValues with GeneratorDrivenPropertyChecks {

  it should "parse a long from a string value for every generated long" in {
    forAll { value: Long =>
      val parsed = parse[Long](value.toString)
      parsed.isValid shouldEqual true

      parsed.toOption.value shouldEqual value
    }

  }

}

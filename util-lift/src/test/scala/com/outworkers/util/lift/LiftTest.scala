package com.outworkers.util.lift

import org.scalatest.{Matchers, FlatSpec}
import com.outworkers.util.testing._

class LiftTest extends FlatSpec with Matchers {

  case class TestClass(name: String)


  implicit object TestClassSampler extends Sample[TestClass] {
    def sample: TestClass = {
      TestClass(
        gen[String]
      )
    }
  }
}

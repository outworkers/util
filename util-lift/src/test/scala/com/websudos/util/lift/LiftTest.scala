package com.websudos.util.lift

import org.scalatest.{Matchers, FlatSpec}
import com.websudos.util.testing._

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

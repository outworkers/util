package com.outworkers.util.testing

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AssertionsTest extends FlatSpec with Matchers {

  it should "correctly assert a failure in a failing test" in {
    val msg = gen[ShortString].value
    val f = Future { throw new Exception(msg)}

    f.failing { err =>

    }
  }

}

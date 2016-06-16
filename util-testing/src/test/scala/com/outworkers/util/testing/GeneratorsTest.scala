package com.outworkers.util.testing

import org.scalatest.FlatSpec

class GeneratorsTest extends FlatSpec {

  it should "generate a sized list based on the given argument" in {
    val limit = 10
    assert(genList[String](limit).size == limit)
  }

  it should "generate a sized map based on the given size argument" in {
    val limit = 10

    assert(genMap[String](limit).size == limit)
  }

  it should "generate a sized map of known key and value types" in {
    val limit = 10

    assert(genMap[Int, Int](limit).size == limit)
  }
}

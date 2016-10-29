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

  it should "automatically derive valid samples" in {
    val sample = gen[User]
    info(sample.trace())
  }

  it should "automatically derive generator samples for complex case classes" in {
    val sample = gen[CollectionSample]
    Console.println(sample)
    //info(sample.trace())
  }
}

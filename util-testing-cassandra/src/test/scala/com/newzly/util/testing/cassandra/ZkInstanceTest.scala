package com.newzly.util.testing.cassandra

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class ZkInstanceTest extends FlatSpec with ShouldMatchers {
  it should "automatically add localhost as a default Cassandra port during test runs" in {
    BaseTestHelper.ports.size shouldEqual 1
    BaseTestHelper.ports shouldEqual "localhost"
  }
}

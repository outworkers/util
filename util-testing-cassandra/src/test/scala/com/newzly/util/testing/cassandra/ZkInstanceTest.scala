package com.newzly.util.testing.cassandra

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.newzly.util.testing.AsyncAssertionsHelper._

class ZkInstanceTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    ZkInstance.start()
  }

  it should "automatically add localhost as a default Cassandra port during test runs" in {
    val ports = ZkInstance.richClient.getData("/cassandra", watch = false)
    ports.successful {
      res => {
        new String(res.data) shouldEqual "localhost"
      }
    }
  }

  override def afterAll(): Unit = {
    ZkInstance.stop()
  }
}

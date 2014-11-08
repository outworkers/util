package com.websudos.util.zookeeper

import java.net.InetSocketAddress

import com.twitter.util.RandomSocket
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import com.twitter.conversions.time._
import com.websudos.util.testing._

class ZooKeeperConfTest extends FlatSpec with Matchers with BeforeAndAfterAll {

  val testPath = "/" + gen[String]
  val instance = new ZooKeeperInstance(testPath)

  object ZkStore extends ZkStore()(3.seconds) {
    val address = instance.address
  }

  object TestConf extends DefaultZkConf {
    val path = testPath

    override val store = ZkStore
  }
  
  override def beforeAll(): Unit = {
    super.beforeAll()
    instance.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    instance.stop()
  }
  
  it should "parse a sequence of hosts and ports to valid set of InetSocketAddresses" in {
    val data = "localhost:9000, localhost:9001, localhost:9002"

    val expected = Set(
      new InetSocketAddress("localhost", 9000),
      new InetSocketAddress("localhost", 9001),
      new InetSocketAddress("localhost", 9002)
    )

    TestConf.parse(data) shouldEqual expected
  }

  it should "store and retrieve a sequence of ports from ZooKeeper when ports are passed as a string of data" in {

    val data = "localhost:9000, localhost:9001, localhost:9002"

    val expected = Set(
      new InetSocketAddress("localhost", 9000),
      new InetSocketAddress("localhost", 9001),
      new InetSocketAddress("localhost", 9002)
    )

    val chain = for {
      store <- TestConf.register(testPath, data)
      get <- TestConf.hosts(testPath)
    } yield get

    chain.successful {
      res => {
        res shouldEqual expected
      }
    }
  }


  it should "store and retrieve a sequence of ports from ZooKeeper when ports are passed as a sequence" in {

    val expected = Set(
      new InetSocketAddress("localhost", 9000),
      new InetSocketAddress("localhost", 9001),
      new InetSocketAddress("localhost", 9002)
    )

    val chain = for {
      store <- TestConf.register(testPath, expected)
      get <- TestConf.hosts(testPath)
    } yield get

    chain.successful {
      res => {
        res shouldEqual expected
      }
    }
  }


  it should "add a host:port pair to an existing set in ZooKeeper" in {

    val data = "localhost:9000, localhost:9001, localhost:9002"
    val next = RandomSocket.nextAddress()

    val expected = Set(
      new InetSocketAddress("localhost", 9000),
      new InetSocketAddress("localhost", 9001),
      new InetSocketAddress("localhost", 9002)
    )

    val chain = for {
      store <- TestConf.register(testPath, data)
      get <- TestConf.hosts(testPath)
      store2 <- TestConf.add(testPath, next)
      get2 <- TestConf.hosts(testPath)
    } yield (get, get2)

    chain.successful {
      res => {
        res._1 shouldEqual expected
        res._2 shouldEqual (expected + next)
      }
    }
  }

  it should "remove a host:port pair from an existing set in ZooKeeper" in {

    val data = "localhost:9000, localhost:9001, localhost:9002"
    val removable = new InetSocketAddress("localhost", 9002)

    val expected = Set(
      new InetSocketAddress("localhost", 9000),
      new InetSocketAddress("localhost", 9001)
    )

    val chain = for {
      store <- TestConf.register(testPath, data)
      get <- TestConf.hosts(testPath)
      store2 <- TestConf.remove(testPath, removable)
      get2 <- TestConf.hosts(testPath)
    } yield (get, get2)

    chain.successful {
      res => {
        res._1 shouldEqual expected + removable
        res._2 shouldEqual expected
      }
    }
  }



}

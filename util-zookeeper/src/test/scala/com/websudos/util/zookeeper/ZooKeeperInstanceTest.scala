package com.websudos.util.zookeeper

import java.net.{InetAddress, InetSocketAddress}

import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import com.websudos.util.testing._


class ZooKeeperInstanceTest extends FlatSpec with Matchers with BeforeAndAfterAll {
  val path = "/" + gen[String]
  val instance = new ZooKeeperInstance(path)

  override def beforeAll(): Unit = {
    super.beforeAll()
    instance.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    instance.stop()
  }

  it should "correctly set the status flag to true after starting the ZooKeeper Instance" in {
    instance.isStarted shouldEqual true
  }

  it should "correctly initialise a ZooKeeper ServerSet after starting a ZooKeeper instance" in {
    instance.zookeeperServer.isRunning shouldEqual true
  }

  it should "allow setting a value for the path" in {

    val data = gen[String]

    val chain = for {
      set <- instance.client.setData(path, data.getBytes, -1)
      get <- instance.client.getData(path, watch = false)
    } yield get


    chain.successful {
      res => {
        new String(res.data) shouldEqual data
      }
    }
  }

  it should "correctly parse the retrieved data into a Sequence of InetSocketAddresses" in {

    val data = InetAddress.getLocalHost
    val port = 1001
    val address = s"${data.getHostName}:$port"

    val chain = for {
      set <- instance.client.setData(path, address.getBytes, -1)
      get <-  instance.hostnamePortPairs
    } yield get

    chain.successful {
      res => {
        res shouldEqual Seq(new InetSocketAddress(data.getHostName, port))
      }
    }
  }
}

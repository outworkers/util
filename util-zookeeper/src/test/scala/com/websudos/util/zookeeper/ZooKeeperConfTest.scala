/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.util.zookeeper

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import com.twitter.util.RandomSocket
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import com.twitter.conversions.time._
import com.websudos.util.testing._

import scala.concurrent.duration.Duration

class ZooKeeperConfTest extends FlatSpec with Matchers with BeforeAndAfterAll {


  implicit val patience: PatienceConfiguration.Timeout = timeout(Duration(3L, TimeUnit.SECONDS))

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


  ignore should "store and retrieve a sequence of ports from ZooKeeper when ports are passed as a sequence" in {

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


  ignore should "add a host:port pair to an existing set in ZooKeeper" in {

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

  ignore should "remove a host:port pair from an existing set in ZooKeeper" in {

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

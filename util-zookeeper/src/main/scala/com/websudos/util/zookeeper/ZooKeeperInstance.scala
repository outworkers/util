package com.websudos.util.zookeeper
/*
 *
 *  * Copyright 2014 websudos ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
import java.net.InetSocketAddress

import org.apache.zookeeper.server.{NIOServerCnxn, ZKDatabase, ZooKeeperServer}
import org.apache.zookeeper.server.persistence.FileTxnSnapLog

import com.twitter.common.io.FileUtils.createTempDir
import com.twitter.common.quantity.{Amount, Time}
import com.twitter.common.zookeeper.{ServerSetImpl, ZooKeeperClient}
import com.twitter.conversions.time._
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.finagle.zookeeper.ZookeeperServerSetCluster
import com.twitter.util.{Await, Future, RandomSocket, Try}


private[this] object ZooKeeperInitLock

class ZooKeeperInstance(zkPath: String, val address: InetSocketAddress = RandomSocket.nextAddress()) {

  private[this] var status = false

  def isStarted: Boolean = status

  val zookeeperConnectString  = address.getHostName + ":" + address.getPort
  val defaultZookeeperConnectorString = "localhost:2181"

  protected[this] val envString = "TEST_ZOOKEEPER_CONNECTOR"

  lazy val connectionFactory: NIOServerCnxn.Factory = new NIOServerCnxn.Factory(address)
  lazy val txn = new FileTxnSnapLog(createTempDir(), createTempDir())
  lazy val zkdb = new ZKDatabase(txn)

  lazy val zookeeperServer: ZooKeeperServer = new ZooKeeperServer(
    txn,
    ZooKeeperServer.DEFAULT_TICK_TIME,
    100,
    100,
    new ZooKeeperServer.BasicDataTreeBuilder,
    zkdb
  )
  var zookeeperClient: ZooKeeperClient = null

  lazy val client = ZooKeeper.newRichClient(zookeeperConnectString)

  def resetEnvironment(cn: String = zookeeperConnectString): Unit = {
    System.setProperty(envString, cn)
  }

  def start(): Unit = Lock.synchronized {
    if (!status) {
      resetEnvironment()
      connectionFactory.startup(zookeeperServer)

      zookeeperClient = new ZooKeeperClient(
        Amount.of(10, Time.MILLISECONDS),
        address)

      val serverSet = new ServerSetImpl(zookeeperClient, zkPath)
      val cluster: ZookeeperServerSetCluster = new ZookeeperServerSetCluster(serverSet)

      cluster.join(address)

      Await.ready(client.connect(2.seconds), 2.seconds)
      // Disable noise from zookeeper logger
      java.util.logging.LogManager.getLogManager.reset()
      status = true
    } else {
      resetEnvironment(defaultZookeeperConnectorString)
    }
  }

  def stop() {
    if (status) {
      connectionFactory.shutdown()
      zookeeperClient.close()
      Await.ready(client.close(), 2.seconds)
      status = false
    }
  }

  def hostnamePortPairs: Future[Seq[InetSocketAddress]] = client.getData(zkPath, watch = false) map {
    res => Try {
      val data = new String(res.data)
      data.split("\\s*,\\s*").map(_.split(":")).map {
        case Array(hostname, port) => new InetSocketAddress(hostname, port.toInt)
      }.toSeq
    } getOrElse Seq.empty[InetSocketAddress]
  }
}

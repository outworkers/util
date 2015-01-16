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

package com.newzly.util.testing.cassandra

import org.apache.zookeeper.server.{ NIOServerCnxn, ZKDatabase, ZooKeeperServer }
import org.apache.zookeeper.server.persistence.FileTxnSnapLog

import com.twitter.common.zookeeper.{ServerSetImpl, ZooKeeperClient}
import com.twitter.common.io.FileUtils.createTempDir
import com.twitter.common.quantity.{Amount, Time}
import com.twitter.conversions.time._
import com.twitter.finagle.zookeeper.ZookeeperServerSetCluster
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.util.{ Await, RandomSocket }

class ZkInstance {
  val zookeeperAddress = RandomSocket.nextAddress()
  val zookeeperConnectString  = zookeeperAddress.getHostName + ":" + zookeeperAddress.getPort
  var connectionFactory: NIOServerCnxn.Factory = null
  var zookeeperServer: ZooKeeperServer = null
  var zookeeperClient: ZooKeeperClient = null
  lazy val richClient = ZooKeeper.newRichClient(zookeeperConnectString)


  def start() {

    val txn = new FileTxnSnapLog(createTempDir(), createTempDir())
    val zkdb = new ZKDatabase(txn)
    zookeeperServer = new ZooKeeperServer(
      txn,
      ZooKeeperServer.DEFAULT_TICK_TIME,
      100,
      100,  // min/max sesssion timeouts in milliseconds
      new ZooKeeperServer.BasicDataTreeBuilder,
      zkdb
    )
    connectionFactory = new NIOServerCnxn.Factory(zookeeperAddress)
    connectionFactory.startup(zookeeperServer)
    zookeeperClient = new ZooKeeperClient(
      Amount.of(10, Time.MILLISECONDS),
      zookeeperAddress)

    val serverSet = new ServerSetImpl(zookeeperClient, "/cassandra")
    val cluster = new ZookeeperServerSetCluster(serverSet)

    cluster.join(zookeeperAddress)

    Await.ready(richClient.connect(2.seconds), 2.seconds)
    Await.ready(richClient.setData("/cassandra", "localhost".getBytes, -1), 3.seconds)

    // Disable noise from zookeeper logger
    java.util.logging.LogManager.getLogManager.reset()
  }

  def stop() {
    connectionFactory.shutdown()
    zookeeperClient.close()
  }
}


object ZkInstance extends ZkInstance {
  start()
}

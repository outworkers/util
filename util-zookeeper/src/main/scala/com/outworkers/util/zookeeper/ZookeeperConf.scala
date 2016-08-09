package com.outworkers.util.zookeeper

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
import java.net.{InetAddress, InetSocketAddress}

import com.twitter.conversions.time._
import com.twitter.finagle.exp.zookeeper.client.ZkClient
import com.twitter.finagle.exp.zookeeper.{SetDataResponse, ZooKeeper}
import com.twitter.util.{Await, Duration, Future, Try}


private[this] object Lock

/**
  * This is a ZooKeeper client store for accessors using ZooKeeperConf traits to read/write from ZooKeeper.
  * Its sole purpose is to centralise access to ZooKeeper hosts and not necessitate access to multiple clients.
  *
  * Using a single global client object with a synchronized access pattern and just-in-time init we can solve the problem of localhost Mesos based deployments.
  * In a Mesos environment the ZooKeeper host is always fixed and every client application can easily talk to the same node to obtain data.
  *
  * Users can easily override all the default implementation details and assumptions this store has and roll out their own communication instance with
  * ZooKeeper. For now, this is mostly tailored to our Mesos deployment mechanism.
  *
  * @param timeout The connection timeout used to enforce a timeout when connection to the ZooKeeper host.
  */
abstract class ZkStore()(implicit val timeout: Duration) {

  val address: InetSocketAddress

  private[this] var _clientStore: ZkClient = null

  private[this] var init = false

  private[this] def initIfNotInited(): Unit = Lock.synchronized {
    if (!init) {
      _clientStore = ZooKeeper.newRichClient(s"${address.getHostName}:${address.getPort}")
      Await.ready(_clientStore.connect(timeout), timeout)
      init = true
    }
  }

  /**
    * Simple accessor that will guarantee a client is initialised before it is used.
    * It's not the cheapest to maintain due to the inherent synchronisation required by maintaining global state.
    *
    * However, for its purpose, a reference to this client is required only in the initialisation of clients and its generally to cheap to matter.
    * @return A reference to the global ZooKeeper client guaranteed by this store.
    */
  def client: ZkClient = Lock.synchronized {
    initIfNotInited()
    _clientStore
  }

  /**
    * The default way to serialise an InetSocketAddress to a string.
    * This method will be used by all other primitive definitions inside this project meaning users can easily override this to enforce their own conventions.
    *
    * JSON serialisation or any other form of serialization could easily be used to store and retrieve data from a ZooKeeper path.
    * The data in question is only intended for sequences of host:port pairs.
    * @param address The InetSocketAddress to serialise to a string.
    * @return The string representation of the InetSocketAddress that will be stored as bytes in ZooKeeper.
    */
  def serialize(address: InetSocketAddress): String = {
    s"${address.getHostName}:${address.getPort}"
  }

  def serialize(address: Set[InetSocketAddress]): String = {
    address.map(serialize).mkString(", ")
  }
}

object DefaultClientStore extends ZkStore()(5.seconds) {
  val address = new InetSocketAddress("localhost", 2181)
}


/**
  * This is a simple mixable trait for client applications to fetch ports from ZooKeeper during initialisation or register themselves on a path.
  * In our eco-system, this is generally mixed in to objects extending TwitterServer or ClientBuilder.
  *
  * The point is to allow very easy access to the local ZooKeeper node available in a Mesos deployment to fetch "the other" host:port pairs in the eco-system,
  * allowing Thrift clients to auto-generate themselves with a trivial awaiting of a Future. The same mechanism can be used for any type of client.
  *
  * This also allows applications to register themselves to a ZooKeeper path making them discoverable by other clients.
  */
trait ZooKeeperConf {

  /**
    * The default connection timeout.
    * This is enforced when the ZooKeeper client connects to the ZooKeeper host.
    */
  implicit val timeout = 3.seconds

  private[this] val localhost = InetAddress.getLocalHost.getHostName

  /**
    * The store synchronising access to the ZooKeeper client.
    * @return
    */
  def store: ZkStore

  /**
    * This method is used to parse the string of data obtained from a ZooKeeper path into a sequence of ports.
    * It's highly opinionated about the format in which teh data should be and it will only work with our default.
    *
    * Our format is "host:port*".
    *
    * @param data The data string fetched from ZooKeeper.
    * @return
    */
  def parse(data: String): Set[InetSocketAddress] = {
    data.split("\\s*,\\s*").map(_.split(":")).map {
      case Array(hostname, port) => new InetSocketAddress(hostname, port.toInt)
    }.toSet[InetSocketAddress]
  }

  /**
    * This has the effect of pushing a new InetSocketAddress or its string representation to be price to the set already in ZooKeeper.
    * A read operation is always performed first to avoid maintaining any state of the client side.
    *
    * If the data is not available in the data that was read, a write operation is carried out. If the data is already there,
    * the write operation is skipped to save time and bandwidth a Future is immediately completed.
    *
    * @param path The ZooKeeper path to add the address to.
    * @param address A Future wrapping an optional operation response. If the address is already in the set no write operation is performed.
    * @return
    */
  def add(path: String, address: InetSocketAddress): Future[Option[SetDataResponse]] = {
    hosts(path).flatMap {
      seq => {
        seq.find(store.serialize(address) == ).fold[Future[Option[SetDataResponse]]] {
          val updated = seq + address
          register(path, store.serialize(updated)) map { Some(_)}
        }(_ => Future.None)
      }
    }
  }

  /**
    * This will remove an address from a ZooKeeper node/path.
    * This is useful when a server is going out of service or any other similar situation.
    *
    * A read operation is always carried out no matter what to avoid maintaining any state of the client side.
    * If the address to be removed is not found in the existing set, no write operation will be carried out.
    *
    * @param path The ZooKeeper path to write to.
    * @param address The InetSocketAddress to remove from the existing set.
    * @return A Future wrapping an optional operation response. If the address is not in the set no write operation is performed.
    */
  def remove(path: String, address: InetSocketAddress): Future[Option[SetDataResponse]] = {
    hosts(path).flatMap {
      seq => {
        seq.find(_ == address).fold[Future[Option[SetDataResponse]]](Future.None) {
          _ => register(path, store.serialize(seq - address)) map { Some(_) }
        }
      }
    }
  }

  /**
    * This is a simple shorthand method allowing users to get a reference to the hostname of the current machine.
    * The purpose of this is to allow shorthand syntax for things like {@code}server.add(localAddress(somePort)){code},
    * effectively allowing applications to register as active and become discoverable via ZooKeeper.
    *
    * @param port The port to use in conjunction with the hostname of the current machine.
    * @return An InetSocketAddress matching the hostname of the current machine and a chosen port.
    */
  def localAddress(port: Int): InetSocketAddress = new InetSocketAddress(localhost, port)

  /**
    * Registers a address on a ZooKeeper node.
    * This will overwrite any existing data on the path.
    *
    * @param path The ZooKeeper path to write to.
    * @param address The InetSocketAddress to save as a sequence.
    * @return A Future wrapping the response of the write operation.
    */
  def register(path: String, address: InetSocketAddress): Future[SetDataResponse] = {
    store.client.setData(path, store.serialize(address).getBytes, -1)
  }

  /**
    * Registers a new set of addresses on a ZooKeeper node.
    * This will overwrite any existing data on the path.
    *
    * @param path The ZooKeeper path to write to.
    * @param address The InetSocketAddress to save as a sequence.
    * @return A Future wrapping the response of the write operation.
    */
  def register(path: String, address: Set[InetSocketAddress]): Future[SetDataResponse] = {
    store.client.setData(path, store.serialize(address).getBytes, -1)
  }

  /**
    * This is a shorthand method for a UTF-8 based write operation to a ZooKeeper path.
    * It will simply overwrite the existing data on a path with the argument provided.
    *
    * The encoding in use by this method is always UTF 8.
    *
    * @param path The ZooKeeper path to write to.
    * @param data The string of data to write to the ZooKeeper path.
    * @return A Future wrapping the response of the write operation.
    */
  protected[zookeeper] def register(path: String, data: String): Future[SetDataResponse] = {
    store.client.setData(path, data.getBytes, -1)
  }

  /**
    * This method is shorthand syntax for retrieving the entire set of host:port pairs available on a ZooKeeper node.
    * It's used in conjunction with the default parsing convention defined.
    *
    * @param path The ZooKeeper path to read from.
    * @return A set of unique host:port combinations represented as a set of InetSocketAddress objects.
    */
  def hosts(path: String): Future[Set[InetSocketAddress]] = {
    store.client.getData(path, watch = false) map {
      res => Try {
        parse(new String(res.data))
      } getOrElse Set.empty[InetSocketAddress]
    }
  }

}

trait DefaultZkConf extends ZooKeeperConf {
  val store = DefaultClientStore
}
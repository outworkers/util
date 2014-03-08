package com.newzly.util.cassandra

import org.cassandraunit.utils.EmbeddedCassandraServerHelper

object CassandraTestHelper extends App {

  override def main(args: Array[String]): Unit = {
    Console.println("Start Cassandra server")
    EmbeddedCassandraServerHelper.startEmbeddedCassandra()
  }

}
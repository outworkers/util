package com.newzly.util.testing.cassandra


import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ Assertions, BeforeAndAfterAll, FeatureSpec, FlatSpec, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, ScalaFutures }

import com.datastax.driver.core.{ Cluster, Session }
import com.twitter.conversions.time._
import com.twitter.util.Await

object BaseTestHelper {

  ZkInstance.start()
  val embeddedMode = new AtomicBoolean(false)

  private[this] def getPort: Int = {
    if (System.getenv().containsKey("TRAVIS_JOB_ID")) {
      Console.println("Using Cassandra as a Travis Service with port 9042")

      9042
    } else {
      Console.println("Using Embedded Cassandra with port 9142")
      embeddedMode.compareAndSet(false, true)
      9142
    }
   }

  val ports = new String(Await.result(ZkInstance.richClient.getData("/cassandra", watch = false), 3.seconds).data)

  val cluster = Cluster.builder()
    .addContactPoint(ports)
    .withPort(getPort)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()
}

trait CassandraTest {
  self: BeforeAndAfterAll =>
  val keySpace: String
  val cluster = BaseTestHelper.cluster
  implicit lazy val session: Session = cluster.connect()
  implicit lazy val context: ExecutionContext = global

  private[this] def createKeySpace(spaceName: String) = {
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $spaceName WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session.execute(s"use $spaceName;")
  }
  override def beforeAll() {
    if (BaseTestHelper.embeddedMode.get()) {
      EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
      EmbeddedCassandraServerHelper.startEmbeddedCassandra()
    }
    createKeySpace(keySpace)
  }

  override def afterAll() {
    session.execute(s"DROP KEYSPACE $keySpace;")
  }

}

trait BaseTest extends FlatSpec with ScalaFutures with BeforeAndAfterAll with Matchers with Assertions with AsyncAssertions with CassandraTest {}

trait FeatureBaseTest extends FeatureSpec with ScalaFutures with BeforeAndAfterAll with Matchers with Assertions with AsyncAssertions with CassandraTest {}



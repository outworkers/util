package com.newzly.util.testing.cassandra

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.{ Assertions, BeforeAndAfterAll, FeatureSpec, FlatSpec, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, ScalaFutures }
import com.datastax.driver.core.{ Cluster, Session }

object BaseTestHelper {

  private[this] def getPort: Int = {
    if (System.getenv().containsKey("TRAVIS_JOB_ID")) {
      Console.println("Using Cassandra as a Travis Service with port 9042")
      9042
    } else {
      Console.println("Using Embedded Cassandra with port 9142")
      9142
    }
   }

  val cluster = Cluster.builder()
    .addContactPoint("localhost")
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
    createKeySpace(keySpace)
  }

  override def afterAll() {
    session.execute(s"DROP KEYSPACE $keySpace;")
  }

}

trait BaseTest extends FlatSpec with ScalaFutures with BeforeAndAfterAll with Matchers with Assertions with AsyncAssertions with CassandraTest {}

trait FeatureBaseTest extends FeatureSpec with ScalaFutures with BeforeAndAfterAll with Matchers with Assertions with AsyncAssertions with CassandraTest {}



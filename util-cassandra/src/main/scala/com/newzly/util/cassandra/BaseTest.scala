package com.newzly.util.cassandra

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest._
import org.scalatest.concurrent.{ AsyncAssertions, ScalaFutures }
import com.datastax.driver.core.{ Cluster, Session }

object BaseTestHelper {
  val cluster = Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9142)
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
    session.execute(s"CREATE IF NOT EXISTS KEYSPACE $spaceName WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
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



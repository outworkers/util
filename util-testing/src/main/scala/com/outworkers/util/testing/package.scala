/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.util

import com.outworkers.util.domain.Definitions
import com.outworkers.util.samplers.Generators
import com.outworkers.util.tags.DefaultTaggedTypes
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.scalacheck.Gen
import org.scalatest.{Assertion, Assertions}
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures, Waiters}
import org.scalatest.exceptions.TestFailedException

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Await => ScalaAwait, Future => ScalaFuture}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

package object testing extends ScalaFutures
  with Generators
  with Definitions
  with DefaultTaggedTypes {

  type Sample[R] = com.outworkers.util.samplers.Sample[R]
  val Sample = com.outworkers.util.samplers.Sample
  val Generators = com.outworkers.util.samplers.Generators

  implicit object DateTimeSampler extends Sample[DateTime] {
    val limit = 10000
    def sample: DateTime = {
      // may the gods of code review forgive me for me sins
      val offset = Gen.choose(-limit, limit).sample.getOrElse(limit)
      val now = new DateTime(DateTimeZone.UTC)
      now.plusSeconds(offset)
    }
  }

  implicit object JodaLocalDateSampler extends Sample[LocalDate] {
    val limit = 10000
    def sample: LocalDate = {
      // may the gods of code review forgive me for me sins
      val offset = Gen.choose(-limit, limit).sample.getOrElse(limit)
      val zone = Generators.oneOf(DateTimeZone.getAvailableIDs.asScala.toList)
      new LocalDate(DateTimeSampler.sample.getMillis + offset * 1000, DateTimeZone.forID(zone))
    }
  }

  def shouldNotThrow[T](pf: => T): Unit = try pf catch {
    case NonFatal(e) =>
      if (e.isInstanceOf[TestFailedException]) {
        throw e
      } else {
        Assertions.fail(s"Expected no errors to be thrown but got ${e.getMessage}")
      }
  }

  def mustNotThrow[T](pf: => T): Unit = shouldNotThrow[T](pf)

  implicit class ScalaBlockHelper[T](val future: ScalaFuture[T]) extends AnyVal {
    def block(duration: scala.concurrent.duration.Duration)(implicit ec: ExecutionContext): T = {
      ScalaAwait.result(future, duration)
    }
  }
}

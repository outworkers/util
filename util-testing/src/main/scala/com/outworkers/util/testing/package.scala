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

import com.outworkers.util.domain.GenerationDomain
import com.outworkers.util.samplers.{Generators, Sample}
import com.outworkers.util.tags.DefaultTaggedTypes
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.scalacheck.Gen
import org.scalatest.Assertions
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures, Waiters}
import org.scalatest.exceptions.TestFailedException

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Await => ScalaAwait, Future => ScalaFuture}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Random}

package object testing extends ScalaFutures
  with Generators
  with GenerationDomain
  with DefaultTaggedTypes {

  implicit object DateTimeSampler extends Sample[DateTime] {
    val limit = 10000
    def sample: DateTime = {
      // may the gods of code review forgive me for me sins
      val offset = Gen.choose(-limit, limit).sample.get
      val now = new DateTime(DateTimeZone.UTC)
      now.plusSeconds(offset)
    }
  }

  implicit object JodaLocalDateSampler extends Sample[LocalDate] {
    val limit = 10000
    def sample: LocalDate = {
      // may the gods of code review forgive me for me sins
      val offset = Gen.choose(-limit, limit).sample.get
      val zone = Generators.oneOf(DateTimeZone.getAvailableIDs.asScala.toList)
      new LocalDate(DateTimeSampler.sample.getMillis + offset, DateTimeZone.forID(zone))
    }
  }

  type Sample[R] = com.outworkers.util.samplers.Sample[R]
  val Sample = com.outworkers.util.samplers.Sample
  val Generators = com.outworkers.util.samplers.Generators

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

  /**
   * Augmentation to allow asynchronous assertions of a @code {scala.concurrent.Future}.
   * @param f The future to augment.
   * @tparam A The underlying type of the computation.
   */
  implicit class ScalaFutureAssertions[A](val f: ScalaFuture[A]) extends Assertions with Waiters {

    /**
     * Use this to assert an expected asynchronous failure of a @code {com.twitter.util.Future}
     * The computation and waiting are both performed asynchronously.
     * @param mf The class Manifest to extract class information from.
     * @param timeout The timeout of the asynchronous Waiter.
     * @tparam T The error returned by the failing computation. Used to assert error messages.
     */
    def failing[T  <: Throwable](implicit mf: Manifest[T], timeout: PatienceConfiguration.Timeout, ec: ExecutionContext): Unit = {
      val w = new Waiter

      f onComplete {
        case Success(_) => w.dismiss()
        case Failure(e) => w(throw e); w.dismiss()
      }

      intercept[T] {
        w.await(timeout, dismissals(1))
      }
    }

    def failingWith[T <: Throwable](fs: ScalaFuture[_]*)(implicit mf: Manifest[T], ec: ExecutionContext) {
      val w = new Waiter
      fs foreach (_ onComplete {
        case Failure(er) =>
          w(intercept[T](er))
          w.dismiss()
        case Success(_) => w.dismiss()
      })
      w.await()
    }

    /**
     * Use this to assert a successful future computation of a @code {com.twitter.util.Future}
     * @param x The computation inside the future to await. This waiting is asynchronous.
     * @param timeout The timeout of the future.
     */
    @deprecated("Use ScalaTest AsyncAssertions trait instead", "0.31.0")
    def successful(x: A => Unit)(implicit timeout: PatienceConfiguration.Timeout, ec: ExecutionContext) : Unit = {
      val w = new Waiter

      f onComplete  {
        case Success(res) => w{x(res)}; w.dismiss()
        case Failure(e) => w(throw e); w.dismiss()
      }
      w.await(timeout, dismissals(1))
    }
  }
}

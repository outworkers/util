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
import com.outworkers.util.samplers.Generators
import com.outworkers.util.tags.DefaultTaggedTypes

import org.scalatest.Assertions
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures, Waiters}

import scala.concurrent.{ExecutionContext, Await => ScalaAwait, Future => ScalaFuture}
import scala.util.{Failure, Success}

package object samplers extends ScalaFutures
  with ScalaTestHelpers
  with Generators
  with GenerationDomain
  with DefaultTaggedTypes {

  implicit class Printer[T](val obj: T) extends AnyVal {
    def trace()(implicit tracer: Tracer[T]): String = tracer.trace(obj)
  }

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

    def asTwitter()(implicit ec: ExecutionContext): com.twitter.util.Future[A] = {
      val promise = com.twitter.util.Promise[A]()

      f onComplete {
        case Failure(er) => promise raise er
        case Success(data) => promise become Future.value(data)
      }

      promise
    }

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

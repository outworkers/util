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
package com.outworkers.util.testing

import com.twitter.util.{Await, Future, Return, Throw}
import org.scalatest.Assertions
import org.scalatest.concurrent.{PatienceConfiguration, Waiters}

package object twitter {

  implicit class TwitterBlockHelper[T](val f: Future[T]) extends AnyVal {
    def block(duration: com.twitter.util.Duration): T = Await.result(f, duration)
  }

  /**
    * Augmentation to allow asynchronous assertions of a @code {com.twitter.util.Future}.
    * @param f The future to augment.
    * @tparam A The underlying type of the computation.
    */
  implicit class TwitterFutureAssertions[A](val f: Future[A]) extends Assertions with Waiters {

    def asScala: scala.concurrent.Future[A] = {
      val promise = scala.concurrent.Promise[A]()

      f respond {
        case Throw(er) => promise failure er
        case Return(data) => promise success data
      }

      promise.future
    }

    /**
      * Use this to assert an expected asynchronous failure of a @code {com.twitter.util.Future}
      * The computation and waiting are both performed asynchronously.
      * @param mf The class Manifest to extract class information from.
      * @param timeout The timeout of the asynchronous Waiter.
      * @tparam T The error returned by the failing computation. Used to assert error messages.
      */
    def failing[T  <: Throwable]()(implicit mf: Manifest[T], timeout: PatienceConfiguration.Timeout): Unit = {
      val w = new Waiter

      f onSuccess  { _ => w.dismiss()}

      f onFailure { e => w(throw e); w.dismiss() }

      intercept[T] {
        w.await(timeout, dismissals(1))
      }
    }

    def failingWith[T <: Throwable : Manifest](fs: Future[_]*) {
      val w = new Waiter
      fs foreach (_ respond  {
        case Throw(er) =>
          w(intercept[T](er))
          w.dismiss()
        case Return(_) => w.dismiss()
      })
      w.await()
    }

    /**
      * Use this to assert a successful future computation of a @code {com.twitter.util.Future}
      * @param x The computation inside the future to await. This waiting is asynchronous.
      * @param timeout The timeout of the future.
      */
    def successful(x: A => Unit)(implicit timeout: PatienceConfiguration.Timeout) : Unit = {
      val w = new Waiter

      f onSuccess {
        res => w{x(res)}; w.dismiss()
      }

      f onFailure {
        e => w(throw e); w.dismiss()
      }
      w.await(timeout, dismissals(1))
    }
  }
}

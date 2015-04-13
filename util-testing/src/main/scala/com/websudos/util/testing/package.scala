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
package com.websudos.util

import com.twitter.util.{Await, Future, Return, Throw}
import org.scalacheck.Arbitrary
import org.scalatest.Assertions
import org.scalatest.concurrent.{AsyncAssertions, PatienceConfiguration, ScalaFutures}

import scala.concurrent.duration._
import scala.concurrent.{Await => ScalaAwait, ExecutionContext, Future => ScalaFuture}
import scala.util.{Failure, Success}

package object testing extends ScalaFutures with DefaultTags with DefaultSamplers with ScalaTestHelpers {
  /**
   * The default timeout of the asynchronous assertions.
   * To override this, simply define another implicit timeout in the desired scope.
   */
  implicit val s: PatienceConfiguration.Timeout = timeout(1 second)


  implicit class ScalaBlockHelper[T](val future: ScalaFuture[T]) extends AnyVal {
    def block(duration: scala.concurrent.duration.Duration)(implicit ec: ExecutionContext): T = {
      ScalaAwait.result(future, duration)
    }
  }

  implicit class TwitterBlockHelper[T](val f: Future[T]) extends AnyVal {
    def block(duration: com.twitter.util.Duration): T = {
      Await.result(f, duration)
    }
  }

  /**
   * Augmentation to allow asynchronous assertions of a @code {com.twitter.util.Future}.
   * @param f The future to augment.
   * @tparam A The underlying type of the computation.
   */
  implicit class TwitterFutureAssertions[A](val f: Future[A]) extends Assertions with AsyncAssertions {

    /**
     * Use this to assert an expected asynchronous failure of a @code {com.twitter.util.Future}
     * The computation and waiting are both performed asynchronously.
     * @param mf The class Manifest to extract class information from.
     * @param timeout The timeout of the asynchronous Waiter.
     * @tparam T The error returned by the failing computation. Used to assert error messages.
     */
    def failing[T  <: Throwable]()(implicit mf: Manifest[T], timeout: PatienceConfiguration.Timeout): Unit = {
      val w = new Waiter

      f onSuccess  {
        res => w.dismiss()
      }

      f onFailure {
        e => w(throw e); w.dismiss()
      }
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
        case Return(data) => w.dismiss()
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
        case res => w{x(res)}; w.dismiss()
      }

      f onFailure {
        e => w(throw e); w.dismiss()
      }
      w.await(timeout, dismissals(1))
    }
  }

  /**
   * Augmentation to allow asynchronous assertions of a @code {scala.concurrent.Future}.
   * @param f The future to augment.
   * @tparam A The underlying type of the computation.
   */
  implicit class ScalaFutureAssertions[A](val f: ScalaFuture[A]) extends Assertions with AsyncAssertions {

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
        case Success(data) => w.dismiss()
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
        case Success(data) => w.dismiss()
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


  implicit def sampleToArbitrary[T](gen: Sample[T]): Arbitrary[T] = Arbitrary(gen.sample)

  implicit def arbitraryToSample[T](arbitrary: Arbitrary[T]): Sample[T] = new Sample[T] {
    override def sample: T = arbitrary.arbitrary.sample.get
  }


}

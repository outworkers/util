package com.newzly.util.finagle

import org.scalatest._
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration, ScalaFutures }
import com.twitter.util.{ Await, Duration, Future, Throw }

import org.scalatest.time.SpanSugar._

object AsyncAssertionsHelper extends ScalaFutures {

  /**
   * The default timeout of the asynchronous assertions.
   * To override this, simply define another implicit timeout in the desired scope.
   */
  implicit val s: PatienceConfiguration.Timeout = timeout(1 second)

  /**
   * A simple augmentation adding a .sync() method to a @code {com.twitter.util.Future}.
   * This is a blocking computation.
   * @param future The future to execute.
   * @tparam T The underlying return type of the computation.
   */
  implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, Duration.fromSeconds(10))
    }
  }

  /**
   * Augmentation to allow asynchronous assertions of a @code {com.twitter.util.Future}.
   * @param f The future to augment.
   * @tparam A The underlying type of the computation.
   */
  implicit class Failing[A](val f: Future[A]) extends Assertions with AsyncAssertions {

    /**
     * Use this to assert an expected asynchronous failure of a @code {com.twitter.util.Future}
     * The computation and waiting are both performed asynchronously.
     * @param mf The class Manifest to extract class information from.
     * @param timeout The timeout of the asynchronous Waiter.
     * @tparam T The error returned by the failing computation. Used to assert error messages.
     */
    def failing[T  <: Throwable](implicit mf: Manifest[T], timeout: PatienceConfiguration.Timeout): Unit = {
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
      val count = new java.util.concurrent.atomic.AtomicInteger(fs.size)
      val w = new Waiter
      fs foreach (_ respond  {
        case Throw(er) =>
          w(intercept[T](er))
          println(s"Bad success $er")
          w.dismiss()

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


}
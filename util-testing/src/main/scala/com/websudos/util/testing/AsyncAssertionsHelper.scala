package com.websudos.util.testing

import scala.concurrent.{ExecutionContext, Await => ScalaAwait, Future => ScalaFuture}
import scala.util.{Failure, Success}

import org.scalatest._
import org.scalatest.concurrent.{AsyncAssertions, PatienceConfiguration, ScalaFutures}
import org.scalatest.time.SpanSugar._

import com.twitter.util.{Await, Duration, Future, Return, Throw}

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
   * A simple augmentation adding a .sync() method to a @code {com.twitter.util.Future}.
   * This is a blocking computation.
   * @param future The future to execute.
   * @tparam T The underlying return type of the computation.
   */
  implicit class ScalaSyncFuture[T](future: ScalaFuture[T])(implicit ec: ExecutionContext) {
    def sync(): T = {
      ScalaAwait.result(future, 5000 millis)
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
      val w = new Waiter
      fs foreach (_ respond  {
        case Throw(er) =>
          w(intercept[T](er))
          println(s"Bad success $er")
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
          println(s"Bad success $er")
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


}

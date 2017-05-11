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
package com.outworkers.util.validators

import cats.data.Validated.{Invalid, Valid}
import cats.{Applicative, Semigroup, SemigroupK}
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import com.outworkers.util.domain.ApiError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

trait ValidatorImplicits extends Wrappers {

  implicit class CatsPropValidation[X, T](val vd: ValidatedNel[X, T]) {
    def prop(str: String): ValidatedNel[(String, X), T] = {
      vd.leftMap(f => NonEmptyList(str -> f.head, Nil))
    }
  }

  implicit def catsErrorConvert[T](vd: ValidatedNel[String, Future[T]])(
    implicit ctx: ExecutionContext
  ): Future[Validated[ApiError, T]] = {
    vd.fold(
      nel => Future.successful(Invalid(ApiError.fromArgs(ApiError.defaultErrorCode, nel.toList))),
      future => future.map(Valid.apply)
    )
  }

  implicit class CatsErrorHelper[T](val vd: ValidatedNel[String, Future[T]]) {
    def mapSuccess(
      errorCode: Int = ApiError.defaultErrorCode
    )(implicit ctx: ExecutionContext): Future[Validated[ApiError, T]] = {
      vd.fold(
        nel => Future.successful(Invalid(ApiError.fromArgs(errorCode, nel.toList))),
        future => future.map(Valid.apply)
      )
    }

    def response(jsonFunc: ApiError => String)(
      implicit ctx: ExecutionContext
    ): Future[T] = vd.fold(
      nel => {
        val err = ApiError.fromArgs(ApiError.defaultErrorCode, nel.toList)
        Future.failed(new RuntimeException(jsonFunc(err)) with NoStackTrace)
      },
      identity
    )

  }

  implicit class FutureErrorConverter[T](val f: Future[Validated[ApiError, T]]) {
    def response(jsonFunc: ApiError => String)(
      implicit ctx: ExecutionContext
    ): Future[T] = f map {
      case Valid(s) => s
      case Invalid(err) => throw new RuntimeException(jsonFunc(err)) with NoStackTrace
    }
  }

  implicit class ValidatedApiError[T](val valid: Validated[Map[String, List[String]], T]) {
    def unwrap: Either[ValidationError, T] = {
      valid.fold(
        errorMap => Left(ValidationError(errorMap.toList.map {
          case (label, errors) => ParseError(label, errors)
        })),
        Right(_)
      )
    }
  }

  implicit class ValidationNelAugmenter[T](val valid: ValidatedNel[(String, String), T]) {
    def unwrap: Either[ValidationError, T] = {
      valid.leftMap(nel => nel.toList.groupBy { case (a, b) => a } map {
        case (label, errors) => label -> errors.map { case (a, b) => b }
      }).unwrap
    }
  }


  /**
    * Augments cats validators with prop labelling.
    * @param vd The validation to augment.
    * @tparam T The underlying type of a successful validation.
    */
  implicit class CatsPropAugmenter[T](val vd: Validated[String, T]) {
    def prop(str: String): ValidatedNel[(String, String), T] = {
      vd.leftMap(f => str -> f).toValidatedNel
    }
  }

  implicit def validatedApplicative[E : Semigroup]: Applicative[Validated[E, ?]] =
    new Applicative[Validated[E, ?]] {
      def ap[A, B](f: Validated[E, A => B])(fa: Validated[E, A]): Validated[E, B] =
        (fa, f) match {
          case (Valid(a), Valid(fab)) => Valid(fab(a))
          case (i@Invalid(_), Valid(_)) => i
          case (Valid(_), i@Invalid(_)) => i
          case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
        }

      def pure[A](x: A): Validated[E, A] = Validated.valid(x)
    }

  implicit val nelSemigroup: Semigroup[NonEmptyList[(String, String)]] =
    SemigroupK[NonEmptyList].algebra[(String, String)]


  implicit class ValidatedNelAugmenter[T](val v1: Nel[T]) {
    def and[T2](v2: Nel[T2]): Wrapper2[T, T2] = Wrapper2[T, T2](v1, v2)
  }

}

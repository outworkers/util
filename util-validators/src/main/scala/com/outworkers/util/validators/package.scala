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

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.{Applicative, SemigroupK, _}

import scalaz.{Validation, ValidationNel}

package object validators extends Wrappers {

  implicit class ValidatedApiError[T](val valid: Validated[Map[String, List[String]], T]) extends AnyVal {
    def unwrap: Either[ValidationError, T] = {
      valid.fold(
        errorMap => Left(ValidationError(errorMap.toList.map {
          case (label, errors) => ParseError(label, errors)
        })),
        Right(_)
      )
    }
  }

  implicit class ValidationNelAugmenter[T](val valid: ValidatedNel[(String, String), T]) extends AnyVal {
    def unwrap: Either[ValidationError, T] = {
      valid.leftMap(nel => nel.toList.groupBy(_._1).map {
        case (label, errors) => label -> errors.map(_._2)
      }).unwrap
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

  implicit class ValidatedNelAugmenter[T](val v1: Nel[T]) extends AnyVal {
    def and[T2](v2: Nel[T2]): Wrapper2[T, T2] = new Wrapper2[T, T2](v1, v2)
  }

  implicit class ScalazToCatsValidation[X, T](val vd: Validation[X, T]) extends AnyVal {
    def cats: Validated[X, T] = vd.fold(
      fail => Invalid(fail),
      valid => Valid(valid)
    )
  }

  implicit class ScalazStringVdToCatsValidation[T](val vd: Validation[String, T]) extends AnyVal {
    def prop(str: String): ValidatedNel[(String, String), T] = {
      vd.leftMap(f => str -> f).cats.toValidatedNel
    }
  }

  implicit class ScalazToCatsValidationNel[X, T](val vd: ValidationNel[X, T]) extends AnyVal {

    def prop(str: String): ValidatedNel[(String, X), T] = {
      vd.cats.leftMap(f => NonEmptyList(str -> f.head, Nil))
    }

    def cats: ValidatedNel[X, T] = vd.fold(
      fail => Invalid(NonEmptyList(fail.head, fail.tail.toList)),
      valid => Valid(valid)
    )
  }
}

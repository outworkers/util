package com.outworkers

import cats.data.Validated.{Invalid, Valid}
import cats.{Applicative, SemigroupK, _}
import cats.data._
import com.outworkers.validators.{ParseError, ValidationError}
import cats.{Applicative, Apply, SemigroupK}
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.data.Validated.{Invalid, Valid}
import cats.std.list._

package object validators {

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
      valid.leftMap(nel => nel.unwrap.groupBy(_._1).map {
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
}

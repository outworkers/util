package com.outworkers.util.validators

import cats.data.Validated.{Invalid, Valid}
import cats.{Applicative, Semigroup, SemigroupK}
import cats.data.{NonEmptyList, Validated, ValidatedNel}

trait ValidatorImplicits extends Wrappers {

  implicit class CatsPropValidation[X, T](val vd: ValidatedNel[X, T]) {
    def prop(str: String): ValidatedNel[(String, X), T] = {
      vd.leftMap(f => NonEmptyList(str -> f.head, Nil))
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
      valid.leftMap(nel => nel.toList.groupBy(_._1).map {
        case (label, errors) => label -> errors.map(_._2)
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

package com.outworkers.util.validators

import _root_.cats.data._
import _root_.cats.data.Validated._
import _root_.cats.data.NonEmptyList
import com.outworkers.util.validators.dsl._

import scalaz.{Validation, _}

trait ScalazImplicits extends ValidatorImplicits {

  implicit class ScalazToCatsValidationNel[X, T](val vd: ValidationNel[X, T]) {

    def prop(str: String): ValidatedNel[(String, X), T] = {
      vd.cats.leftMap(f => NonEmptyList(str -> f.head, Nil))
    }

    def cats: ValidatedNel[X, T] = vd.fold(
      fail => Invalid(NonEmptyList(fail.head, fail.tail.toList)),
      valid => Valid(valid)
    )
  }

  implicit class ScalazToCatsValidation[X, T](val vd: Validation[X, T]) {
    def cats: Validated[X, T] = vd.fold(
      fail => Invalid(fail),
      valid => Valid(valid)
    )
  }


  implicit class ScalazStringVdToCatsValidation[T](val vd: Validation[String, T]){
    def prop(str: String): ValidatedNel[(String, String), T] = {
      vd.leftMap(f => str -> f).cats.toValidatedNel
    }
  }
}

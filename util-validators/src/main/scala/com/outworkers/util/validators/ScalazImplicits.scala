package com.outworkers.util.validators

import cats.data._
import com.outworkers.util.validators.cats.ValidatorImplicits

import scalaz.{Validation, _}

trait ScalazImplicits extends ValidatorImplicits {

  implicit class ValidatedNelAugmenter[T](val v1: Nel[T]) {
    def and[T2](v2: Nel[T2]): Wrapper2[T, T2] = Wrapper2[T, T2](v1, v2)
  }

  implicit class ScalazToCatsValidation[X, T](val vd: Validation[X, T]) {
    def cats: Validated[X, T] = vd.fold(
      fail => Invalid(fail),
      valid => Valid(valid)
    )
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

  implicit class ScalazStringVdToCatsValidation[T](val vd: Validation[String, T]){
    def prop(str: String): ValidatedNel[(String, String), T] = {
      vd.leftMap(f => str -> f).cats.toValidatedNel
    }
  }
}

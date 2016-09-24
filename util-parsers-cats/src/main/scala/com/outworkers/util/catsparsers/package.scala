package com.outworkers.util

import cats.data.Validated.{Invalid, Valid}
import cats.data.{ValidatedNel, NonEmptyList => NEL}

import scala.util.{Failure, Success, Try}

package object catsparsers extends DefaultParsers {

  implicit class StringToNel(val str: String) extends AnyVal {
    def invalidNel[T]: ValidatedNel[String, T] = Invalid(NEL(str, Nil))
  }

  implicit class ValidAugmenter[T](val obj: T) extends AnyVal {
    def valid: ValidatedNel[String, T] = Valid(obj)

    def invalid(msg: String = obj.toString): ValidatedNel[String, T] = Invalid(NEL(msg, Nil))
  }

  implicit class TryConverter[T](val block: Try[T]) extends AnyVal {
    def asValidation: ValidatedNel[String, T] = {
      block match {
        case Success(value) => value.valid
        case Failure(err) => err.getMessage.invalidNel[T]
      }
    }
  }

  implicit class ValidationNelConverted[String, T](val validation: ValidatedNel[String, T]) extends AnyVal {
    def asTry: Try[T] = {
      validation.fold(
        nel => Failure(new Exception(nel.toList.mkString(", "))),
        obj => Success(obj)
      )
    }
  }

}

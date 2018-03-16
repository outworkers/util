package com.outworkers.util.parsers

import cats.data.ValidatedNel
import cats.syntax.{SemigroupalSyntax, ValidatedSyntax}
import scala.util.{Failure, Success, Try}

trait CatsOps extends ValidatedSyntax with SemigroupalSyntax {

  implicit class TryConverter[T](val block: Try[T]) {
    def asValidation: ValidatedNel[String, T] = {
      block match {
        case Success(value) => value.valid
        case Failure(err) => err.getMessage.invalidNel[T]
      }
    }
  }

  implicit class ValidationNelConverted[String, T](val validation: ValidatedNel[String, T]) {
    def asTry: Try[T] = {
      validation.fold(
        nel => Failure(new Exception(nel.toList.mkString(", "))),
        obj => Success(obj)
      )
    }
  }
}

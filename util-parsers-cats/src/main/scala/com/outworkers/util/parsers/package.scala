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
import cats.data.{ValidatedNel, NonEmptyList => NEL}
import cats.syntax.CartesianSyntax

import scala.util.{Failure, Success, Try}

package object parsers extends DefaultParsers with CartesianSyntax {

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

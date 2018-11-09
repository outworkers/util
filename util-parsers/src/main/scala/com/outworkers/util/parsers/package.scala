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

import scala.util.{Success, Failure, Try}
import scalaz._
import scalaz.Scalaz._

package object parsers extends DefaultParsers {

  implicit class ValidationNelConverted[String, T](val validation: ValidationNel[String, T]) extends AnyVal {
    def asTry: Try[T] = {
      validation.fold(
        nel => Failure(new Exception(nel.list.toList.mkString(", "))),
        obj => Success(obj)
      )
    }
  }

  implicit class TryConverter[T](val block: Try[T]) extends AnyVal {
    def asValidation: ValidationNel[String, T] = {
      block match {
        case Success(value) => value.successNel[String]
        case Failure(err) => err.getMessage.failureNel[T]
      }
    }
  }
}


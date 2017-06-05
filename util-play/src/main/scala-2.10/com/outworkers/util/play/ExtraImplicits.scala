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
package com.outworkers.util.play

import _root_.play.api.data.validation.ValidationError
import _root_.play.api.libs.json.{JsPath, Json}
import com.google.common.base.Charsets
import com.outworkers.util.domain.ApiError
import play.api.libs.iteratee.Enumerator

import scala.util.control.NoStackTrace

trait ExtraImplicits {
  protected[this] final val defaultErrorCode = 400

  implicit class ApiErrorBody(val err: ApiError) {
    def body: Enumerator[Array[Byte]] = Enumerator.apply(Json.toJson(err).toString().getBytes(Charsets.UTF_8))
  }

  implicit class ParseErrorAugmenter(val errors: Seq[(JsPath, Seq[ValidationError])]) {

    def errorMessages: List[String] = errors.toList.map {
      case (path, validations) => s"${path.toJsonString} -> ${validations.map(_.message).mkString(", ")}"
    }

    def asException: Exception with NoStackTrace = {
      new RuntimeException(errorMessages.mkString(", ")) with NoStackTrace
    }

    /**
      * This will transform a list of accumulated errors to a JSON body that's usable as a response format.
      * From a non empty liust of errors this will produce something in the following format:
      *
      * {{{
      *   {
      *     "error": {
      *       "code": 400,
      *       "messages": [
      *         "this is an error message",
      *         "this is another error message
      *       ]
      *     }
      *   }
      * }}}
      *
      * @return
      */
    def apiError: ApiError = ApiError.fromArgs(defaultErrorCode, errorMessages)
  }
}
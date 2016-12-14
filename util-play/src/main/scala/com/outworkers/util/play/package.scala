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

import _root_.play.api.data.validation.ValidationError
import _root_.play.api.libs.json.{JsPath, JsValue, Json}
import _root_.play.api.mvc.{Result, Results}
import cats.data.NonEmptyList
import com.outworkers.util.domain.{ApiError, ApiErrorResponse}

import scala.concurrent.Future
import scala.util.control.NoStackTrace

package object play {

  protected[this] final val defaultErrorCode = 400

  implicit val apiErrorResponseFormat = Json.format[ApiErrorResponse]
  implicit val apiErrorFormat = Json.format[ApiError]

  implicit class NelAugmenter(val list: NonEmptyList[String]) extends AnyVal {

    def response: Result = {
      Results.BadRequest(Json.toJson(ApiError(ApiErrorResponse(defaultErrorCode, list.list.toList))))
    }

    def futureResponse(): Future[Result] = {
      Future.successful(response)
    }
  }

  implicit class ParseErrorAugmenter(val errors: Seq[(JsPath, Seq[ValidationError])]) extends AnyVal {

    def errorMessages: List[String] = errors.toList.map {
      case (path, validations) => {
        s"${path.toJsonString} -> ${validations.map(_.message).mkString(", ")}"
      }
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

    def toJson: JsValue = Json.toJson(apiError)

    def response: Result = Results.BadRequest(toJson)
  }

  def errorResponse(msg: String, code: Int = defaultErrorCode): Result = {
    Results.BadRequest(Json.toJson(ApiError.fromArgs(code, List(msg))))
  }

  implicit class ResultAugmenter(val res: Result) {
    def future: Future[Result] = {
      Future.successful(res)
    }
  }

  def malformedJson(): Result = {
    errorResponse("Malformed or missing JSON body")
  }
}

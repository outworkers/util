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

import _root_.play.api.libs.json._
import _root_.play.api.mvc.{ResponseHeader, Result, Results}
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}
import com.outworkers.util.domain.{ApiError, ApiErrorResponse}

import scala.concurrent.Future

package object play extends ExtraImplicits {

  implicit lazy val apiErrorResponseFormat = Json.format[ApiErrorResponse]
  implicit lazy val apiErrorFormat = Json.format[ApiError]

  implicit class JsonHelpers[T](val obj: T) extends AnyVal {
    def jsValue()(implicit fmt: Writes[T]): JsValue = Json.toJson(obj)

    def json()(implicit fmt: Writes[T]): String = jsValue.toString()
  }

  implicit class CatsHelpers[T](val obj: T) extends AnyVal {
    def valid: Valid[T] = Valid(obj)

    def invalid: Invalid[T] = Invalid(obj)

    def invalidNel: Invalid[NonEmptyList[T]] = Invalid(NonEmptyList(obj, Nil))
  }

  implicit class NelAugmenter(val list: NonEmptyList[String]) extends AnyVal {

    def response: Result = {
      Results.BadRequest(
        Json.toJson(list.toError(defaultErrorCode))
      )
    }

    def futureResponse(): Future[Result] = Future.successful(response)
  }

  implicit class ResponseConverter(val resp: NonEmptyList[String]) extends AnyVal {

    def toError(code: Int): ApiError = ApiError.fromArgs(code, resp.list.toList)

    def asResponse(code: Int = defaultErrorCode): Result = {
      Result(
        header = ResponseHeader(code),
        body = toError(code).body
      )
    }
  }

  implicit class ValidationResponseHelper[+A](val vd: ValidatedNel[String, A]) extends AnyVal {

    /**
      * Maps a validation to a LiftResponse if the validation is successful.
      * If the validation is not successful, this method provides a default response mechanism
      * which returns a JSON HTTP 400 response, where the body is an object containing the error code
      * and a list of messages corresponding to each individual error in the applicative functor.
      *
      * @param pf The partial function that maps the successful result to a LiftResponse.
      * @return A future wrapping a Lift Response.
      */
    def mapSuccess(pf: A => Future[Result]): Future[Result] = vd.fold(_.asResponse().future, pf)

    /**
      * Maps a validation to a LiftResponse if the validation is successful.
      * If the validation is not successful, this method provides a default response mechanism
      * which returns a JSON HTTP 400 response, where the body is an object containing the error code
      * and a list of messages corresponding to each individual error in the applicative functor.
      *
      * @param pf The partial function that maps the successful result to a LiftResponse.
      * @return A future wrapping a Lift Response.
      */
    def respond(pf: A => Result): Result = vd.fold(_.asResponse(), pf)
  }


  def errorResponse(msg: String, code: Int = defaultErrorCode): Result = {
    Results.BadRequest(Json.toJson(ApiError.fromArgs(code, List(msg))))
  }

  implicit class ResultAugmenter(val res: Result) extends AnyVal {
    def future: Future[Result] = Future.successful(res)
  }

  def malformedJson(): Result = errorResponse("Malformed or missing JSON body")
}

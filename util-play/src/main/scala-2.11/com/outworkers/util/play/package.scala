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
import _root_.play.api.libs.json.{ JsPath, JsValue, Json, JsonValidationError, OFormat }
import _root_.play.api.mvc.{Result, Results}
import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, ValidatedNel}
import com.outworkers.util.domain.{ApiError, ApiErrorResponse}

import scala.concurrent.Future
import scala.util.control.NoStackTrace

package object play {

  protected[this] final val defaultErrorCode = 400
  protected[this] final val defaultErrorResponse = 400

  implicit val apiErrorFormat: OFormat[ApiError] = Json.format[ApiError]

  implicit val apiErrorResponseFormat = Json.format[ApiErrorResponse]

  implicit class CatsHelpers[T](val obj: T) extends AnyVal {
    def valid: Valid[T] = Valid(obj)

    def invalid: Invalid[T] = Invalid(obj)

    def invalidNel: Invalid[NonEmptyList[T]] = Invalid(NonEmptyList(obj, Nil))
  }

  implicit class NelAugmenter(val list: NonEmptyList[String]) extends AnyVal {

    def response: Result = {
      Results.BadRequest(Json.toJson(ApiError(ApiErrorResponse(defaultErrorCode, list.toList))))
    }

    def futureResponse(): Future[Result] = Future.successful(response)
  }

  trait ValidationMessage[T] {
    def messages(source: T): Seq[String]

    def message(source: T): String
  }

  object ValidationMessage {

    def apply[T](implicit ev: ValidationMessage[T]): ValidationMessage[T] = ev

    implicit object JsonValidationErrorMessage extends ValidationMessage[JsonValidationError] {
      override def message(source: JsonValidationError): String = source.message

      override def messages(source: JsonValidationError): Seq[String] = source.messages
    }

    implicit object ValidationErrorMessage extends ValidationMessage[ValidationError] {
      override def message(source: ValidationError): String = source.message

      override def messages(source: ValidationError): Seq[String] = source.messages
    }
  }

  implicit class ResponseConverter(val resp: NonEmptyList[String]) extends AnyVal {

    def toError(code: Int): ApiError = ApiError.fromArgs(code, resp.list.toList)

    def toJson(code: Int = defaultErrorResponse): Result = {
      Results.Ok(Json.toJson(toError(code)))
    }

    def asResponse(code: Int = defaultErrorResponse): Result = {
      Results.Ok(Json.toJson(toError(code)))
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
    def mapSuccess(pf: A => Future[Result]): Future[Result] = vd.fold(_.toJson().future, pf)

    /**
      * Maps a validation to a LiftResponse if the validation is successful.
      * If the validation is not successful, this method provides a default response mechanism
      * which returns a JSON HTTP 400 response, where the body is an object containing the error code
      * and a list of messages corresponding to each individual error in the applicative functor.
      *
      * @param pf The partial function that maps the successful result to a LiftResponse.
      * @return A future wrapping a Lift Response.
      */
    def respond(pf: A => Result): Result = vd.fold(_.toJson(), pf)
  }

  implicit class ParseDataErrorAugmenter[Source](
    val errors: Seq[(JsPath, Seq[Source])]
  ) extends AnyVal {

    def errorMessages()(implicit ev: ValidationMessage[Source]): Seq[String] = errors.map {
      case (path, vds) =>
        s"${path.toJsonString} -> ${vds.map(ValidationMessage[Source].messages(_).mkString(", "))}"
    }

    def asException()(implicit ev: ValidationMessage[Source]): Exception with NoStackTrace = {
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
    def apiError()(implicit ev: ValidationMessage[Source]): ApiError = ApiError.fromArgs(defaultErrorCode, errorMessages)

    def toJson()(implicit ev: ValidationMessage[Source]): JsValue = Json.toJson(apiError)

    def response()(implicit ev: ValidationMessage[Source]): Result = Results.BadRequest(toJson)
  }

  def errorResponse(msg: String, code: Int = defaultErrorCode): Result = {
    Results.BadRequest(Json.toJson(ApiError.fromArgs(code, List(msg))))
  }

  implicit class ResultAugmenter(val res: Result) extends AnyVal {
    def future: Future[Result] = Future.successful(res)
  }

  def malformedJson(): Result = errorResponse("Malformed or missing JSON body")
}

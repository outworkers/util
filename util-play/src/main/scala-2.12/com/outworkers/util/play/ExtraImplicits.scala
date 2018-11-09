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

import _root_.play.api.http.{ HttpEntity, MimeTypes }
import _root_.play.api.data.validation.ValidationError
import _root_.play.api.libs.json.{JsPath, JsValue, Json, JsonValidationError}
import _root_.play.api.mvc.{Result, Results}
import akka.util.ByteString
import com.google.common.base.Charsets
import com.outworkers.util.domain.ApiError
import play.api.http.MimeTypes

import scala.util.control.NoStackTrace

trait ExtraImplicits {

  protected[this] final val defaultErrorCode = 400

  implicit class ApiErrorBody(err: ApiError) {
    def body: HttpEntity.Strict = HttpEntity.Strict(
      ByteString(
        Json.toJson(err).toString.getBytes(Charsets.UTF_8)
      ),
      Some(MimeTypes.JSON)
    )
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

  implicit class ParseDataErrorAugmenter[Source](
    val errors: Seq[(JsPath, Seq[Source])]
  ) extends {

    def errorMessages()(implicit ev: ValidationMessage[Source]): Seq[String] = errors.map {
      case (path, vds) =>
        s"${path.toJsonString} -> ${vds.map(ValidationMessage[Source].messages(_).mkString(", "))}"
    }

    def asException()(implicit ev: ValidationMessage[Source]): Exception with NoStackTrace = {
      new RuntimeException(errorMessages.mkString(", ")) with NoStackTrace
    }

    /**
      * This will transform a list of accumulated errors to a JSON body that's usable as a response format.
      * From a non empty list of errors this will produce something in the following format:
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
    def apiError()(implicit ev: ValidationMessage[Source]): ApiError = {
      ApiError.fromArgs(defaultErrorCode, errorMessages)
    }

    def toJson()(implicit ev: ValidationMessage[Source]): JsValue = Json.toJson(apiError)

    def response()(implicit ev: ValidationMessage[Source]): Result = Results.BadRequest(toJson)
  }
}
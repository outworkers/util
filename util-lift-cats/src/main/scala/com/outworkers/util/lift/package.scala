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

import cats.data.{NonEmptyList, ValidatedNel}
import cats.syntax.CartesianSyntax
import com.outworkers.util.domain.ApiError
import com.outworkers.util.parsers._
import net.liftweb.http.rest.RestContinuation
import net.liftweb.http.{JsonResponse, LiftResponse}
import net.liftweb.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

package object lift extends LiftParsers with JsonHelpers with CatsOps with CartesianSyntax {

  protected[this] val defaultSuccessResponse = 200
  protected[this] val noContentSuccessResponse = 204
  protected[this] val defaultErrorResponse = 400
  protected[this] val failureResponse = 500

  implicit class OptionResponseHelper[T](val opt: Option[T]) extends AnyVal {

    /**
     * When the Option is full, this will continue the transformation flow of an Option to a LiftResponse.
     * Otherwise, the flow will short-circuit to a an unauthorized response.
      *
      * @param pf A partial function from a full option of type T to an async LiftResponse.
     * @return A Future wrapping the obtained LiftResponse.
     */
    def required(pf: T => Future[LiftResponse]): Future[LiftResponse] = {
      opt.fold(Future.successful(JsonUnauthorizedResponse()))(pf)
    }
  }

  implicit class FutureOptionTransformer[T <: Product with Serializable](val future: Future[Option[T]]) extends AnyVal {

    def json()(implicit ec: ExecutionContext, formats: Formats, mf: Manifest[T]): Future[LiftResponse] = {
      future map { item =>
        item.fold(JsonUnauthorizedResponse())(s => JsonResponse(s.asJValue(), defaultSuccessResponse))
      }
    }
  }

  implicit class ResponseToFuture(val response: LiftResponse) extends AnyVal {
    def toFuture: Future[LiftResponse] = Future.successful(response)

    def future(): Future[LiftResponse] = Future.successful(response)
  }

  implicit class ResponseConverter(val resp: NonEmptyList[String]) extends AnyVal {

    def toError(code: Int): ApiError = ApiError.fromArgs(code, resp.toList)

    def toJson(code: Int = defaultErrorResponse)(implicit formats: Formats): LiftResponse = {
      JsonResponse(Extraction.decompose(toError(code)), code)
    }

    def asResponse(code: Int = defaultErrorResponse)(implicit formats: Formats): LiftResponse = {
      JsonResponse(Extraction.decompose(toError(code), code))
    }
  }

  implicit class ErrorConverter(val err: Throwable) extends AnyVal {

    def toError(code: Int): ApiError = ApiError.fromArgs(code, List(err.getMessage))

    def toJson(code: Int)(implicit formats: Formats): LiftResponse = JsonResponse(Extraction.decompose(toError(code)), code)
  }

  implicit class JsonHelper[T <: Product with Serializable](val clz: T) extends AnyVal {
    def asJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      compactRender(Extraction.decompose(clz))
    }

    def asPrettyJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      JsonWrapper.prettyRender(Extraction.decompose(clz))
    }

    def asJValue()(implicit formats: Formats, manifest: Manifest[T]): JValue = {
      Extraction.decompose(clz)
    }

    def asResponse()(implicit mf: Manifest[T], formats: Formats): LiftResponse = {
      JsonResponse(clz.asJValue(), defaultSuccessResponse)
    }
  }

  implicit class JsonSeqHelper[T <: Product with Serializable](val list: Seq[T]) extends AnyVal {
    def asJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      compactRender(Extraction.decompose(list))
    }

    def asPrettyJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      JsonWrapper.prettyRender(Extraction.decompose(list))
    }

    def asJValue()(implicit formats: Formats, manifest: Manifest[T]): JValue = {
      Extraction.decompose(list)
    }

    def asResponse()(implicit mf: Manifest[T], formats: Formats): LiftResponse = {
      if (list.nonEmpty) {
        JsonResponse(list.asJValue(), defaultSuccessResponse)
      } else {
        JsonResponse(JArray(Nil), noContentSuccessResponse)
      }
    }
  }

  implicit class JsonSetHelper[T <: Product with Serializable](val set: Set[T]) extends AnyVal {
    def asJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      compactRender(Extraction.decompose(set))
    }

    def asPrettyJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      JsonWrapper.prettyRender(Extraction.decompose(set))
    }

    def asJValue()(implicit formats: Formats, manifest: Manifest[T]): JValue = {
      Extraction.decompose(set)
    }

    def asResponse()(implicit mf: Manifest[T], formats: Formats): LiftResponse = {
      if (set.nonEmpty) {
        JsonResponse(set.asJValue(), defaultSuccessResponse)
      } else {
        JsonResponse(JArray(Nil), noContentSuccessResponse)
      }
    }
  }

  implicit class FutureResponseHelper(val responseFuture: Future[LiftResponse]) extends AnyVal {

    def async(failureCode: Int = failureResponse)(implicit context: ExecutionContext): LiftResponse = {
      RestContinuation.async {
        reply => {
          responseFuture.onComplete {
            case Success(resp) => reply(resp)
            case Failure(err) => reply(err.toJson(failureCode))
          }
        }
      }
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
    def mapSuccess(pf: A => Future[LiftResponse]): Future[LiftResponse] = vd.fold(_.toJson().future(), pf)

    /**
      * Maps a validation to a LiftResponse if the validation is successful.
      * If the validation is not successful, this method provides a default response mechanism
      * which returns a JSON HTTP 400 response, where the body is an object containing the error code
      * and a list of messages corresponding to each individual error in the applicative functor.
      *
      * @param pf The partial function that maps the successful result to a LiftResponse.
      * @return A future wrapping a Lift Response.
      */
    def respond(pf: A => LiftResponse): LiftResponse = vd.fold(_.toJson(), pf)
  }
}

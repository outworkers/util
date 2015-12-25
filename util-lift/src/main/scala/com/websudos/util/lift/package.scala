/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.util

import net.liftweb.http.rest.RestContinuation
import net.liftweb.http.{JsonResponse, LiftResponse}
import net.liftweb.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scalaz.{NonEmptyList, ValidationNel}

package object lift extends LiftParsers with JsonHelpers {


  implicit class OptionResponseHelper[T](val opt: Option[T]) extends AnyVal {

    /**
     * When the Option is full, this will continue the transformation flow of an Option to a LiftResponse.
     * Otherwise, the flow will short-circuit to a an unauthorized response.
     * @param pf A partial function from a full option of type T to an async LiftResponse.
     * @return A Future wrapping the obtained LiftResponse.
     */
    def required(pf: T => Future[LiftResponse]): Future[LiftResponse] = {
      opt.fold(Future.successful(JsonUnauthorizedResponse()))(pf)
    }
  }

  implicit class FutureOptionTransformer[T <: Product with Serializable](val future: Future[Option[T]]) extends AnyVal {

    def json()(implicit ec: ExecutionContext, formats: Formats, mf: Manifest[T]): Future[LiftResponse] = {
      future map {
        item => item.fold(JsonUnauthorizedResponse())(item => JsonResponse(item.asJValue(), 200))
      }
    }
  }

  implicit class ResponseToFuture(val response: LiftResponse) extends AnyVal {
    def toFuture(): Future[LiftResponse] = Future.successful(response)

    def future(): Future[LiftResponse] = Future.successful(response)
  }

  implicit class ResponseConverter(val resp: NonEmptyList[String]) extends AnyVal {

    def toError(code: Int): ApiError = ApiError(ApiErrorResponse(code, resp.list))

    def toJson(code: Int = 406)(implicit formats: Formats): LiftResponse = JsonResponse(Extraction.decompose(toError(code)), code)

    def asResponse(code: Int = 406)(implicit formats: Formats): LiftResponse = {
      JsonResponse(Extraction.decompose(toError(code), 406))
    }
  }

  implicit class ErrorConverter(val err: Throwable) extends AnyVal {

    def toError(code: Int): ApiError = ApiError(ApiErrorResponse(code, List(err.getMessage)))

    def toJson(code: Int)(implicit formats: Formats): LiftResponse = JsonResponse(Extraction.decompose(toError(code)), code)
  }

  implicit class JsonHelper[T <: Product with Serializable](val clz: T) extends AnyVal {
    def asJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      compactRender(Extraction.decompose(clz))
    }

    def asJValue()(implicit formats: Formats, manifest: Manifest[T]): JValue = {
      Extraction.decompose(clz)
    }

    def asResponse()(implicit mf: Manifest[T], formats: Formats): LiftResponse = {
      JsonResponse(clz.asJValue(), 200)
    }
  }

  implicit class JsonSeqHelper[T <: Product with Serializable](val list: Seq[T]) extends AnyVal {
    def asJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      compactRender(Extraction.decompose(list))
    }

    def asJValue()(implicit formats: Formats, manifest: Manifest[T]): JValue = {
      Extraction.decompose(list)
    }

    def asResponse()(implicit mf: Manifest[T], formats: Formats): LiftResponse = {
      if (list.nonEmpty) {
        JsonResponse(list.asJValue(), 200)
      } else {
        JsonResponse(JArray(Nil), 204)
      }
    }
  }

  implicit class JsonSetHelper[T <: Product with Serializable](val set: Set[T]) extends AnyVal {
    def asJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      compactRender(Extraction.decompose(set))
    }

    def asJValue()(implicit formats: Formats, manifest: Manifest[T]): JValue = {
      Extraction.decompose(set)
    }

    def asResponse()(implicit mf: Manifest[T], formats: Formats): LiftResponse = {
      if (set.nonEmpty) {
        JsonResponse(set.asJValue(), 200)
      } else {
        JsonResponse(JArray(Nil), 204)
      }
    }
  }

  implicit class JsonListHelper[T <: Product with Serializable](val list: List[T]) extends AnyVal {
    def asJson()(implicit formats: Formats, manifest: Manifest[T]): String = {
      compactRender(Extraction.decompose(list))
    }

    def asJValue()(implicit formats: Formats, manifest: Manifest[T]): JValue = {
      Extraction.decompose(list)
    }

    def asResponse()(implicit mf: Manifest[T], formats: Formats): LiftResponse = {
      list match {
        case head :: tail => JsonResponse(list.asJValue(), 200)
        case Nil => JsonResponse(JArray(Nil), 204)
      }
    }
  }

  implicit class FutureResponseHelper(val responseFuture: Future[LiftResponse]) extends AnyVal {

    def async(failureCode: Int = 500)(implicit context: ExecutionContext): LiftResponse = {
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

  implicit class ValidationResponseHelper[+A](val eval: ValidationNel[String, A]) extends AnyVal {
    def respond(pf: A => LiftResponse)(code: Int = 400): LiftResponse = {
      eval.fold(_.toJson(code), pf)
    }


    /**
     * Maps a validation to a LiftResponse if the validation is successful.
     * If the validation is not successful, this method provides a default response mechanism
     * which returns a JSON HTTP 400 response, where the body is an object containing the error code
     * and a list of messages corresponding to each individual error in the applicative functor.
     *
     * @param pf The partial function that maps the successful result to a LiftResponse.
     * @param code The error status code to use if the validation is a failure.
     * @return A future wrapping a Lift Response.
     */
    @deprecated("Use mapSuccess instead", "0.9.11")
    def async(pf: A => Future[LiftResponse])(code: Int = 400): Future[LiftResponse] = {
      eval.fold(_.toJson(code).toFuture(), pf)
    }

    def mapSuccess(pf: A => Future[LiftResponse]): Future[LiftResponse] = {
      eval.fold(_.toJson().toFuture(), pf)
    }

    def respond(pf: A => LiftResponse): LiftResponse = {
      eval.fold(_.toJson(), pf)
    }

    @deprecated("Use mapSuccess instead", "0.9.11")
    def async(pf: A => Future[LiftResponse]): Future[LiftResponse] = {
      eval.fold(_.toJson().toFuture(), pf)
    }
  }
}

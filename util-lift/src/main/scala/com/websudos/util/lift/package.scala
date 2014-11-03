package com.websudos.util

import scalaz.NonEmptyList

import net.liftweb.http.{JsonResponse, LiftResponse}
import net.liftweb.json.{DefaultFormats, Extraction}

package object lift extends LiftParsers with JsonHelpers {

  implicit class ResponseConverter(val resp: NonEmptyList[String]) extends AnyVal {

    def toError(code: Int): ApiError = ApiError(ApiErrorResponse(code, resp.list))

    def toJson(code: Int = 406): LiftResponse = JsonResponse(Extraction.decompose(toError(code))(DefaultFormats), code)
  }

  implicit class ErrorConverter(val err: Throwable) extends AnyVal {

    def toError(code: Int): ApiError = ApiError(ApiErrorResponse(code, List(err.getMessage)))

    def toJson(code: Int): LiftResponse = JsonResponse(Extraction.decompose(toError(code))(DefaultFormats))
  }
}

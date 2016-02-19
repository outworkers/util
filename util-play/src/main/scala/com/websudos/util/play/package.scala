package com.websudos.util

import _root_.play.api.data.validation.ValidationError
import _root_.play.api.libs.json.{JsValue, JsPath, Json}
import _root_.play.api.mvc.{Results, Result}
import com.websudos.util.domain.{ApiErrorResponse, ApiError}

import scala.concurrent.Future
import scalaz.NonEmptyList

package object play {

  protected[this] final val defaultErrorCode = 400

  implicit val apiErrorResponseFormat = Json.format[ApiErrorResponse]
  implicit val apiErrorFormat = Json.format[ApiError]

  implicit class NelAugmenter(val list: NonEmptyList[String]) extends AnyVal {

    def response: Result = {
      Results.BadRequest(Json.toJson(ApiError(ApiErrorResponse(defaultErrorCode, list.list))))
    }

    def futureResponse(): Future[Result] = {
      Future.successful(response)
    }
  }

  implicit class ParseErrorConverter(val errors: Seq[(JsPath, Seq[ValidationError])]) extends AnyVal {

    def apiError: ApiError = {
      ApiError(ApiErrorResponse(defaultErrorCode, errors.toList.map {
        case (path, validations) => {
          s"${path.toJsonString} -> ${validations.map(_.message).mkString(", ")}"
        }
      }))
    }

    def toJson: JsValue = {
      Json.toJson(apiError)
    }

    def response: Result = {
      Results.BadRequest(toJson)
    }
  }

  def errorResponse(msg: String, code: Int = defaultErrorCode): Result = {
    Results.BadRequest(Json.toJson(ApiError(code, List(msg))))
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

package com.websudos.util.domain

case class ApiErrorResponse(
  code: Int,
  messages: List[String]
)

case class ApiError(error: ApiErrorResponse)

object ApiError {
  def apply(code: Int, messages: List[String]): ApiError = {
    ApiError(ApiErrorResponse(code, messages))
  }
}

package com.outworkers.validators

case class ParseError(label: String, errors: List[String])

case class ValidationError(
  errors: List[ParseError]
)

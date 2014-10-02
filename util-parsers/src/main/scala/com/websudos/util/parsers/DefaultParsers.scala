package com.websudos.util.parsers

import java.util.UUID

import scala.util.Try
import scalaz.Scalaz._
import scalaz._

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter


trait DefaultParsers extends LowPriorityImplicits {

  final def uuid(str: String): ValidationNel[String, UUID] = {
    Try(UUID.fromString(str))
      .toOption.fold(s"Couldn't parse an UUID from string $str".failureNel[UUID])(_.successNel[String])
  }

  final def timestamp(str: String): ValidationNel[String, DateTime] = {
    Try(new DateTime(str.toLong))
      .toOption
      .fold(s"Couldn't not parse a timestamp from $str.".failureNel[DateTime])(_.successNel[String])
  }


  final def date(str: String, format: DateTimeFormatter): ValidationNel[String, DateTime] = {
    Try {
      format.parseDateTime(str)
    }.toOption
      .fold(s"Couldn't not parse a date from $str.".failureNel[DateTime])(_.successNel[String])
  }

  final def int(str: String): ValidationNel[String, Int] = {
    Try {
      str.toInt
    }.toOption.fold(
      s"Couldn't parse an Int from string $str".failureNel[Int]
    )(_.successNel[String])
  }

  final def float(str: String): ValidationNel[String, Float] = {
    Try {
      str.toFloat
    }.toOption.fold(
        s"Couldn't parse a Float from string $str".failureNel[Float]
      )(_.successNel[String])
  }

  final def double(str: String): ValidationNel[String, Double] = {
    Try {
      str.toDouble
    }.toOption.fold (
      s"Couldn't parse a Double from string $str".failureNel[Double]
    )(_.successNel[String])
  }

  final def long(str: String): ValidationNel[String, Long] = {
    Try {
      str.toLong
    }.toOption.fold (
      s"Couldn't parse a Long from string $str".failureNel[Long]
    )(_.successNel[String])
  }

}

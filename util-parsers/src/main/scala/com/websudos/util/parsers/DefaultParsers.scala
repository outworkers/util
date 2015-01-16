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
package com.websudos.util.parsers

import java.util.UUID

import scala.util.Try
import scalaz.Scalaz._
import scalaz._
import org.apache.commons.validator.routines.EmailValidator
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter


trait DefaultParsers {

  final def optional[T](str: Option[String])(f: String => ValidationNel[String, T]) = {
    str.fold(Option.empty[T].successNel[String]) { s =>
      f(s).map(Some(_))
    }
  }


  private[util] final def parseRequired[T](str: Option[String])(f: String => ValidationNel[String, T]) = {
    str.fold(s"Couldn't parse $str from None".failureNel[T])(f)
  }

  final def required[T](opt: Option[T]): ValidationNel[String, T] = {
    opt.fold("Required parameter is empty".failureNel[T])(_.successNel[String])
  }

  final def uuidOpt(str: String): Option[UUID] = {
    Try(UUID.fromString(str)).toOption
  }

  final def uuid(str: String): ValidationNel[String, UUID] = {
    uuidOpt(str)
      .fold(s"Couldn't parse an UUID from string $str".failureNel[UUID])(_.successNel[String])
  }

  final def uuid(str: Option[String]): ValidationNel[String, UUID] = {
    parseRequired(str)(uuid)
  }

  final def timestampOpt(str: String): Option[DateTime] = {
    Try(new DateTime(str.toLong)).toOption
  }

  final def timestamp(str: String): ValidationNel[String, DateTime] = {
    timestampOpt(str)
      .fold(s"Couldn't not parse a timestamp from $str.".failureNel[DateTime])(_.successNel[String])
  }

  final def timestamp(str: Option[String]): ValidationNel[String, DateTime] = {
    parseRequired(str)(timestamp)
  }

  final def dateOpt(str: String, format: DateTimeFormatter): Option[DateTime] = {
    Try(format.parseDateTime(str)).toOption
  }

  final def date(str: String, format: DateTimeFormatter): ValidationNel[String, DateTime] = {
    dateOpt(str, format)
      .fold(s"Couldn't not parse a date from $str.".failureNel[DateTime])(_.successNel[String])
  }

  final def date(str: Option[String], format: DateTimeFormatter): ValidationNel[String, DateTime] = {
    parseRequired(str)(x => date(x, format))
  }

  final def intOpt(str: String): Option[Int] = {
    Try(str.toInt).toOption
  }

  final def int(str: String): ValidationNel[String, Int] = {
    intOpt(str).fold(
      s"Couldn't parse an Int from string $str".failureNel[Int]
    )(_.successNel[String])
  }

  final def int(str: Option[String]): ValidationNel[String, Int] = {
    parseRequired(str)(int)
  }

  final def floatOpt(str: String): Option[Float] = {
    Try(str.toFloat).toOption
  }

  final def float(str: String): ValidationNel[String, Float] = {
    floatOpt(str).fold(
        s"Couldn't parse a Float from string $str".failureNel[Float]
      )(_.successNel[String])
  }

  final def float(str: Option[String]): ValidationNel[String, Float] = {
    parseRequired(str)(float)
  }

  final def doubleOpt(str: String): Option[Double] = {
    Try(str.toDouble).toOption
  }

  final def double(str: String): ValidationNel[String, Double] = {
    doubleOpt(str).fold (
      s"Couldn't parse a Double from string $str".failureNel[Double]
    )(_.successNel[String])
  }

  final def double(str: Option[String]): ValidationNel[String, Double] = {
    parseRequired(str)(double)
  }

  final def longOpt(str: String): Option[Long] = {
    Try(str.toLong).toOption
  }

  final def long(str: String): ValidationNel[String, Long] = {
    longOpt(str).fold(
      s"Couldn't parse a Long from string $str".failureNel[Long]
    )(_.successNel[String])
  }

  final def long(str: Option[String]): ValidationNel[String, Long] = {
    parseRequired(str)(long)
  }

  final def emailOpt(str: String): Option[String] = {
    if (EmailValidator.getInstance().isValid(str)) {
      Some(str)
    } else {
      None
    }
  }

  final def email(str: String): ValidationNel[String, String] = {
    emailOpt(str)
      .fold(s"Invalid email address $str".failureNel[String])(_.successNel[String])
  }

  final def email(str: Option[String]): ValidationNel[String, String] = {
    parseRequired(str)(email)
  }

  final def present(str: String, name: String): ValidationNel[String, String] = {
    if (str.trim.length == 0) {
      s"$name is empty".failureNel[String]
    } else {
      str.successNel[String]
    }
  }

  final def confirm(first: String, second: String): ValidationNel[String, String] = {
    if (first != second) {
      s"Strings $first and $second don't match".failureNel[String]
    } else {
      first.successNel[String]
    }
  }
}

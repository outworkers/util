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
package com.outworkers.util.lift

import com.outworkers.util.parsers._
import net.liftweb.common.Box
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json._

import scala.util.control.NonFatal
import cats.data.{ NonEmptyList, ValidatedNel }

trait LiftParsers extends DefaultParsers {

  implicit val formats = DefaultFormats

  final def json[T](str: String)(
    implicit mf: Manifest[T],
    formats: Formats
  ): ValidatedNel[String, T] = {
    try {
      JsonParser.parse(str).extract[T].successNel[String]
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
  }

  final def json[T](str: Option[String])(
    implicit mf: Manifest[T],
    formats: Formats
  ): ValidatedNel[String, T] = {
    try {
      str.map(JsonParser.parse(_)
        .extract[T]
        .successNel[String])
        .getOrElse("Missing required parameter".failureNel[T])
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
  }


  final def json[T](str: JValue)(
    implicit mf: Manifest[T],
    formats: Formats
  ): ValidatedNel[String, T] = {
    try {
      str.extract[T].successNel[String]
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
  }

  final def jsonOpt[T](str: JValue)(implicit mf: Manifest[T], formats: Formats): Option[T] = {
    str.extractOpt[T]
  }

  final def required[T](box: Box[T]): ValidatedNel[String, T] = {
    box.map(_.successNel[String])
      .getOrElse("Required parameter is missing or empty".failureNel[T])
  }
}

trait JsonHelpers {

  def toJson[T <: Product with Serializable](obj: T)(implicit formats: Formats): String = {
    compactRender(Extraction.decompose(obj))
  }
}

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
package com.websudos.util.lift

import com.websudos.util.parsers.DefaultParsers
import net.liftweb.common.Box
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{DefaultFormats, Extraction, Formats, JsonParser, compactRender}

import scala.util.control.NonFatal
import scalaz.Scalaz._
import scalaz._


trait LiftParsers extends DefaultParsers {

  implicit val formats = DefaultFormats

  final def json[T](str: String)(implicit mf: Manifest[T], formats: Formats): ValidationNel[String, T] = {
    try {
      JsonParser.parse(str).extract[T].successNel[String]
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
  }

  final def json[T](str: Option[String])(implicit mf: Manifest[T], formats: Formats): ValidationNel[String, T] = {
    try {
      str.map(JsonParser.parse(_).extract[T].successNel[String]).getOrElse("Missing required parameter".failureNel[T])
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
  }


  final def json[T](str: JValue)(implicit mf: Manifest[T], formats: Formats): ValidationNel[String, T] = {
    try {
      str.extract[T].successNel[String]
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
  }

  final def jsonOpt[T](str: JValue)(implicit mf: Manifest[T], formats: Formats): Option[T] = {
    str.extractOpt[T]
  }

  final def required[T](box: Box[T]): ValidationNel[String, T] = {
    box.map(_.successNel[String])
      .getOrElse("Required parameter is missing or empty".failureNel[T])
  }
}

trait JsonHelpers {

  def toJson[T <: Product with Serializable](obj: T)(implicit formats: Formats): String = {
    compactRender(Extraction.decompose(obj))
  }
}

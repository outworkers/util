package com.websudos.util.lift

import scalaz._
import scalaz.Scalaz._
import scala.util.control.NonFatal
import com.websudos.util.parsers.DefaultParsers
import net.liftweb.common.Box
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{compactRender, Formats, Extraction, DefaultFormats, JsonParser}


trait LiftParsers extends DefaultParsers {

  implicit val formats = DefaultFormats

  final def json[T : Manifest](str: String): ValidationNel[String, T] = {
    try {
      JsonParser.parse(str).extract[T].successNel[String]
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
  }

  final def json[T: Manifest](str: Option[String]): ValidationNel[String, T] = {
    try {
      str.map(JsonParser.parse(_).extract[T].successNel[String]).getOrElse("Missing required parameter".failureNel[T])
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
  }


  final def json[T : Manifest](str: JValue): ValidationNel[String, T] = {
    try {
      str.extract[T].successNel[String]
    } catch {
      case NonFatal(e) => e.getMessage.failureNel[T]
    }
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

package com.websudos.util.lift

import scalaz._
import scalaz.Scalaz._
import scala.util.control.NonFatal
import com.websudos.util.parsers.DefaultParsers
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{DefaultFormats, JsonParser}

trait LiftParsers extends DefaultParsers {

  implicit val formats = DefaultFormats

  final def json[T : Manifest](str: String): ValidationNel[String, T] = {
    try {
      JsonParser.parse(str).extract[T].successNel[String]
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

}

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
package com.outworkers.util.parsers

import java.net.URL
import java.util.UUID

import com.outworkers.util.domain.GenerationDomain
import org.apache.commons.validator.routines.EmailValidator
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.{Failure, Success, Try}
import scalaz.Scalaz._
import scalaz.{Success => _, _}

sealed trait BaseParser[X, T] {

  type Out = ValidationNel[String, T]

  final def optional(str: Option[X])(f: X => ValidationNel[String, T]): ValidationNel[String, Option[T]] = {
    str.fold(Option.empty[T].successNel[String]) { s =>
      f(s).map(Some(_))
    }
  }

  private[util] def missing : ValidationNel[String, T] = {
    "Required option expected to be Some, found None instead".failureNel[T]
  }

  private[util] final def parseRequired(str: Option[X])(f: X => ValidationNel[String, T]) = {
    str.fold(s"Couldn't parse $str from None".failureNel[T])(f)
  }

  final def required(opt: Option[T]): ValidationNel[String, T] = {
    opt.fold("Required parameter is empty".failureNel[T])(_.successNel[String])
  }

  def tryParse(obj: X): Try[T] = parse(obj).asTry

  /**
   * A basic way to parse known types from options.
    *
    * @param str The string to attempt to parse from.
   * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
   */
  def parseOpt(str: X): Option[T] = tryParse(str).toOption

  def parse(str: X): ValidationNel[String, T]

  def parse(str: Option[X]): ValidationNel[String, T] = {
    parseRequired(str)(s => parse(s))
  }

  def parseIfExists(str: Option[X]): ValidationNel[String, Option[T]] = {
    optional(str)(parse)
  }
}

trait BiParser[X, T] extends BaseParser[X, T]

trait Parser[T] extends BaseParser[String, T]

private[util] trait DefaultImplicitParsers extends GenerationDomain {

  implicit object UUIDParser extends Parser[UUID] {
    override def parse(str: String): ValidationNel[String, UUID] = {
      Try(UUID.fromString(str)).asValidation
    }
  }

  implicit object BooleanParser extends Parser[Boolean] {

    override def parse(str: String): ValidationNel[String, Boolean] = {
      val obj = str match {
        case "true" => Success(true)
        case "false" => Success(false)
        case _ => Failure(new Exception("A boolean parser will only parse the tings 'true' and 'false'"))
      }

      obj.asValidation
    }

  }

  implicit object TimestampParser extends Parser[DateTime] {
    /**
     * A basic way to parse known types from options.
      *
      * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def parse(str: String): ValidationNel[String, DateTime] = {
      Try(new DateTime(str.toLong, DateTimeZone.UTC)).asValidation
    }
}

  implicit object IntParser extends Parser[Int] {
    /**
     * A basic way to parse known types from options.
      *
      * @param str The string to attempt to parse from.
     * @return An ValidationNel wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def parse(str: String): ValidationNel[String, Int] = {
      Try(str.toInt).asValidation
    }
}

  implicit object DoubleParser extends Parser[Double] {
    /**
     * A basic way to parse known types from options.
 *
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def parse(str: String): ValidationNel[String, Double] = {
      Try(str.toDouble).asValidation
    }
}

  implicit object FloatParser extends Parser[Float] {
    /**
     * A basic way to parse known types from options.
 *
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def parse(str: String): ValidationNel[String, Float] = {
      Try(str.toFloat).asValidation
    }
}

  implicit object LongParser extends Parser[Long] {
    /**
     * A basic way to parse known types from options.
 *
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def parse(str: String): ValidationNel[String, Long] = {
      Try(java.lang.Long.parseLong(str)).asValidation
    }
}

  implicit object URLParser extends Parser[URL] {
    /**
     * A basic way to parse known types from options.
 *
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def parse(str: String): ValidationNel[String, URL] = {
      Try(new URL(str)).asValidation
    }
}

  implicit object EmailParser extends Parser[EmailAddress] {
    override def parse(str: String): ValidationNel[String, EmailAddress] = {
      val block: Try[EmailAddress] = if (EmailValidator.getInstance().isValid(str)) {
        Success(EmailAddress(str))
      } else {
        Failure(new Exception(s"""The string value "$str" is not a valid email address"""))
      }

      block.asValidation
    }
  }

  implicit class EnumParser[T <: Enumeration](enum: T) extends Parser[T#Value] {

    /**
     * A basic way to parse known types from options.
 *
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def parse(str: String): ValidationNel[String, T#Value] = {
      enum.values.find(_.toString == str) match {
        case Some(value) => value.successNel[String]
        case None => s"Value $str not found inside enumeration ${enum.getClass.getName}".failureNel[T#Value]
      }
    }
}

  def tryParse[T : Parser](str: String): Try[T] = implicitly[Parser[T]].tryParse(str)

  def parse[T : Parser](str: String): ValidationNel[String, T] = implicitly[Parser[T]].parse(str)

  def parse[T : Parser](obj: Option[String]): ValidationNel[String, T] = implicitly[Parser[T]].parse(obj)

  def parseOpt[T : Parser](obj: String): Option[T] = implicitly[Parser[T]].parseOpt(obj)

  def parseNonEmpty[T: Parser](obj: Option[String]): ValidationNel[String, Option[T]] = implicitly[Parser[T]].parseIfExists(obj)


  def biparse[A, B](obj: A)(implicit p: BiParser[A, B]): ValidationNel[String, B] = {
    implicitly[BiParser[A, B]].parse(obj)
  }

  def biparse[A, B](obj: Option[A])(implicit p: BiParser[A, B]): ValidationNel[String, B] = {
    implicitly[BiParser[A, B]].parse(obj)
  }

  def biparseOpt[A, B](obj: A)(implicit p: BiParser[A, B]): Option[B] = {
    implicitly[BiParser[A, B]].parseOpt(obj)
  }

  def biparseNonEmpty[A, B](obj: Option[A])(implicit p: BiParser[A, B]): ValidationNel[String, Option[B]] = {
    implicitly[BiParser[A, B]].parseIfExists(obj)
  }

}


private[util] trait DefaultParsers extends DefaultImplicitParsers {

  implicit class OptionDelegation[T](val option: Option[T]) {
    def delegate[Y]()(implicit bi: BiParser[T, Y]): ValidationNel[String, Y] = {
      option.fold(
        "Option was empty, couldn't delegate to biparser".failureNel[Y])(
        bi.parse
      )
    }

    def chain[Y](nel: T => ValidationNel[String, Y]): ValidationNel[String, Y] = {
      option.fold(
        "Option was empty, couldn't chain parser function".failureNel[Y])(
        nel
      )
    }
  }


  implicit class NelDelegation[X, T](val nel: ValidationNel[X, T]) {
    def chain[Y](fn: T => ValidationNel[String, Y]): ValidationNel[String, Y] = {
      nel.fold(
        _.list.toList.mkString(", ").failureNel[Y],
        fn
      )
    }
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

  def nonEmpty(str: String): ValidationNel[String, Boolean] = {
    if (str.length > 0) {
      true.successNel[String]
    } else {
      s"String required to be non-empty found empty".failureNel[Boolean]
    }
  }

  def nonEmpty[K, V](coll: Map[K, V]): ValidationNel[String, Boolean] = {
    if (coll.nonEmpty) {
      true.successNel[String]
    } else {
      "This collection is empty".failureNel[Boolean]
    }
  }

  def nonEmpty[M[X] <: Traversable[X]](coll: M[_]): ValidationNel[String, Boolean] = {
    if (coll.nonEmpty) {
      true.successNel[String]
    } else {
      "This collection is empty".failureNel[Boolean]
    }
  }

  final def enumOpt[T <: Enumeration](obj: Int, enum: T): Option[T#Value] = {
    Try(enum(obj)).toOption
  }

  final def enum[T <: Enumeration](obj: Int, enum: T): ValidationNel[String, T#Value] = {

    Try(enum.apply(obj)).toOption
      .fold(s"Value $obj is not part of the enumeration".failureNel[T#Value])(_.successNel[String])
  }

  final def enumOpt[T <: Enumeration](obj: String, enum: T): Option[T#Value] = {
    Try(enum.withName(obj)).toOption
  }

  final def enum[T <: Enumeration](obj: String, enum: T): ValidationNel[String, T#Value] = {
    Try(enum.withName(obj)).toOption
      .fold(s"Value $obj is not part of the enumeration".failureNel[T#Value])(_.successNel[String])
  }

}

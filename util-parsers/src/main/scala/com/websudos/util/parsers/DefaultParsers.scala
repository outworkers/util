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

import java.net.URL
import java.util.UUID

import com.websudos.util.domain.GenerationDomain
import org.apache.commons.validator.routines.EmailValidator
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}
import scalaz.Scalaz._
import scalaz.{Success => _, _}

sealed trait BaseParser[X, T] {

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

  def tryParse(obj: X): Try[T]


  /**
   * A basic way to parse known types from options.
   * @param str The string to attempt to parse from.
   * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
   */
  def parseOpt(str: X): Option[T] = tryParse(str).toOption

  def parse(str: X): ValidationNel[String, T] = {
    parseOpt(str).fold(s"Failed to parse from $str".failureNel[T])(_.successNel[String])
  }

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
    override def tryParse(str: String): Try[UUID] = Try(UUID.fromString(str))
  }

  implicit object BooleanParser extends Parser[Boolean] {

    override def tryParse(str: String): Try[Boolean] = str match {
      case "true" => Success(true)
      case "false" => Success(false)
      case _ => Failure(new Exception("A boolean parser will only parse the tings 'true' and 'false'"))
    }
  }

  implicit object TimestampParser extends Parser[DateTime] {
    /**
     * A basic way to parse known types from options.
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def tryParse(str: String): Try[DateTime] = {
      Try(new DateTime(str.toLong))
    }
  }

  implicit object IntParser extends Parser[Int] {
    /**
     * A basic way to parse known types from options.
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def tryParse(str: String): Try[Int] = {
      Try(str.toInt)
    }
  }

  implicit object DoubleParser extends Parser[Double] {
    /**
     * A basic way to parse known types from options.
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def tryParse(str: String): Try[Double] = {
      Try(str.toDouble)
    }
  }

  implicit object FloatParser extends Parser[Float] {
    /**
     * A basic way to parse known types from options.
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def tryParse(str: String): Try[Float] = {
      Try(str.toFloat)
    }
  }

  implicit object LongParser extends Parser[Long] {
    /**
     * A basic way to parse known types from options.
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def tryParse(str: String): Try[Long] = {
      Try(str.toLong)
    }
  }

  implicit object URLParser extends Parser[URL] {
    /**
     * A basic way to parse known types from options.
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def tryParse(str: String): Try[URL] = {
      Try(new URL(str))
    }
  }

  implicit object EmailParser extends Parser[EmailAddress] {
    override def tryParse(str: String): Try[EmailAddress] = {
      if (EmailValidator.getInstance().isValid(str)) {
        Success(EmailAddress(str))
      } else {
        Failure(new Exception(s"The string $str is not a vlaid email address"))
      }
    }
  }

  implicit class EnumParser[T <: Enumeration](enum: T) extends Parser[T#Value] {

    /**
     * A basic way to parse known types from options.
     * @param str The string to attempt to parse from.
     * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
     */
    override def tryParse(str: String): Try[T#Value] = {
      Try(enum.withName(str))
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

  def nonEmpty[T](coll: Traversable[T]): ValidationNel[String, Boolean] = {
    if (coll.nonEmpty) {
      true.successNel[String]
    } else {
      "This collection is empty".failureNel[Boolean]
    }
  }


  final def enumOpt[T <: Enumeration](obj: String, enum: T): Option[T#Value] = {
    Try(enum.withName(obj)).toOption
  }

  final def enum[T <: Enumeration](obj: String, enum: T): ValidationNel[String, T#Value] = {
    Try(enum.withName(obj)).toOption
      .fold(s"Value $obj is not part of the enumeration".failureNel[T#Value])(item => item.successNel[String])
  }

}

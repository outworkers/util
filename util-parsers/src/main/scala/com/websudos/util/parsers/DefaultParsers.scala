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
      Try(new DateTime(str.toLong)).asValidation
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
      Try(str.toLong).asValidation
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
        x => bi.parse(x)
      )
    }

    def chain[Y](nel: T => ValidationNel[String, Y]): ValidationNel[String, Y] = {
      option.fold(
        "Option was empty, couldn't delegate to biparser".failureNel[Y])(
        x => nel(x)
      )
    }
  }


  implicit class NelDelegation[X, T](val nel: ValidationNel[X, T]) {
    def chain[Y](fn: T => ValidationNel[String, Y]): ValidationNel[String, Y] = {
      nel.fold(
        fail => fail.list.mkString(", ").failureNel[Y],
        succ => fn(succ)
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

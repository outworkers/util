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

import cats.data._
import com.outworkers.util.domain.GenerationDomain
import org.apache.commons.validator.routines.EmailValidator
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.{Failure, Success, Try}

trait BiParser[X, T] {

  final def optional(str: Option[X])(f: X => ValidatedNel[String, T]): ValidatedNel[String, Option[T]] = {
    str.fold(Option.empty[T].validNel[String])(f(_).map(Some(_)))
  }

  private[util] def missing: ValidatedNel[String, T] = {
    "Required option expected to be Some, found None instead".invalidNel
  }

  private[util] final def parseRequired(str: Option[X])(f: X => ValidatedNel[String, T]) = {
    str.fold(s"Couldn't parse $str from None".invalidNel[T])(f)
  }

  final def required(opt: Option[T]): ValidatedNel[String, T] = {
    opt.fold("Required parameter is empty".invalidNel[T])(_.valid)
  }

  def tryParse(obj: X): Try[T] = parse(obj).asTry

  /**
    * A basic way to parse known types from options.
    *
    * @param in The string to attempt to parse from.
    * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
    */
  def parseOpt(in: X): Option[T] = tryParse(in).toOption

  def validateOpt(in: X): Option[T] = parseOpt(in)

  def parse(in: X): ValidatedNel[String, T]

  def validate(in: X): ValidatedNel[String, T] = parse(in)

  def parse(in: Option[X]): ValidatedNel[String, T] = parseRequired(in)(parse)

  def validate(in: Option[X]): ValidatedNel[String, T] = parse(in)

  def parseIfExists(str: Option[X]): ValidatedNel[String, Option[T]] = optional(str)(parse)

  def validateSkip(str: Option[X]): ValidatedNel[String, Option[T]] = parseIfExists(str)
}

object BiParser {
  def apply[Source, Target]()(
    implicit ev: BiParser[Source, Target]
  ): BiParser[Source, Target] = ev
}

trait Parser[T] extends BiParser[String, T]

object Parser {
  def apply[Target]()(implicit ev: Parser[Target]): Parser[Target] = ev
}

trait CatsImplicitParsers extends GenerationDomain {

  implicit object UUIDParser extends Parser[UUID] {
    override def parse(str: String): ValidatedNel[String, UUID] = {
      Try(UUID.fromString(str)).asValidation
    }
  }

  implicit object BooleanParser extends Parser[Boolean] {

    override def parse(str: String): ValidatedNel[String, Boolean] = {
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
    override def parse(str: String): ValidatedNel[String, DateTime] = {
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
    override def parse(str: String): ValidatedNel[String, Int] = {
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
    override def parse(str: String): ValidatedNel[String, Double] = {
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
    override def parse(str: String): ValidatedNel[String, Float] = {
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
    override def parse(str: String): ValidatedNel[String, Long] = {
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
    override def parse(str: String): ValidatedNel[String, URL] = {
      Try(new URL(str)).asValidation
    }
  }

  implicit object EmailParser extends Parser[EmailAddress] {
    override def parse(str: String): ValidatedNel[String, EmailAddress] = {
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
    override def parse(str: String): ValidatedNel[String, T#Value] = {
      enum.values.find(_.toString == str) match {
        case Some(value) => value.asInstanceOf[T#Value].valid
        case None => s"Value $str not found inside enumeration ${enum.getClass.getName}".invalidNel[T#Value]
      }
    }
  }

  def summon[T](implicit ev: BiParser[String, T]): BiParser[String, T] = ev

  def tryParse[T](str: String)(
    implicit parser: BiParser[String, T]
  ): Try[T] = summon[T].tryParse(str)

  def parse[T](str: String)(
    implicit parser: BiParser[String, T]
  ): ValidatedNel[String, T] = summon[T].parse(str)

  def validate[T](str: String)(
    implicit parser: BiParser[String, T]
  ): ValidatedNel[String, T] = parser.parse(str)

  def parse[T](obj: Option[String])(
    implicit parser: BiParser[String, T]
  ): ValidatedNel[String, T] = summon[T].parse(obj)

  def validate[T](obj: Option[String])(
    implicit parser: BiParser[String, T]
  ): ValidatedNel[String, T] = parser.parse(obj)

  def parseOpt[T](obj: String)(
    implicit parser: BiParser[String, T]
  ): Option[T] = summon[T].parseOpt(obj)

  def validateOpt[T](obj: String)(
    implicit parser: BiParser[String, T]
  ): Option[T] = parser.parseOpt(obj)

  def parseNonEmpty[T](obj: Option[String])(
    implicit parser: BiParser[String, T]
  ): ValidatedNel[String, Option[T]] = summon[T].parseIfExists(obj)

  def validateNonEmpty[T](obj: Option[String])(
    implicit parser: BiParser[String, T]
  ): ValidatedNel[String, Option[T]] = parser.parseIfExists(obj)

  def biparse[A, B](obj: A)(implicit p: BiParser[A, B]): ValidatedNel[String, B] = p.parse(obj)

  def biparse[A, B](obj: Option[A])(implicit p: BiParser[A, B]): ValidatedNel[String, B] = p.parse(obj)

  def biparseOpt[A, B](obj: A)(implicit p: BiParser[A, B]): Option[B] = p.parseOpt(obj)

  def biparseNonEmpty[A, B](obj: Option[A])(implicit p: BiParser[A, B]): ValidatedNel[String, Option[B]] = {
    BiParser[A, B].parseIfExists(obj)
  }
}

trait DefaultParsers extends CatsImplicitParsers {

  implicit class OptionDelegation[T](val option: Option[T]) {
    def delegate[Y]()(implicit bi: BiParser[T, Y]): ValidatedNel[String, Y] = {
      option.fold(
        "Option was empty, couldn't delegate to biparser".invalidNel[Y])(
        x => bi.parse(x)
      )
    }

    def chain[Y](nel: T => ValidatedNel[String, Y]): ValidatedNel[String, Y] = {
      option.fold(
        "Option was empty, couldn't delegate to biparser".invalidNel[Y])(
        x => nel(x)
      )
    }
  }

  implicit class NelDelegation[X, T](val nel: ValidatedNel[X, T]) {
    def chain[Y](fn: T => ValidatedNel[String, Y]): ValidatedNel[String, Y] = {
      nel.fold(
        fail => fail.toList.mkString(", ").invalidNel[Y],
        succ => fn(succ)
      )
    }
  }

  final def present(str: String, name: String): ValidatedNel[String, String] = {
    if (str.trim.length == 0) {
      s"$name is empty".invalidNel[String]
    } else {
      str.valid
    }
  }

  final def confirm(first: String, second: String): ValidatedNel[String, String] = {
    if (first != second) {
      s"Strings $first and $second don't match".invalidNel[String]
    } else {
      first.valid
    }
  }

  /**
    * A validator for strings, will only succeed if the string is not empty.
    * @param str The source option.
    * @return A [[ValidatedNel]] that will return the content of the string if not empty, or a failure otherwise.
    */
  def nonEmpty(str: String): ValidatedNel[String, String] = {
    if (str.length > 0) {
      str.valid
    } else {
      s"String required to be non-empty found empty".invalidNel[String]
    }
  }

  /**
    * A validator for maps, will only succeed if the option is not empty.
    * @param map The source option.
    * @tparam K The type of the key.
    * @tparam V The type of the value of the map.
    * @return A [[ValidatedNel]] that will return the map itself if it's not empty, or a failure otherwise.
    */
  def nonEmpty[K, V](map: Map[K, V]): ValidatedNel[String, Map[K, V]] = {
    if (map.nonEmpty) {
      map.valid
    } else {
      "This Map is empty".invalidNel[Map[K, V]]
    }
  }

  /**
    * A validator for options, will only succeed if the option is not empty.
    * @param opt The source option.
    * @tparam T The type of the option.
    * @return A [[ValidatedNel]] that will return the content of the option if not empty, or a failure otherwise.
    */
  def nonEmpty[T](opt: Option[T]): ValidatedNel[String, T] = {
    opt.fold("This collection is empty".invalidNel[T])(_.validNel[String])
  }

  /**
    * A validator for collections, will only succeed if the underlying collection is not empty.
    * @param coll The source collection.
    * @tparam T The inner type of the collection.
    * @return A [[ValidatedNel]] that will return the content of the collection if not empty, or a failure otherwise.
    */
  def nonEmpty[M[X] <: Traversable[X], T](coll: M[T]): ValidatedNel[String, M[T]] = {
    if (coll.nonEmpty) {
      coll.valid
    } else {
      "This collection is empty".invalidNel[M[T]]
    }
  }

  final def enumOpt[T <: Enumeration](obj: String, enum: T): Option[T#Value] = {
    Try(enum.withName(obj)).toOption
  }

  final def enum[T <: Enumeration](obj: String, enum: T): ValidatedNel[String, T#Value] = {
    Try(enum.withName(obj)).toOption
      .fold(s"Value $obj is not part of the enumeration".invalidNel[T#Value])(_.asInstanceOf[T#Value].valid)
  }

}

package com.outworkers.util.catsparsers

import java.net.URL
import java.util.UUID

import cats.data.ValidatedNel
import com.outworkers.util.domain.GenerationDomain
import org.apache.commons.validator.routines.EmailValidator
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.{Failure, Success, Try}

sealed trait CatsBiParser[X, T] {

  final def optional(str: Option[X])(f: X => ValidatedNel[String, T]): ValidatedNel[String, Option[T]] = {
    str.fold(Option.empty[T].invalid("Option was not defined")) { s =>
      f(s).bimap(identity, Some(_))
    }
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
    * @param str The string to attempt to parse from.
    * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
    */
  def parseOpt(str: X): Option[T] = tryParse(str).toOption

  def parse(str: X): ValidatedNel[String, T]

  def parse(str: Option[X]): ValidatedNel[String, T] = {
    parseRequired(str)(s => parse(s))
  }

  def parseIfExists(str: Option[X]): ValidatedNel[String, Option[T]] = {
    optional(str)(parse)
  }
}

trait CatsParser[T] extends CatsBiParser[String, T]

trait CatsImplicitParsers extends GenerationDomain {

  implicit object UUIDParser extends CatsParser[UUID] {
    override def parse(str: String): ValidatedNel[String, UUID] = {
      Try(UUID.fromString(str)).asValidation
    }
  }

  implicit object BooleanParser extends CatsParser[Boolean] {

    override def parse(str: String): ValidatedNel[String, Boolean] = {
      val obj = str match {
        case "true" => Success(true)
        case "false" => Success(false)
        case _ => Failure(new Exception("A boolean parser will only parse the tings 'true' and 'false'"))
      }

      obj.asValidation
    }

  }

  implicit object TimestampParser extends CatsParser[DateTime] {
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

  implicit object IntParser extends CatsParser[Int] {
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

  implicit object DoubleParser extends CatsParser[Double] {
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

  implicit object FloatParser extends CatsParser[Float] {
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

  implicit object LongParser extends CatsParser[Long] {
    /**
      * A basic way to parse known types from options.
      *
      * @param str The string to attempt to parse from.
      * @return An Option wrapping a valid T instance if the parsing was successful, None otherwise.
      */
    override def parse(str: String): ValidatedNel[String, Long] = {
      Try(str.toLong).asValidation
    }
  }

  implicit object URLParser extends CatsParser[URL] {
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

  implicit object EmailParser extends CatsParser[EmailAddress] {
    override def parse(str: String): ValidatedNel[String, EmailAddress] = {
      val block: Try[EmailAddress] = if (EmailValidator.getInstance().isValid(str)) {
        Success(EmailAddress(str))
      } else {
        Failure(new Exception(s"""The string value "$str" is not a valid email address"""))
      }

      block.asValidation
    }
  }

  implicit class EnumParser[T <: Enumeration](enum: T) extends CatsParser[T#Value] {

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

  def tryParse[T : CatsParser](str: String): Try[T] = implicitly[CatsParser[T]].tryParse(str)

  def parse[T : CatsParser](str: String): ValidatedNel[String, T] = implicitly[CatsParser[T]].parse(str)

  def parse[T : CatsParser](obj: Option[String]): ValidatedNel[String, T] = implicitly[CatsParser[T]].parse(obj)

  def parseOpt[T : CatsParser](obj: String): Option[T] = implicitly[CatsParser[T]].parseOpt(obj)

  def parseNonEmpty[T: CatsParser](obj: Option[String]): ValidatedNel[String, Option[T]] = implicitly[CatsParser[T]].parseIfExists(obj)

  def biparse[A, B](obj: A)(implicit p: CatsBiParser[A, B]): ValidatedNel[String, B] = {
    implicitly[CatsBiParser[A, B]].parse(obj)
  }

  def biparse[A, B](obj: Option[A])(implicit p: CatsBiParser[A, B]): ValidatedNel[String, B] = {
    implicitly[CatsBiParser[A, B]].parse(obj)
  }

  def biparseOpt[A, B](obj: A)(implicit p: CatsBiParser[A, B]): Option[B] = {
    implicitly[CatsBiParser[A, B]].parseOpt(obj)
  }

  def biparseNonEmpty[A, B](obj: Option[A])(implicit p: CatsBiParser[A, B]): ValidatedNel[String, Option[B]] = {
    implicitly[CatsBiParser[A, B]].parseIfExists(obj)
  }
}

trait DefaultParsers extends CatsImplicitParsers {

  implicit class OptionDelegation[T](val option: Option[T]) {
    def delegate[Y]()(implicit bi: CatsBiParser[T, Y]): ValidatedNel[String, Y] = {
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

  def nonEmpty(str: String): ValidatedNel[String, Boolean] = {
    if (str.length > 0) {
      true.valid
    } else {
      s"String required to be non-empty found empty".invalidNel[Boolean]
    }
  }

  def nonEmpty[K, V](coll: Map[K, V]): ValidatedNel[String, Boolean] = {
    if (coll.nonEmpty) {
      true.valid
    } else {
      "This collection is empty".invalidNel[Boolean]
    }
  }

  def nonEmpty[T](coll: Traversable[T]): ValidatedNel[String, Boolean] = {
    if (coll.nonEmpty) {
      true.valid
    } else {
      "This collection is empty".invalidNel[Boolean]
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

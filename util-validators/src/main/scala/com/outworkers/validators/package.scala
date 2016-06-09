package com.outworkers

import com.websudos.util.parsers._
import shapeless.Poly._
import shapeless._
import shapeless.ops.function.{FnFromProduct, FnToProduct}
import shapeless.ops.hlist._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.higherKinds
import scalaz.{Applicative, NonEmptyList, ValidationNel}
import scalaz.Scalaz._
import shapeless.ops.hlist.Reverse

package object validators {

  object applier extends Poly2 {
    implicit def ap[F[_]: Applicative, H, T <: HList, R]:
    shapeless.poly.Case2.Aux[applier.type, F[(H :: T) => R], F[H], F[T => R]] =
      at[F[(H :: T) => R], F[H]](
        (f, fa) => fa <*> f.map(hf => (h: H) => (t: T) => hf(h :: t))
      )
  }

  def validate[F[_], G, H, V <: HList, I <: HList, M <: HList, A <: HList, R](g: G)(v: V)(implicit hlG: FnToProduct.Aux[G, A => R],
    zip: ZipApply.Aux[V, I, M],
    mapped: Mapped.Aux[A, F, M],
    unH: FnFromProduct.Aux[I => F[R], H],
    folder: LeftFolder.Aux[M, F[A => R],
      applier.type, F[HNil => R]],
    appl: Applicative[F]
  ): H = unH((in: I) => folder(zip(v, in), hlG(g).point[F]).map(_(HNil)))

  type ErrorsOr[A] = ValidationNel[String, A]
  type Validator[A] = String => ErrorsOr[A]

  implicit class VdNelAug[T](val vd: ErrorsOr[T]) extends AnyVal {
    def prop(key: String): WrappedValidation[T] = new WrappedValidation[T](key, vd)
  }

  case class Foo(a: Int, b: Char, c: String)

  def f: (Int, Char, String) => Foo = Foo.apply
  /*

  val checkA: Validator[Int] = (s: String) =>
    try s.toInt.success catch {
      case _: NumberFormatException => "Not a number!".failureNel
    }

  val checkB: Validator[Char] = (s: String) =>
    if (s.length != 1 || s.head < 'a' || s.head > 'z') {
      "Not a lower case letter!".failureNel
    } else {
      s.head.success
    }

  val checkC: Validator[String] = (s: String) =>
    if (s.length == 4) s.success else "Wrong size!".failureNel

  val validateFoo = validate(Foo.apply _)(checkA :: checkB :: checkC :: HNil)
  */

  class WrappedValidation[T](val prop: String, val validation: ErrorsOr[T]) {

    def tp: (String, ErrorsOr[T]) = prop -> validation

    def and[A](wv: WrappedValidation[A]): ValidationBuilder[
      ErrorsOr[T] ::ErrorsOr[A] :: HNil,
      T :: A :: HNil
      ] = {
      new ValidationBuilder(List(wv.prop, prop), validation :: wv.validation :: HNil)
    }

  }

  class ValidationBuilder[
    ValTypes <: HList,
    ArgTypes <: HList
  ](props: List[String], list: ValTypes) {
    def and[T](wv: WrappedValidation[T]): ValidationBuilder[
      ErrorsOr[T] :: ValTypes,
      T :: ArgTypes
      ] = {
      new ValidationBuilder(props.::(wv.prop), wv.validation :: list)
    }

    def unwrap[F, H, R, Rev, I <: HList, M <: HList](fn: F)(
      implicit unH: FnToProduct.Aux[F, ArgTypes => R],
      zip: ZipApply.Aux[ValTypes, I, M],
      rev: Reverse.Aux[ArgTypes, Rev],
      mapped: Mapped.Aux[A, F, M],
    ) = {
      validate(fn)(list)
    }
  }
}

object Test {

  import validators._

  case class Contact(email: String, firstName: String, age: Int)

  def parseAge(age: Int): ErrorsOr[Int] = {
    if (age > 18) {
      age.successNel[String]
    } else {
      "Minors are not allowed".failureNel[Int]
    }
  }


  object Contact {
    def validate(contact: Contact) = {
      (parse[EmailAddress](contact.email).prop("email") and
        parse[EmailAddress](contact.firstName).prop("firstName") and
        parseAge(contact.age).prop("age")
        ).unwrap()
    }
  }

  def test(email: String, firstName: String) = {
    (parse[EmailAddress](email).prop("email") and
      parse[EmailAddress](firstName).prop("firstName")).unwrap()

  }
}




case class ParseError(property: String, messages: List[String])

object ParseError {
}

case class ValidationError(errors: List[ParseError]) {
  def add(other: ValidationError): ValidationError = ValidationError(errors ++ other.errors)
}

object ValidationError  {

  def empty: ValidationError = ValidationError(Nil)

  def fromErrorMap(props: (String, String)*): ValidationError = {
    ValidationError(List(props.map {
      case (key, value) => ParseError(key, value :: Nil)
    }: _*))
  }

  def fromErrors(props: (String, String)*): ValidationError = {
    ValidationError(List(props.map {
      case (property, errorList) => ParseError(property, errorList :: Nil)
    }: _*))
  }

  implicit class NelConverter(val nel: String) extends AnyVal {
    def asValidationError(prop: String): ValidationError = fromErrors(prop -> nel)
  }

  implicit class ValidationNelConverter[T](val validation: ValidationNel[String, T]) extends AnyVal {
    def handleErrors(prop: String): Either[ValidationError, T] = {
      validation fold (
        fail => Left(fromErrors(prop -> fail.head)),
        obj => Right(obj)
        )
    }
  }

  implicit class ValidationNelFutureConverter[T](val validation: ValidationNel[String, Future[T]]) extends AnyVal {
    def handleErrors(prop: String = "unknown")(implicit ec: ExecutionContextExecutor): Future[Either[ValidationError, T]] = {
      validation fold (
        fail => Future.successful(Left(fromErrors(prop -> fail.head))),
        obj => obj.map(Right(_))
        )
    }
  }



  implicit class ValidationUnwrapper[T](val validation: ValidationNel[String, Future[Either[ValidationError, T]]]) extends AnyVal {
    def unwrap(prop: String = "unknown"): Future[Either[ValidationError, T]] = {
      validation.fold(
        nel => Future.successful(Left(nel.head.asValidationError(prop))),
        identity
      )
    }
  }
}
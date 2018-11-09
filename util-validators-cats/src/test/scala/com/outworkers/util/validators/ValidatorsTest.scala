package com.outworkers.util.validators

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try
import cats.data.ValidatedNel
import cats.data.Validated.{Invalid, Valid}
import com.outworkers.util.domain._
import com.outworkers.util.validators._
import com.outworkers.util.parsers._

trait Read[A] {
  def read(s: String): Option[A]
}

object Read {
  def apply[A](implicit A: Read[A]): Read[A] = A

  implicit val stringRead: Read[String] =
    new Read[String] {
      def read(s: String): Option[String] = Some(s)
    }

  implicit val intRead: Read[Int] =
    new Read[Int] {
      def read(s: String): Option[Int] = {
        if (s.matches("-?[0-9]+")) {
          Some(s.toInt)
        } else {
          None
        }
      }
    }

  implicit val doubleRead: Read[Double] = new Read[Double] {
    override def read(s: String): Option[Double] = Try(s.toDouble).toOption
  }
}

case class Config(map: Map[String, String]) {
  def parse[A : Read](key: String): ValidatedNel[(String, String), A] =
    map.get(key) match {
      case None => Invalid(key -> "Missing configuration").toValidatedNel
      case Some(value) =>
        Read[A].read(value) match {
          case None    => Invalid(key -> "Could not parse field value").toValidatedNel
          case Some(a) => Valid(a)
        }
    }
}

case class Address(houseNumber: Int, street: String)
case class Person(name: String, age: Int, address: Address)

class ValidatorsTest extends FlatSpec with Matchers {

  implicit val readString: Read[String] = Read.stringRead
  implicit val readInt: Read[Int] = Read.intRead

  val config2 = Config(Map(
    "name" -> "cat",
    "age" -> "not a number",
    "houseNumber" -> "1234",
    "email" -> "teasg",
    "lane" -> "feline street"
  ))

  it should "correctly group validations by name" in {
    val personFromConfig =
      config2.parse[String]("name") and
        config2.parse[Int]("name") and
        config2.parse[Int]("age") and
        config2.parse[Int]("house_number") and
        config2.parse[Double]("house_number") and
        parse[EmailAddress](config2.map("email")).prop("email") and
        config2.parse[String]("street") map {
        case (name, name2, age, houseNumber, hn, email, street) => {
          Person(name, age, Address(houseNumber, street))
        }
      }

    personFromConfig.unwrap.isLeft shouldEqual true
    personFromConfig.unwrap match {
      case Left(err) => info(err.toString)
      case Right(clz) => info(clz.toString)
    }
  }
}

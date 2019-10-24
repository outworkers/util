package com.outworkers.util.empty

import java.net.InetAddress
import java.util.{Date, UUID}

import com.outworkers.util.domain._
import com.outworkers.util.samplers.Sample
import org.scalacheck.{Arbitrary, Gen}

import scala.reflect.ClassTag
import scala.util.Random

trait Empty[T] extends Sample[T]

object Empty extends EmptyGenerators {

  def apply[T](fn: =>T): Empty[T] = new Empty[T] {
    override def sample: T = fn
  }

  implicit val emptyString: Empty[String] = apply("")
  implicit val emptyInt: Empty[Int] = apply(0)
  implicit val emptyShort: Empty[Short] = apply(0.toShort)
  implicit val emptyDouble: Empty[Double] = apply(0.toDouble)
  implicit val emptyFloat: Empty[Float] = apply(0.toFloat)
  implicit val emptyLong: Empty[Long] = apply(0.toLong)
  implicit val emptyByte: Empty[Byte] = apply(0.toByte)
  implicit val emptyBoolean: Empty[Boolean] = apply(Random.nextBoolean())
  implicit val emptyBigDec: Empty[BigDecimal] = apply(BigDecimal(0))
  implicit val emptyBigInt: Empty[BigInt] = apply(BigInt(0))
  implicit val emptyDate: Empty[Date] = apply(new Date())
  implicit val emptyUUID: Empty[UUID] = apply(UUID.randomUUID())
  implicit val emptyEmailAddress: Empty[EmailAddress] = apply(EmailAddress(""))
  implicit val emptyFirstName: Empty[FirstName] = apply(FirstName(""))
  implicit val emptyLastName: Empty[LastName] = apply(LastName(""))
  implicit val emptyFullName: Empty[FullName] = apply(FullName(""))
  implicit val emptyCountryCode: Empty[CountryCode] = apply(CountryCode(""))
  implicit val emptyCountry: Empty[Country] = apply(Country(""))
  implicit val emptyCity: Empty[City] = apply(City(""))
  implicit val emptyInet: Empty[InetAddress] = apply(InetAddress.getByAddress(Array(0, 0, 0 ,0)))
  implicit val emptyProgrammingLang: Empty[ProgrammingLanguage] = apply(ProgrammingLanguage(""))
  implicit val emptyLoremIpsum: Empty[LoremIpsum] = apply(LoremIpsum(""))
  implicit val emptyUrl: Empty[Url] = apply(Url(""))

  /**
    * !! Warning !! Black magic going on. This will use the excellent macro compat
    * library to macro materialise an instance of the required primitive based on the type argument.
    * @tparam T The type parameter to materialise a sample for.
    * @return A derived sampler, materialised via implicit blackbox macros.
    */
  implicit def materialize[T]: Empty[T] = macro EmptyMacro.materialize[T]

  implicit def arbitrary[T : Empty]: Arbitrary[T] = Arbitrary(generator[T])

  def generator[T : Empty]: Gen[T] = Gen.delay(void[T])

  // Scala 2.13 compat
  implicit def listEmpty[T]: Empty[List[T]] = new Empty[List[T]] {
    override def sample: List[T] = List.empty[T]
  }

  // Scala 2.13 compat
  implicit def seqEmpty[T]: Empty[Seq[T]] = new Empty[Seq[T]] {
    override def sample: Seq[T] = Seq.empty[T]
  }

  // Scala 2.13 compat
  implicit def arrEmpty[T : ClassTag]: Empty[Array[T]] = new Empty[Array[T]] {
    override def sample: Array[T] = Array.empty[T]
  }

  /**
    * Derives samplers and encodings for a non standard type.
    * @param fn The function that converts a [[T]] instance to a [[T1]] instance.
    * @tparam T1 The type you want to derive a sampler for.
    * @tparam T The source type of the sampler, must already have a sampler defined for it.
    * @return A new sampler that can interact with the target type.
    */
  def iso[T : Empty, T1](fn: T => T1): Empty[T1] = Empty(fn(void[T]))

  /**
    * Convenience method to materialise the context bound and return a reference to it.
    * This is somewhat shorter syntax than using implicitly.
    * @tparam RR The type of the sample to retrieve.
    * @return A reference to a concrete materialised implementation of a sample for the given type.
    */
  def apply[RR]()(implicit ev: Empty[RR]): Empty[RR] = ev

}

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
package com.outworkers.util.samplers

import java.net.InetAddress
import java.util.{Date, Locale, UUID}

import org.scalacheck.{Arbitrary, Gen}
import com.outworkers.util.domain._
import com.outworkers.util.empty.Empty

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag
import scala.util.Random

@implicitNotFound("No automated way to create A Sample for ${T}. Create an implicit Sample for ${T} in scope manually")
trait Sample[T] {
  def sample: T
}

object Sample extends Generators {

  private[this] val byteLimit = 127
  private[this] val shortLimit = 256
  private[this] val inetBlock = 4

  class StringSampler extends Sample[String] {
    /**
      * Get a unique random generated string.
      * This uses the default java GUID implementation.
      * @return A random string with 64 bits of randomness.
      */
    def sample: String = UUID.randomUUID().toString
  }

  class ShortStringSampler extends Sample[ShortString] {
    def sample: ShortString = {
      ShortString(java.lang.Long.toHexString(java.lang.Double.doubleToLongBits(Math.random())))
    }
  }

  class ByteSampler extends Sample[Byte] {
    def sample: Byte = Random.nextInt(byteLimit).toByte
  }

  class BooleanSampler extends Sample[Boolean] {
    def sample: Boolean = Random.nextBoolean()
  }

  class IntSampler extends Sample[Int] {
    def sample: Int = Random.nextInt()
  }

  class ShortSampler extends Sample[Short] {
    def sample: Short = Random.nextInt(shortLimit).toShort
  }

  class DoubleSampler extends Sample[Double] {
    def sample: Double = Random.nextDouble()
  }

  class FloatSampler extends Sample[Float] {
    def sample: Float = Random.nextFloat()
  }

  class LongSampler extends Sample[Long] {
    def sample: Long = Random.nextLong()
  }

  class BigDecimalSampler extends Sample[BigDecimal] {
    def sample: BigDecimal = BigDecimal(Random.nextDouble())
  }

  class BigIntSampler extends Sample[BigInt] {
    def sample: BigInt = BigInt(Random.nextLong())
  }

  class DateSampler extends Sample[Date] {
    def sample: Date = new Date()
  }

  class UUIDSampler extends Sample[UUID] {
    def sample: UUID = UUID.randomUUID()
  }

  class EmailAddressSampler extends Sample[EmailAddress] {
    def sample: EmailAddress = {
      val random = new Random
      val test = random.nextInt(100)
      var email: String = ""
      if (test < 50) {
        // name and initial
        email = Generators.oneOf(NameValues.firstNames).charAt(0) +  Generators.oneOf(NameValues.lastNames)
      }
      else {
        // 2 words
        email = Generators.oneOf(ContentDataValues.words) + Generators.oneOf(ContentDataValues.words)
      }

      if (random.nextInt(100) > 80) email = email + random.nextInt(100)
      email = email + "@" + Generators.oneOf(ContentDataValues.emailHosts) + "." + Generators.oneOf(ContentDataValues.tlds)
      EmailAddress(email.toLowerCase)
    }
  }

  class FirstNameSampler extends Sample[FirstName] {
    def sample: FirstName = FirstName(Generators.oneOf(NameValues.firstNames))
  }

  class LastNameSampler extends Sample[LastName] {
    def sample: LastName = LastName(Generators.oneOf(NameValues.lastNames))
  }

  class FullNameSampler extends Sample[FullName] {
    def sample: FullName = FullName(s"${Gen.oneOf(NameValues.firstNames).sample.get} ${Gen.oneOf(NameValues.lastNames).sample.get}")
  }

  class CountryCodeSampler extends Sample[CountryCode] {
    def sample: CountryCode = CountryCode(Gen.oneOf(Locale.getISOCountries).sample.get)
  }

  class CountrySampler extends Sample[Country] {
    def sample: Country = Country(Gen.oneOf(BaseSamplers.Countries).sample.get)
  }

  class CitySampler extends Sample[City] {
    def sample: City = City(Generators.oneOf(BaseSamplers.cities))
  }

  class InetAddressSampler extends Sample[InetAddress] {
    def sample: InetAddress = {
      InetAddress.getByAddress(List.tabulate(inetBlock)(_ => new ByteSampler().sample).toArray)
    }
  }

  class ProgrammingLanguageSampler extends Sample[ProgrammingLanguage] {
    def sample: ProgrammingLanguage = ProgrammingLanguage(Gen.oneOf(BaseSamplers.ProgrammingLanguages).sample.get)
  }

  class LoremIpsumSampler extends Sample[LoremIpsum] {
    def sample: LoremIpsum = LoremIpsum(Gen.oneOf(BaseSamplers.LoremIpsum).sample.get)
  }

  class UrlSampler extends Sample[Url] {
    def sample: Url = {
      val str = java.lang.Long.toHexString(java.lang.Double.doubleToLongBits(Math.random()))
      Url(oneOf(protocols) + "://www." + str + "." + oneOf(domains))
    }
  }

  /**
    * !! Warning !! Black magic going on. This will use the excellent macro compat
    * library to macro materialise an instance of the required primitive based on the type argument.
    * @tparam T The type parameter to materialise a sample for.
    * @return A derived sampler, materialised via implicit blackbox macros.
    */
  implicit def materialize[T]: Sample[T] = macro SamplerMacro.materialize[T]

  def arbitrary[T : Sample]: Arbitrary[T] = Arbitrary(generator[T])

  def generator[T : Sample]: Gen[T] = Gen.delay(gen[T])

  // Scala 2.13 compat
  implicit def listMaterializer[T : Sample]: Sample[List[T]] = new Sample[List[T]] {
    override def sample: List[T] = List.fill(defaultGeneration)(gen[T])
  }

  // Scala 2.13 compat
  implicit def seqMaterializer[T : Sample]: Sample[Seq[T]] = new Sample[Seq[T]] {
    override def sample: Seq[T] = Seq.fill(defaultGeneration)(gen[T])
  }

  // Scala 2.13 compat
  implicit def setMaterializer[T : Sample]: Sample[Set[T]] = new Sample[Set[T]] {
    override def sample: Set[T] = gen[List[T]].toSet
  }

  // Scala 2.13 compat
  implicit def arrayMaterializer[T : ClassTag : Sample]: Sample[Array[T]] = new Sample[Array[T]] {
    override def sample: Array[T] = Array.fill(defaultGeneration)(gen[T])
  }

  // Scala 2.13 compat
  implicit def indexedSeqMaterializer[T : Sample]: Sample[IndexedSeq[T]] = new Sample[IndexedSeq[T]] {
    override def sample: IndexedSeq[T] = IndexedSeq.fill(defaultGeneration)(gen[T])
  }

  /**
    * Derives samplers and encodings for a non standard type.
    * @param fn The function that converts a [[T]] instance to a [[T1]] instance.
    * @tparam T1 The type you want to derive a sampler for.
    * @tparam T The source type of the sampler, must already have a sampler defined for it.
    * @return A new sampler that can interact with the target type.
    */
  def iso[T : Sample, T1](fn: T => T1): Sample[T1] = new Sample[T1] {
    override def sample: T1 = fn(gen[T])
  }

  /**
    * Convenience method to materialise the context bound and return a reference to it.
    * This is somewhat shorter syntax than using implicitly.
    * @tparam RR The type of the sample to retrieve.
    * @return A reference to a concrete materialised implementation of a sample for the given type.
    */
  def apply[RR]()(implicit ev: Sample[RR]): Sample[RR] = ev
}

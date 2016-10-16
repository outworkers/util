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
package com.outworkers.util.testing

import java.util.{Date, Locale, UUID}

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.scalacheck.Gen
import com.outworkers.util.domain._

import scala.util.Random

trait Sample[T] {
  def sample: T
}

object Sample extends Generators {


  implicit object StringSample extends Sample[String] {
    def sample: String = Sampler.string
  }

  implicit object ShortStringSampler extends Sample[ShortString] {
    def sample: ShortString = {
      ShortString(java.lang.Long.toHexString(java.lang.Double.doubleToLongBits(Math.random())))
    }
  }

  implicit object BooleanSampler extends Sample[Boolean] {
    def sample: Boolean = Random.nextBoolean()
  }

  implicit object IntSample extends Sample[Int] {
    def sample: Int = Sampler.int()
  }

  implicit object DoubleSample extends Sample[Double] {
    def sample: Double = gen[Int].toDouble / 0.3
  }

  implicit object FloatSample extends Sample[Float] {
    def sample: Float = gen[Int].toFloat
  }

  implicit object LongSample extends Sample[Long] {
    def sample: Long = gen[Int].toLong
  }

  implicit object BigDecimalSampler extends Sample[BigDecimal] {
    def sample: BigDecimal = BigDecimal(gen[Int])
  }

  implicit object BigIntSampler extends Sample[BigInt] {
    def sample: BigInt = BigInt(gen[Int])
  }

  implicit object DateSample extends Sample[Date] {
    def sample: Date = new Date()
  }

  implicit object DateTimeSampler extends Sample[DateTime] {
    def sample: DateTime = new DateTime(DateTimeZone.UTC)
  }

  implicit object LocalDateSampler extends Sample[LocalDate] {
    def sample: LocalDate = new LocalDate()
  }

  implicit object UUIDSample extends Sample[UUID] {
    def sample: UUID = UUID.randomUUID()
  }

  implicit object EmailSample extends Sample[EmailAddress] {
    def sample: EmailAddress = EmailAddress(Sampler.email())
  }

  implicit object FirstNameSampler extends Sample[FirstName] {
    def sample: FirstName = FirstName(Sampler.factory.getFirstName)
  }

  implicit object LastNameSampler extends Sample[LastName] {
    def sample: LastName = LastName(Sampler.factory.getLastName)
  }

  implicit object FullNameSampler extends Sample[FullName] {
    def sample: FullName = FullName(s"${Sampler.factory.getFirstName} ${Sampler.factory.getLastName}")
  }

  implicit object CountryCodeSampler extends Sample[CountryCode] {
    def sample: CountryCode = CountryCode(Gen.oneOf(Locale.getISOCountries).sample.get)
  }

  implicit object CountrySampler extends Sample[Country] {
    def sample: Country = Country(Gen.oneOf(CommonDataSamplers.Countries).sample.get)
  }

  implicit object CitySampler extends Sample[City] {
    def sample: City = City(Gen.oneOf(CommonDataSamplers.Cities).sample.get)
  }

  implicit object ProgrammingLanguageSampler extends Sample[ProgrammingLanguage] {
    def sample: ProgrammingLanguage = ProgrammingLanguage(Gen.oneOf(CommonDataSamplers.PrgrammingLanguages).sample.get)
  }

  implicit object LoremIpsumSampler extends Sample[LoremIpsum] {
    def sample: LoremIpsum = LoremIpsum(Gen.oneOf(CommonDataSamplers.LoremIpsum).sample.get)
  }

  implicit object UrlSampler extends Sample[Url] {
    def sample: Url = Url(
      oneOf(protocols) + "://www." + gen[ShortString].str + "." + oneOf(domains)
    )
  }

  def apply[T : Sample]: Sample[T] = implicitly[Sample[T]]
}

private[util] trait Generators extends GenerationDomain {

  protected[this] val domains = List("net", "com", "org", "io", "biz", "co.uk", "co.za")
  protected[this] val protocols = List("http", "https")

  val defaultGeneration = 5

  /**
   * Uses the type class available in implicit scope to mock a certain custom object.
   * @tparam T The parameter to mock.
   * @return A sample of the given type generated using the implicit sampler.
   */
  def gen[T : Sample]: T = implicitly[Sample[T]].sample

  /**
   * Generates a tuple of the given type arguments, using the implicit samplers in scope.
   * @tparam X The first type of the tuple to be sampled.
   * @tparam Y The second type of the type to be sampled.
   * @return A Tuple2[X, Y] generated using the implicit samplers.
   */
  def gen[X: Sample, Y: Sample]: (X, Y) = (gen[X], gen[Y])

  def genOpt[T : Sample]: Option[T] = Some(implicitly[Sample[T]].sample)

  def genList[T : Sample](size: Int = defaultGeneration): List[T] = List.tabulate(size)(i => gen[T])

  def genMap[T : Sample](size: Int = defaultGeneration): Map[String, T] = {
    genList[T](size).map(item => (item.toString, item)).toMap
  }

  /**
   * Generates a map of known key -> value types using implicit samplers.
   * @param size The number of elements to generate in the map.
   * @tparam Key The type of the key the generated map should have. Needs a Sample[Key] in scope.
   * @tparam Value The type of the value the generated map should have. Needs a Sample[Value] in scope.
   * @return A key -> value map generated using the pre-defined samples for Key and Value.
   */
  def genMap[Key : Sample, Value : Sample](size: Int): Map[Key, Value] = {
    List.tabulate(size)(i => (gen[Key], gen[Value])).toMap
  }

  /**
   * Generates a map using a Sampler for the key and a function Key -> Value for the value.
   * Useful when the value of a key can be inferred by knowing the key itself.
   *
   * The implementation uses the value during mapping as the genMap function called with
   * a single type argument will generate a Map[String, Type].
   *
   * @param size The size of the map to generate.
   * @param producer The function used to generate the value from a key.
   * @tparam Key The type of the Key to generate, needs to have a Sample available in scope.
   * @tparam Value The type of the Value to generate.
   * @return A map of the given size with sampled keys and values inferred by the producer function.
   */
  def genMap[Key : Sample, Value](size: Int, producer: Key => Value): Map[Key, Value] = {
    genMap[Key](size) map {
      case (k, v) => (v, producer(v))
    }
  }

  def oneOf[T](list: Seq[T]): T = Gen.oneOf(list).sample.get

  def oneOf[T <: Enumeration](enum: T): T#Value = {
    oneOf(enum.values.toList)
  }
}
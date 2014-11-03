package com.websudos.util.testing

import java.util.{Date, UUID}

import org.joda.time.{LocalDate, DateTime}

trait Sample[T] {
  def sample: T
}

case class EmailAddress(address: String)


sealed trait Generators {

  /**
   * Uses the type class available in implicit scope to mock a certain custom object.
   * @tparam T The parameter to mock.
   * @return
   */
  def gen[T : Sample]: T = implicitly[Sample[T]].sample

  def gen[X: Sample, Y: Sample]: (X, Y) = (gen[X], gen[Y])

  def genOpt[T : Sample]: Option[T] = Some(implicitly[Sample[T]].sample)

  def genList[T : Sample](limit: Int = 5): List[T] = List.range(1, limit) map(_ => gen[T])

  def getSet[T : Sample](limit: Int = 5): List[T] = List.range(1, limit) map(_ => gen[T]).toSet[T]

  def genMap[T: Sample](limit: Int = 5): Map[String, T] = genList[T]().map(x => {x.toString -> x}).toMap

}

trait DefaultSamplers extends Generators {

  implicit object StringSample extends Sample[String] {
    def sample: String = Sampler.string
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
    def sample: DateTime = new DateTime()
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
}



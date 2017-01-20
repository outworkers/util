package com.outworkers.util.testing

import com.outworkers.util.domain.GenerationDomain
import org.scalacheck.Gen

import scala.collection.generic.CanBuildFrom

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

  def genOpt[T : Sample]: Option[T] = Some(implicitly[Sample[T]].sample)

  /**
    * Generates a list of elements based on an input collection type.
    * @param size The number of elements to generate
    * @param cbf The implicit builder
    * @tparam M The type of collection to build
    * @tparam T The type of the underlying sampled type.
    * @return A Collection of "size" elements with type T.
    */
  def gen[M[X] <: TraversableOnce[X], T](size: Int = defaultGeneration)(
    implicit cbf: CanBuildFrom[Nothing, T, M[T]],
    sampler: Sample[T]
  ): M[T] = {
    val builder = cbf()
    builder.sizeHint(size)
    for (_ <- 1 to size) builder += gen[T]
    builder.result()
  }

  def genList[T : Sample](size: Int = defaultGeneration): List[T] = gen[List, T](size)

  def genSet[T : Sample](size: Int = defaultGeneration): Set[T] = gen[Set, T](size)

  def genMap[T, A1, A2](size: Int = defaultGeneration)(
    implicit ev: T <:< (A1, A2),
    sampler: Sample[T]
  ): Map[A1, A2] = {
    gen[List, T](size).toMap[A1, A2]
  }

  def oneOf[T](list: Seq[T]): T = Gen.oneOf(list).sample.get

  def oneOf[T <: Enumeration](enum: T): T#Value = oneOf(enum.values.toList)
}

object Generators extends Generators

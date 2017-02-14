package com.outworkers.util.samplers

import org.scalacheck.Gen

import scala.collection.generic.CanBuildFrom
import _root_.com.outworkers.util.domain.GenerationDomain

import scala.util.Random

trait Generators extends GenerationDomain {

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

  def genMap[A1 : Sample, A2 : Sample](size: Int = defaultGeneration)(
    implicit cbf: CanBuildFrom[Nothing, (A1, A2), Map[A1, A2]]
  ): Map[A1, A2] = {
    val builder = cbf()
    builder.sizeHint(size)

    for (_ <- 1 to size) builder += gen[A1] -> gen[A2]

    builder.result()
  }

  def oneOf[T](list: Seq[T]): T = Gen.oneOf(list).sample.get

  def oneOf[T <: Enumeration](enum: T): T#Value = oneOf(enum.values.toList)
}

object Generators extends Generators

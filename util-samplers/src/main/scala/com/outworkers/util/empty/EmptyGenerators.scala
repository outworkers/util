package com.outworkers.util.empty

import org.scalacheck.Gen

import scala.collection.generic.CanBuildFrom

trait EmptyGenerators {

  val defaultGeneration = 5

  /**
    * Uses the type class available in implicit scope to mock a certain custom object.
    * @tparam T The parameter to mock.
    * @return A sample of the given type generated using the implicit sampler.
    */
  def void[T : Empty]: T = implicitly[Empty[T]].sample

  def voidOpt[T]: Option[T] = Option.empty[T]
  def voidMap[K : Empty, V: Empty]: Map[K, V] = Map.empty[K, V]

  /**
    * Generates a list of elements based on an input collection type.
    * @param cbf The implicit builder
    * @tparam M The type of collection to build
    * @tparam T The type of the underlying sampled type.
    * @return A Collection of "size" elements with type T.
    */
  def void[M[X] <: TraversableOnce[X], T](size: Int = 0)(
    implicit cbf: CanBuildFrom[Nothing, T, M[T]]
  ): M[T] = cbf().result()

  def oneOf[T](list: Seq[T]): T = Gen.oneOf(list).sample.get

  def oneOf[T <: Enumeration](enum: T): T#Value = oneOf(enum.values.toList)
}

object EmptyGenerators extends EmptyGenerators

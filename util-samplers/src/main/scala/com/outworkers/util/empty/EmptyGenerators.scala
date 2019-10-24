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

  def oneOf[T](list: Seq[T]): T = Gen.oneOf(list).sample.get

  def oneOf[T <: Enumeration](enum: T): T#Value = oneOf(enum.values.toList)
}

object EmptyGenerators extends EmptyGenerators

package com.outworkers.util.samplers

import org.scalacheck.Gen

import scala.collection.generic.CanBuildFrom

import scala.util.Random

trait Generators {

  protected[this] val domains = List("net", "com", "org", "io", "biz", "co.uk", "co.za")
  protected[this] val protocols = List("http", "https")

  val defaultGeneration = 5

  /**
   * Uses the type class available in implicit scope to mock a certain custom object.
   * @tparam T The parameter to mock.
   * @return A sample of the given type generated using the implicit sampler.
   */
  def gen[T : Sample]: T = implicitly[Sample[T]].sample

  def genOpt[T : Sample]: Option[T] = {
    val bool = Random.nextBoolean()

    if (bool) {
      Some(implicitly[Sample[T]].sample)
    } else {
      None
    }
  }

  def getConstOpt[T : Sample]: Option[T] = {
    Some(gen[T])
  }


  def genList[T : Sample](size: Int = defaultGeneration): List[T] = List.fill(size)(gen[T])

  def genSet[T : Sample](size: Int = defaultGeneration): Set[T] = genList[T](size).toSet

  def genMap[A1 : Sample, A2 : Sample](size: Int = defaultGeneration): Map[A1, A2] = {
    List.fill[(A1, A2)](size) { gen[A1] -> gen[A2] } toMap
  }

  def oneOf[T](list: Seq[T]): T = Gen.oneOf(list).sample.get

  def oneOf[T <: Enumeration](enum: T): T#Value = oneOf(enum.values.toList)
}

object Generators extends Generators

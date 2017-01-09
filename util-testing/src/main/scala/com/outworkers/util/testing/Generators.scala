package com.outworkers.util.testing

import com.outworkers.util.domain.GenerationDomain
import org.scalacheck.Gen

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

  def genList[T : Sample](size: Int = defaultGeneration): List[T] = List.tabulate(size)(_ => gen[T])

  def genSet[T : Sample](size: Int = defaultGeneration): Set[T] = genList[T](size).toSet[T]

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

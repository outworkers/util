package com.websudos.util.testing

import java.util.UUID

import scala.util.Random

import org.scalatest.Tag

private[testing] object Sampler {

  def email(domain: String = "test"): String = {
    s"${string.substring(0, 8)}@$domain.com"
  }

  /**
   * Get a unique random generated string.
   * This uses the default java GUID implementation.
   * @return A random string with 64 bits of randomness.
   */
  def string: String = {
    UUID.randomUUID().toString
  }

  /**
   * Returns a pseudo-random number between min and max, inclusive.
   * The difference between min and max can be at most
   * <code>Integer.MAX_VALUE - 1</code>.
   *
   * @param min Minimum value
   * @param max Maximum value.  Must be greater than min.
   * @return Integer between min and max, inclusive.
   * @see java.util.Random#nextInt(int)
   */
  def int(min: Int = 1, max: Int = Int.MaxValue): Int = {
    val rand = new Random()
    rand.nextInt((max - min) + 1) + min
  }
}

trait DefaultTags {
  object DatabaseTest extends Tag("com.websudos.testing.tags.DatabaseTest")
  object ApiTest extends Tag("com.websudos.testing.tags.ApiTest")
  object RequestParserTest extends Tag("com.websudos.testing.tags.RequestParserTest")
  object UnstableTest extends Tag("com.websudos.testing.tags.UnstableTest")

}



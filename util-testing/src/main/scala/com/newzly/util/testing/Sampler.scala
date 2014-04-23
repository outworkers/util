package com.newzly.util.testing

import java.util.UUID
import scala.util.Random
import org.scalatest.Tag

object Sampler {

  def uuid(str: String) : UUID = UUID.fromString(str)

  final val universalPassword = "77fe656a9fa329d8711f438b074c73bb2c215d0af347efc0bcdd7f901e463f44"

  def getAUniqueEmailAddress: String = {
    s"${getARandomString.substring(0, 8)}@newzlytest.com"
  }

  /**
   * Get a unique random generated string.
   * This uses the default java GUID implementation.
   * @return A random string with 64 bits of randomness.
   */
  def getARandomString: String = {
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
  def getARandomInteger(min: Int = 1, max: Int = Int.MaxValue): Int = {
    val rand = new Random()
    rand.nextInt((max - min) + 1) + min
  }

  /**
   * Get a unique random generated string.
   * This uses the default java GUID implementation.
   * @return A random string with 64 bits of randomness.
   */
  @deprecated(message = "Use getARandomString instead", "0.0.27")
  def getAUniqueString: String = {
    UUID.randomUUID().toString
  }
}

object DatabaseTest extends Tag("com.newzly.testing.tags.DatabaseTest")
object ApiTest extends Tag("com.newzly.testing.tags.ApiTest")
object RequestParserTest extends Tag("com.newzly.testing.tags.RequestParserTest")
object UnstableTest extends Tag("com.newzly.testing.tags.UnstableTest")
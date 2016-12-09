/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.util.testing

import java.util.UUID

import org.fluttercode.datafactory.impl.DataFactory

import scala.util.Random

import org.scalatest.Tag

private[testing] object Sampler {

  private[testing] lazy val factory = new DataFactory

  def email(domain: String = "test"): String = {
   factory.getEmailAddress
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



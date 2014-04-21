package com.newzly.util.testing

import java.util.UUID
import org.scalatest.Tag

object Sampler {

  def uuid(str: String) : UUID = UUID.fromString(str)

  final val universalPassword = "77fe656a9fa329d8711f438b074c73bb2c215d0af347efc0bcdd7f901e463f44"

  def getAUniqueEmailAddress: String = {
    s"${getARandomString().substring(0, 8)}@newzlytest.com"
  }

  def getARandomString(): String = {
    UUID.randomUUID().toString
  }

}

object DatabaseTest extends Tag("com.newzly.testing.tags.DatabaseTest")
object ApiTest extends Tag("com.newzly.testing.tags.ApiTest")
object RequestParserTest extends Tag("com.newzly.testing.tags.RequestParserTest")
object UnstableTest extends Tag("com.newzly.testing.tags.UnstableTest")
package com.newzly.util.testing

import java.util.UUID

object Sampler {

  def uuid(str: String) : UUID = UUID.fromString(str)

  final val universalPassword = "77fe656a9fa329d8711f438b074c73bb2c215d0af347efc0bcdd7f901e463f44"

  def getAUniqueEmailAddress: String = {
    s"${UUID.randomUUID().toString.substring(0, 8)}@newzlytest.com"
  }

}

package com.newzly.core

import java.util.UUID
import org.scalatest.{ FlatSpec, Matchers }
import com.newzly.util.core.PasswordHash

class PasswordHashingTest extends FlatSpec with Matchers {

  it should "correctly hash a password and decode" in {
    val password = UUID.randomUUID().toString
    val hashed = PasswordHash.createHash(password)
    PasswordHash.validatePassword(password, hashed) shouldBe true
  }

}

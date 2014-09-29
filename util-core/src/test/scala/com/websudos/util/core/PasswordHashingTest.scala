package com.websudos.util.core

import java.util.UUID

import com.websudos.core.PasswordHash

class PasswordHashingTest extends FlatSpec with Matchers {

  it should "correctly hash a password and decode" in {
    val password = UUID.randomUUID().toString
    val hashed = PasswordHash.createHash(password)
    PasswordHash.validatePassword(password, hashed) shouldBe true
  }

}

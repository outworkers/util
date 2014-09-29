package com.websudos.util.core

import java.util.UUID

import org.scalatest.{Matchers, FlatSpec}

class PasswordHashingTest extends FlatSpec with Matchers {

  it should "correctly hash a password and decode" in {
    val password = UUID.randomUUID().toString
    val hashed = PasswordHash.createHash(password)
    PasswordHash.validatePassword(password, hashed) shouldEqual true
  }

}

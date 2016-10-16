package com.outworkers.util.testing

import java.util.UUID

@sample case class User(
  id: UUID,
  firstName: String,
  lastName: String,
  email: String
)

object User {
  val x = 5
}

@sample case class CollectionSample(
  id: UUID,
  firstName: String,
  lastName: String,
  name: String,
  email: String,
  tests: List[String],
  sets: List[String],
  map: Map[String, String],
  emails: List[String]
)
package com.outworkers.util.testing

import java.util.UUID

@sample case class User(
  id: UUID,
  firstName: FirstName,
  lastName: LastName,
  email: EmailAddress
)
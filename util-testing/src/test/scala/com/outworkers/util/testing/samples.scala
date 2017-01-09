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

case class User(
  id: UUID,
  firstName: String,
  lastName: String,
  email: String
)

case class CollectionSample(
  id: UUID,
  firstName: String,
  lastName: String,
  sh: Short,
  name: String,
  email: String,
  tests: List[String],
  sets: List[String],
  map: Map[String, String],
  emails: List[String],
  opt: Option[Int]
)

case class TupleRecord(id: UUID, tp: (String, Long))
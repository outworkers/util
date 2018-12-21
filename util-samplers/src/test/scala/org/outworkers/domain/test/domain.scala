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
package org.outworkers.domain.test

import java.util.UUID

case class OtherPackageExample(
 id: UUID,
 name: String,
 firstName: String,
 email: String,
 sample: Int,
 double: Double,
 bigDec: BigDecimal,
 long: Long
)

case class SimpleFoo(name: Option[String])

case class NestedCollections(
  id: UUID,
  text: String,
  nestedList: List[List[String]],
  nestedListSet: List[Set[String]],
  props: Map[String, List[String]],
  doubleProps: Map[Set[String], List[String]]
)

case class NestedOptions(
  id: Option[UUID],
  name: Option[String],
  firstName: Option[String],
  user: Option[User],
  collections: Option[CollectionSample]
)

import java.util.UUID

import org.outworkers.domain.test._

case class User(
  id: UUID,
  firstName: String,
  lastName: String,
  email: String
)

case class NestedUser(
  timestamp: Long,
  user: User
)

case class CollectionSample(
  id: UUID,
  firstName: String,
  lastName: String,
  sh: Short,
  b: Byte,
  name: String,
  email: String,
  tests: List[String],
  sets: List[String],
  map: Map[String, String],
  emails: List[String],
  opt: Option[Int]
)

case class TupleRecord(id: UUID, tp: (String, Long))

case class TupleCollectionRecord(id: UUID, tuples: List[(Int, String)])

case class NestedOtherPackage(
  id: UUID,
  otherPkg: OtherPackageExample,
  emails: List[String]
)

trait RoleType extends Enumeration {
  //represents built-in role types.
  type RoleType = Value

  val Leader = Value("leader")
  val AllianceMember = Value("member")
}

object RoleType extends RoleType

case class Membership(
  memberId: String,
  entityType: String,
  allianceId: String,
  role: RoleType.Value = RoleType.Leader,
  rankId: String
)
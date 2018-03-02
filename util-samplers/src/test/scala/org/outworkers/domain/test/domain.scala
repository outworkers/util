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
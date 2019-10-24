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
package com.outworkers.util.empty

import org.outworkers.domain.test._
import org.scalatest.{FlatSpec, Matchers}

class EmptyGeneratorsTest extends FlatSpec with Matchers {

  it should "generate a sized list based on the given argument" in {
    assert(void[List[String]].isEmpty)
  }

  it should "generate a sized map based on the given size argument" in {
    assert(voidMap[String, String].isEmpty)
  }

  it should "generate a sized map of known key and value types" in {
    assert(voidMap[String, String].isEmpty)
  }

  it should "automatically derive valid samples" in {
    val sample = void[User]
  }

  it should "automatically derive generator samples for complex case classes" in {
    val sample = void[CollectionSample]
    val user = void[User]
    val tp = void[TupleRecord]
    val tpColl = void[TupleCollectionRecord]
  }

  it should "automatically derive a generator for a nested case class in a different package" in {
    "val sample = void[NestedOtherPackage]" should compile
  }

  it should "automatically derive a sample for a nested case class" in {
    val sample = void[NestedUser]
    sample shouldEqual sample
  }

  it should "automatically derive samplers for nested collections" in {
    val sample = void[List[List[String]]]
    sample shouldEqual sample
  }


  it should "automatically derive dictionaries for nested options" in {
    val sample = void[SimpleFoo]
    sample shouldEqual sample
  }

  it should "automatically sample nested collections" in {

    val sample = void[NestedCollections]
    Console.println(sample.trace())
    sample shouldEqual sample
  }

  it should "automatically generate a sampler for random collection" in {
    val sample = void[IndexedSeq[String]]
    sample shouldEqual sample
  }


  it should "automatically generate a sampler for a nested Enumeration inside a CaseClass" in {
    val sample = void[IndexedSeq[String]]
    sample shouldEqual sample
  }
}

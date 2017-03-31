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
package com.outworkers.util.samplers

import org.scalatest.{ FlatSpec, Matchers }

class TracerTests extends FlatSpec with Matchers {

  it should "automatically derive a tracer for a simple type" in {
    val sample = gen[User]
    sample.trace()
    """sample.trace()""" should compile
  }

  it should "automatically derive a tracer for a nested type" in {
    val sample = gen[NestedUser]
    sample.trace()
    """sample.trace()""" should compile
  }

  it should "automatically derive a tracer for a nested tuple type" in {
    val sample = gen[TupleRecord]
    sample.trace()
    """sample.trace()""" should compile
  }

  it should "automatically derive a tracer for a nested tuple collection type" in {
    val sample = gen[TupleCollectionRecord]
    sample.trace()
    """sample.trace()""" should compile
  }

  it should "automatically derive a tracer for a type with collections" in {
    val sample = gen[CollectionSample]
    sample.trace()
    """sample.trace()""" should compile
  }

}

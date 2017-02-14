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
package com.outworkers.util.lift

import com.outworkers.util.samplers.Sample
import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.util.samplers._

case class Test(name: String, amount: Int)

trait TagType extends Enumeration {
  val language = Value("language")
  val framework = Value("framework")
}

object TagType extends TagType

case class Tag(
  name: String,
  tagType: TagType#Value
)

class LiftTest extends FlatSpec with Matchers {

  case class TestClass(name: String)

  implicit object TestClassSampler extends Sample[TestClass] {
    def sample: TestClass = TestClass(gen[String])
  }
}

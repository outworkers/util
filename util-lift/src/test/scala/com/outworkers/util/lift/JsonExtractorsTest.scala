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

import net.liftweb.json.{DefaultFormats, JsonParser}
import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.util.testing._

class JsonExtractorsTest extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats


  it should "serialise a case class to a JSON string" in {
    val data = Test("test", 2)

    data.asJson() shouldEqual """ {"name": "test", "amount": 2}""".stripMargin.replaceAll("\\s", "")
  }

  it should "re-parse a case class serialised to a string" in {
    val data = Test("test", 2)

    shouldNotThrow {
      JsonParser.parse(data.asJson()).extract[Test] shouldEqual data
    }
  }

  it should "re-parse a case class from a serialised JValue" in {
    val data = Test("test", 2)

    shouldNotThrow {
      data.asJValue().extract[Test] shouldEqual data
    }
  }

  it should "pretty print a case class to JSON" in {
    val data = Test("test2", 2)

    """data.asPrettyJson()""" should compile
    info(data.asPrettyJson())
  }

  it should "pretty print a Seq of case classes to JSON" in {
    val data = Seq(Test("test2", 2))
    """data.asPrettyJson()""" should compile
    info(data.asPrettyJson())
  }

  it should "pretty print a Set of case classes to JSON" in {
    val data = Set(Test("test2", 2), Test("test3", 3))
    """data.asPrettyJson()""" should compile
    info(data.asPrettyJson())
  }
}

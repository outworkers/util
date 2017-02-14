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

import net.liftweb.json._
import org.scalatest.{Matchers, FlatSpec}
import com.outworkers.util.testing._

class EnumerationSerializerTest extends FlatSpec with Matchers {


  implicit val formats = net.liftweb.json.DefaultFormats + EnumNameSerializer[TagType](TagType)

  it should "serialize an enumeration value to a string" in {
    val tag = Tag("test", TagType.language)

    shouldNotThrow {
      compactRender(Extraction.decompose(tag))
    }
  }

  it should "parse a tagType enumeration value from a json string" in {
    val tag = Tag("test", TagType.language)
    val json = compactRender(Extraction.decompose(tag))

    shouldNotThrow {
      val reparsed = JsonParser.parse(json).extract[Tag]

      // reparsed.isDefined shouldEqual true
      reparsed.tagType shouldEqual tag.tagType
    }
  }

}

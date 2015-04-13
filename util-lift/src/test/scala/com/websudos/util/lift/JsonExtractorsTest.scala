package com.websudos.util.lift

import net.liftweb.json.{JsonParser, DefaultFormats}
import org.scalatest.{Matchers, FlatSpec}
import com.websudos.util.testing._
case class Test(name: String, amount: Int)

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
}

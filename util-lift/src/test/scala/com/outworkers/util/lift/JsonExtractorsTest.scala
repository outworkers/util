package com.outworkers.util.lift

import com.outworkers.util.testing._
import net.liftweb.json.{DefaultFormats, JsonParser}
import org.scalatest.{FlatSpec, Matchers}


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

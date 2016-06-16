package com.outworkers.util.lift

import net.liftweb.json._
import org.scalatest.{Matchers, FlatSpec}
import com.outworkers.util.testing._

trait TagType extends Enumeration {
  val language = Value("language")
  val framework = Value("framework")
}

object TagType extends TagType

case class Tag(
  name: String,
  tagType: TagType#Value
)


class EnumerationSerializerTest extends FlatSpec with Matchers {


  implicit val formats = net.liftweb.json.DefaultFormats + EnumNameSerializer[TagType](TagType)

  it should "serialize an enumeration value to a string" in {
    val tag = new Tag("test", TagType.language)

    shouldNotThrow {
      val json = compactRender(Extraction.decompose(tag))
    }
  }

  it should "parse a tagType enumeration value from a json string" in {
    val tag = new Tag("test", TagType.language)
    val json = compactRender(Extraction.decompose(tag))

    shouldNotThrow {
      val reparsed = JsonParser.parse(json).extract[Tag]

      // reparsed.isDefined shouldEqual true
      reparsed.tagType shouldEqual tag.tagType
    }
  }

}

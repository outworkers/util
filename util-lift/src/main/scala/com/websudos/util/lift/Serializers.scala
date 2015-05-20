package com.websudos.util.lift

import java.util.UUID
import scala.util.control.NonFatal

import net.liftweb.json.JsonAST.{JString, JValue}
import net.liftweb.json._

sealed class UUIDSerializer extends Serializer[UUID] {
  private val Class = classOf[UUID]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), UUID] = {
    case (TypeInfo(Class, _), json) => json match {
      case JString(value) => try {
        UUID.fromString(value)
      }  catch {
        case NonFatal(err) => {
          val exception =  new MappingException(s"Couldn't extract an UUID from $value")
          exception.initCause(err)
          throw exception
        }
      }
      case x => throw new MappingException("Can't convert " + x + " to UUID")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: UUID => JString(x.toString)
  }
}


trait CustomSerializers {
  implicit val formats = Serialization.formats(NoTypeHints) + new UUIDSerializer
}
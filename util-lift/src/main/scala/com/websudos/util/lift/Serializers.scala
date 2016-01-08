package com.websudos.util.lift

import java.util.UUID

import net.liftweb.json.JsonAST.{JString, JValue}
import net.liftweb.json._
import org.joda.time.DateTime

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

sealed class UUIDSerializer extends Serializer[UUID] {
  private[this] val Class = classOf[UUID]

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

class EnumSerializer[E <: Enumeration: ClassTag](enum: E)
  extends Serializer[E#Value] {
  import JsonDSL._

  val EnumerationClass = classOf[E#Value]

  def deserialize(implicit format: Formats):
  PartialFunction[(TypeInfo, JValue), E#Value] = {
    case (TypeInfo(EnumerationClass, _), json) => json match {
      case JInt(value) if value <= enum.maxId => enum(value.toInt)
      case value => throw new MappingException("Can't convert " +
        value + " to "+ EnumerationClass)
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: E#Value => i.id
  }
}

sealed class DateTimeSerializer extends Serializer[DateTime] {

  val DateTimeClass = classOf[DateTime]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateTime] = {
    case (TypeInfo(DateTimeClass, _), json) => json match {
      case JString(value) =>
        Try {
          new DateTime(value.toLong)
        } match {
          case Success(dt) => dt
          case Failure(err) => {
            val exception =  new MappingException(s"Couldn't extract a DateTime from $value")
            exception.initCause(err)
            throw exception
          }
        }
      case x => throw new MappingException("Can't convert " + x + " to DateTime")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: DateTime => JString(x.getMillis.toString)
  }
}


class EnumNameSerializer[E <: Enumeration: ClassTag](enum: E)
  extends Serializer[E#Value] {
  import JsonDSL._

  val EnumerationClass = classOf[E#Value]

  def deserialize(implicit format: Formats):
  PartialFunction[(TypeInfo, JValue), E#Value] = {
    case (TypeInfo(EnumerationClass, _), json) => json match {
      case JString(value) if enum.values.exists(_.toString == value) =>
        enum.withName(value)
      case value => throw new MappingException("Can't convert " +
        value + " to "+ EnumerationClass)
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: E#Value => i.toString
  }
}


object EnumNameSerializer {
  def apply[E <: Enumeration : ClassTag](enum: E): EnumNameSerializer[E] = {
    new EnumNameSerializer(enum)
  }
}


trait CustomSerializers {
  implicit val formats = Serialization.formats(NoTypeHints) + new UUIDSerializer + new DateTimeSerializer
}
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
package com.outworkers.util.testing

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.outworkers.util.domain.Definitions
import com.outworkers.util.macros.AnnotationToolkit
import org.joda.time.DateTime

import scala.collection.concurrent.TrieMap

@macrocompat.bundle
class SamplerMacro(override val c: scala.reflect.macros.blackbox.Context) extends AnnotationToolkit(c) {

  import c.universe._

  /**
    * Adds a caching layer for subsequent requests to materialise the same primitive type.
    * This adds a simplistic caching layer that computes primitives based on types.
    */
  val treeCache: TrieMap[Symbol, Tree] = TrieMap.empty[Symbol, Tree]

  val prefix = q"com.outworkers.util.testing"
  val domainPkg = q"com.outworkers.util.domain.GenerationDomain"
  val definitions = "com.outworkers.util.domain.Definitions"

  object SamplersSymbols {
    val intSymbol: Symbol = typed[Int]
    val byteSymbol: Symbol = typed[Byte]
    val stringSymbol: Symbol = typed[String]
    val boolSymbol: Symbol = typed[Boolean]
    val shortSymbol: Symbol = typed[Short]
    val longSymbol: Symbol = typed[Long]
    val doubleSymbol: Symbol = typed[Double]
    val floatSymbol: Symbol = typed[Float]
    val dateSymbol: Symbol = typed[Date]
    val shortString: Symbol= typed[ShortString]
    val listSymbol: Symbol = typed[scala.collection.immutable.List[_]]
    val setSymbol: Symbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol: Symbol = typed[scala.collection.immutable.Map[_, _]]
    val dateTimeSymbol: Symbol = typed[DateTime]
    val uuidSymbol: Symbol = typed[UUID]
    val jodaLocalDateSymbol: Symbol = typed[org.joda.time.LocalDate]
    val inetSymbol: Symbol = typed[InetAddress]
    val bigInt: Symbol = typed[BigInt]
    val bigDecimal: Symbol = typed[BigDecimal]
    val optSymbol: Symbol = typed[Option[_]]
    val buffer: Symbol = typed[ByteBuffer]
    val enum: Symbol = typed[Enumeration#Value]
    val firstName: Symbol = typed[FirstName]
    val lastName: Symbol = typed[LastName]
    val fullName: Symbol = typed[Definitions.FullName]
    val emailAddress: Symbol = typed[EmailAddress]
    val city: Symbol = typed[City]
    val country: Symbol = typed[Country]
    val countryCode: Symbol = typed[CountryCode]
    val programmingLanguage: Symbol = typed[ProgrammingLanguage]
    val url: Symbol = typed[Url]
  }

  // val example: String => gen[String]
  // val firstName: String => gen[FirstName].value
  // val lastName: Option[String] => genOpt[LastName].map(_.value)
  // val emails: List[String] => genList[EmailAddress].map(_.value)
  // val sample: List[String] => genList[String]

  def extract(exp: Tree): Option[TypeName] = {
    Some(c.typecheck(exp, c.TYPEmode).tpe.typeSymbol.name.toTypeName)
  }

  object KnownField {
    def unapply(nm: TermName): Option[TypeName] = unapply(nm.decodedName.toString)

    def unapply(str: String): Option[TypeName] = {
      str.toLowerCase() match {
        case "first_name" | "firstname" => extract(q"$domainPkg.FirstName")
        case "last_name" | "lastname" => extract(q"$domainPkg.LastName")
        case "name" | "fullname" | "fullName" | "full_name" => extract(q"$domainPkg.FullName")
        case "email" | "email_address" | "emailaddress" => extract(q"$domainPkg.EmailAddress")
        case "country" => extract(q"$domainPkg.CountryCode")
        case _ => None
      }
    }
  }

  /**
    * Describes a complex collection type.
    * The source types are the type arguments that will be needed to produce the collection.
    * We have more than one because in the case of a Map for instance we could have multiple ones.
    *
    * The applier function is a way to produce a new type of the same collection type given an input type.
    * It's useful because we often want to derive the types of the generators based on the name of the field,
    * to offer the auto-replacement funtionality for collections.
    *
    * @param sources The source types that are needed to produce the collection type.
    * @param applier The applier function that allows producing a collection type from a list of type arguments.
    */
  case class MapType(
    sources: List[Type],
    applier: List[Type] => TypeName,
    generator: List[TypeName] => Tree
  ) {
    def infer: TypeName = applier(sources)

    def default: Tree = generator(sources.map(sym => sym.typeSymbol.name.toTypeName))
  }

  object MapType {
    def apply(
      source: Type,
      applier: List[Type] => TypeName,
      generator: List[TypeName] => Tree
    ): Option[MapType] = Some(new MapType(source :: Nil, applier, generator))

    def unapply(arg: Accessor): Option[MapType] = {

      if (arg.symbol == SamplersSymbols.mapSymbol) {
        arg.typeArgs match {
          case keyType :: listType :: Nil =>   Some(
            MapType(
              List(keyType, listType),
              applied => TypeName(s"scala.collection.immutable.Map[..$applied]"),
              generator = types => q"$prefix.genMap[..$types]($prefix.defaultGeneration)"
            )
          )
          case _ => c.abort(c.enclosingPosition, "Failed to find 2 type arguments for Map type")
        }


      } else {
        None
      }
    }
  }

  case class CollectionType(
    source: Type,
    applier: Type => TypeName,
    generator: TypeName => Tree
  ) {
    def infer: TypeName = applier(source)

    def default: Tree = generator(source.typeSymbol.name.toTypeName)
  }

  object CollectionType {
    def unapply(arg: Accessor): Option[CollectionType] = {
      if (arg.symbol == SamplersSymbols.listSymbol) {
        arg.typeArgs match {
          case sourceTpe :: Nil => Some(
            CollectionType(
              source = sourceTpe,
              applier = applied => TypeName(s"$collectionPkg.List[..$applied]"),
              generator = tpe => q"$prefix.Sample.collection[$collectionPkg.List, $tpe]"
            )
          )
          case _ => c.abort(c.enclosingPosition, "Could not extract inner type argument of List.")
        }
      } else if (arg.symbol == SamplersSymbols.setSymbol) {
        arg.typeArgs match {
          case sourceTpe :: Nil => Some(
            CollectionType(
              source = sourceTpe,
              applier = applied => TypeName(s"$collectionPkg.Set[..$applied]"),
              generator = tpe => q"$prefix.Sample.collection[$collectionPkg.Set, $tpe]"
            )
          )
          case _ => c.abort(c.enclosingPosition, "Could not extract inner type argument of Set.")
        }
      } else {
        None
      }
    }
  }

  case class OptionType(
    source: Type,
    applier: Type => TypeName,
    generator: TypeName => Tree
  ) {
    def infer: TypeName = applier(source)

    def default: Tree = generator(source.typeSymbol.name.toTypeName)
  }

  object OptionType {
    def unapply(arg: Accessor): Option[(OptionType)] = {

      if (arg.symbol == SamplersSymbols.optSymbol) {
        arg.typeArgs match {
          case head :: Nil => Some(
            OptionType(
              source = head,
              applier = applied => TypeName(s"scala.Option[$applied]"),
              generator = tpe => q"""$prefix.genOpt[$head]"""
            )
          )
          case _ => c.abort(c.enclosingPosition, s"Expected a single type argument for Option[_], found ${arg.typeArgs.size} instead")
        }
      } else {
        None
      }
    }
  }

  private[this] def deriveSamplerType(accessor: Accessor): Tree = {
    accessor match {
      case MapType(col) => col.default
      case OptionType(opt) => accessor.name match {
        case KnownField(derived) => opt.generator(derived)
        case _ => opt.default
      }
      case CollectionType(col) => accessor.name match {
        case KnownField(derived) => col.generator(derived)
        case _ => col.default
      }

      case _ => accessor.name match {
        case KnownField(derived) => q"$prefix.Sample.apply[$derived].sample.value"
        case _ => q"$prefix.Sample.apply[${accessor.paramType}].sample"
      }
    }
  }

  def caseClassSample(
    tpe: Type
  ): Tree = {
    val applies = caseFields(tpe).map { a => {
      q"${a.name} = ${deriveSamplerType(a)}"
    } }

    q"""
      new $prefix.Sample[$tpe] {
        override def sample: $tpe = ${tpe.typeSymbol.name.toTermName}.apply(..$applies)
      }
    """
  }

  def listSample(tpe: Type): Tree = {
    tpe.typeArgs match {
      case inner :: Nil => {
        q"""
          new $prefix.Sample[$tpe] {
            override def sample: $tpe = $prefix.Generate.genList[${inner.typeSymbol.typeSignatureIn(tpe)}]()
          }
        """
      }
      case _ => c.abort(c.enclosingPosition, "Expected a single type argument for type List")
    }
  }

  def tupleSample(tpe: Type): Tree = {
    val comp = tpe.typeSymbol.name.toTermName

    val samplers = tpe.typeArgs.map(t => q"$prefix.Sample[$t].sample")

    q"""
      new $prefix.Sample[$tpe] {
        override def sample: $tpe = $comp.apply(..$samplers)
      }
    """
  }

  def mapSample(tpe: Type): Tree = {
    tpe.typeArgs match {
      case k :: v :: Nil =>
        q"""
          new $prefix.Sample[$tpe] {
            override def sample: $tpe = $prefix.Generate.genMap[$k, $v]()
          }
        """
      case _ => c.abort(c.enclosingPosition, "Expected exactly two type arguments to be provided to map")
    }
  }

  def setSample(tpe: Type): Tree = {
    tpe.typeArgs match {
      case inner :: Nil =>
        q"""
          new $prefix.Sample[$tpe] {
           override def sample: $tpe = $prefix.Generate.getList[$inner]().toSet
          }
        """
      case _ => c.abort(c.enclosingPosition, "Expected inner type to be defined")
    }
  }

  def enumPrimitive(tpe: Type): Tree = {
    val comp = c.parse(s"${tpe.toString.replace("#Value", "")}")

    q"""
      new $prefix.Sample[$tpe] {
        override def sample: $tpe = $prefix.Sample.oneOf($comp)
      }
    """
  }

  def sampler(nm: String): Tree = q"new $prefix.Samples.${TypeName(nm)}"

  def materialize[T : c.WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]
    val symbol = tpe.typeSymbol

    val tree = symbol match {
      case sym if isTuple(tpe) => tupleSample(tpe)
      case SamplersSymbols.enum => treeCache.getOrElseUpdate(typed[T], enumPrimitive(tpe))
      case SamplersSymbols.listSymbol => treeCache.getOrElseUpdate(typed[T], listSample(tpe))
      case SamplersSymbols.setSymbol => treeCache.getOrElseUpdate(typed[T], setSample(tpe))
      case SamplersSymbols.mapSymbol => treeCache.getOrElseUpdate(typed[T], mapSample(tpe))
      case SamplersSymbols.stringSymbol => sampler("StringSampler")
      case SamplersSymbols.shortSymbol => sampler("ShortSampler")
      case SamplersSymbols.boolSymbol => sampler("BooleanSampler")
      case SamplersSymbols.byteSymbol => sampler("ByteSampler")
      case SamplersSymbols.dateSymbol => sampler("DateSampler")
      case SamplersSymbols.floatSymbol => sampler("FloatSampler")
      case SamplersSymbols.longSymbol => sampler("LongSampler")
      case SamplersSymbols.intSymbol => sampler("IntSampler")
      case SamplersSymbols.shortString => sampler("ShortStringSampler")
      case SamplersSymbols.doubleSymbol => sampler("DoubleSampler")
      case SamplersSymbols.bigInt => sampler("BigIntSampler")
      case SamplersSymbols.bigDecimal => sampler("BigDecimalSampler")
      case SamplersSymbols.dateTimeSymbol => sampler("DateTimeSampler")
      case SamplersSymbols.jodaLocalDateSymbol => sampler("JodaLocalDateSampler")
      case SamplersSymbols.inetSymbol => sampler("InetAddressSampler")
      case SamplersSymbols.uuidSymbol => sampler("UUIDSampler")
      case SamplersSymbols.firstName => sampler("FirstNameSampler")
      case SamplersSymbols.lastName => sampler("LastNameSampler")
      case SamplersSymbols.fullName =>sampler("FullNameSampler")
      case SamplersSymbols.emailAddress => sampler("EmailAddressSampler")
      case SamplersSymbols.city => sampler("CitySampler")
      case SamplersSymbols.country => sampler("CountrySampler")
      case SamplersSymbols.countryCode => sampler("CountryCodeSampler")
      case SamplersSymbols.programmingLanguage => sampler("ProgrammingLanguageSampler")
      case SamplersSymbols.url => sampler("UrlSampler")
      case sym if sym.isClass && sym.asClass.isCaseClass => treeCache.getOrElseUpdate(typed[T], caseClassSample(tpe))
      case _ => c.abort(c.enclosingPosition, s"Cannot derive sampler implementation for $tpe")
    }

    println(showCode(tree))
    tree
  }
}
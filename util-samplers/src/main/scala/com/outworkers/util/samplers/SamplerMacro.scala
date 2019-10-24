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

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import _root_.com.outworkers.util.macros.{AnnotationToolkit, BlackboxToolbelt}

import scala.reflect.macros.blackbox

@macrocompat.bundle
class SamplerMacro(val c: blackbox.Context) extends AnnotationToolkit with BlackboxToolbelt {

  import c.universe._
  lazy val fillOptions = typeOf[com.outworkers.util.samplers.FillOptions]

  val prefix = q"com.outworkers.util.samplers"
  val domainPkg = q"com.outworkers.util.domain"
  val definitions = "com.outworkers.util.domain"

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
    val uuidSymbol: Symbol = typed[UUID]
    val inetSymbol: Symbol = typed[InetAddress]
    val bigInt: Symbol = typed[BigInt]
    val bigDecimal: Symbol = typed[BigDecimal]
    val optSymbol: Symbol = typed[Option[_]]
    val buffer: Symbol = typed[ByteBuffer]
    val enum: Symbol = typed[Enumeration#Value]
    val firstName: Symbol = typed[FirstName]
    val lastName: Symbol = typed[LastName]
    val fullName: Symbol = typed[FullName]
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

  def extract(exp: Tree): Option[Type] = {
    Some(c.typecheck(exp, c.TYPEmode).tpe)
  }

  object KnownField {
    def unapply(nm: TermName): Option[Type] = unapply(nm.decodedName.toString)

    def unapply(str: String): Option[Type] = {
      str.toLowerCase() match {
        case "first_name" | "firstname" => extract(tq"$domainPkg.FirstName")
        case "last_name" | "lastname" => extract(tq"$domainPkg.LastName")
        case "name" | "fullname" | "fullName" | "full_name" => extract(tq"$domainPkg.FullName")
        case "email" | "email_address" | "emailaddress" => extract(tq"$domainPkg.EmailAddress")
        case "country" => extract(tq"$domainPkg.CountryCode")
        case _ => None
      }
    }
  }

  trait TypeExtractor {

    def sources: List[Type]

    def applier: List[Type] => TypeName

    def infer: TypeName = applier(sources)

    def generator: List[Type] => Tree

    def default: Tree = generator(sources)
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
    generator: List[Type] => Tree
  ) extends TypeExtractor

  object MapType {
    def apply(
      source: Type,
      applier: List[Type] => TypeName,
      generator: List[Type] => Tree
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
    sources: List[Type],
    applier: List[Type] => TypeName,
    generator: List[Type] => Tree
  ) extends TypeExtractor

  object CollectionType {
    def unapply(arg: Accessor): Option[CollectionType] = {
      if (arg.symbol == SamplersSymbols.listSymbol) {
        arg.typeArgs match {
          case sourceTpe :: Nil => Some(
            CollectionType(
              sources = sourceTpe :: Nil,
              applier = applied => TypeName(s"$collectionPkg.List[..$applied]"),
              generator = tpe => q"$prefix.Sample.collection[$collectionPkg.List, ..$tpe].sample"
            )
          )
          case _ => c.abort(c.enclosingPosition, "Could not extract inner type argument of List.")
        }
      } else if (arg.symbol == SamplersSymbols.setSymbol) {
        arg.typeArgs match {
          case sourceTpe :: Nil => Some(
            CollectionType(
              sources = sourceTpe :: Nil,
              applier = applied => TypeName(s"$collectionPkg.Set[..$applied]"),
              generator = tpe => q"$prefix.Sample.collection[$collectionPkg.Set, ..$tpe].sample"
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
    sources: List[Type],
    applier: List[Type] => TypeName,
    generator: List[Type] => Tree
  ) extends TypeExtractor

  object OptionType {
    def unapply(arg: Accessor): Option[(OptionType)] = {

      if (arg.symbol == SamplersSymbols.optSymbol) {
        arg.typeArgs match {
          case head :: Nil => {

            val fillOptionsImp = c.inferImplicitValue(fillOptions, silent = true)

            Some(
              OptionType(
                sources = head :: Nil,
                applier = applied => TypeName(s"scala.Option[..$applied]"),
                generator = t => {
                  if (fillOptionsImp.nonEmpty) {
                    q"""$fillOptionsImp($prefix.genOpt[..$t])"""
                  } else {
                    q"""$prefix.genOpt[..$t]"""
                  }
                }
              )
            )
          }
          case _ => c.abort(
            c.enclosingPosition,
            s"Expected a single type argument for Option[_], found ${arg.typeArgs.size} instead"
          )
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
        case KnownField(derived) if accessor.paramType =:= typeOf[String] => {

          val fillOptionsImp = c.inferImplicitValue(fillOptions, silent = true)

          if (fillOptionsImp.nonEmpty) {
            q"""$fillOptionsImp($prefix.genOpt[$derived]).map(_.value)"""
          } else {
            q"""$prefix.genOpt[$derived].map(_.value)"""
          }
        }
        case _ => opt.default
      }
      case CollectionType(col) => accessor.name match {
        case KnownField(derived) => col.generator(derived :: Nil)
        case _ => col.default
      }

      case _ => accessor.name match {
        case KnownField(derived) if accessor.paramType =:= typeOf[String] => {

          q"$prefix.gen[$derived].value"
        }
        case _ => q"$prefix.gen[${accessor.paramType}]"
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
        override def sample: $tpe = new $tpe(..$applies)
      }
    """
  }

  def tupleSample(tpe: Type): Tree = {
    val comp = tpe.typeSymbol.name.toTermName

    val samplers = tpe.typeArgs.map(t => q"$prefix.gen[$t]")

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
            override def sample: $tpe = $prefix.genMap[$k, $v]()
          }
        """
      case _ => c.abort(c.enclosingPosition, "Expected exactly two type arguments to be provided to map")
    }
  }

  def enumSample(tpe: Type): Tree = {
    val comp = c.parse(s"${tpe.toString.replace("#Value", "")}")

    q"""
      new $prefix.Sample[$tpe] {
        override def sample: $tpe = $prefix.oneOf($comp)
      }
    """
  }

  def sampler(nm: String): Tree = q"new $prefix.Sample.${TypeName(nm)}"

  def macroImpl(tpe: Type): Tree = {
    val symbol = tpe.typeSymbol

    val tree = symbol match {
      case SamplersSymbols.mapSymbol => mapSample(tpe)
      case sym if isTuple(tpe) => tupleSample(tpe)
      case SamplersSymbols.enum => enumSample(tpe)
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
      case sym if sym.isClass && sym.asClass.isCaseClass => caseClassSample(tpe)
      case _ => c.abort(c.enclosingPosition, s"Cannot derive sampler implementation for $tpe")
    }

    if (showTrees) {
      echo(showCode(tree))
    }

    if (showCache) {
      echo(BlackboxToolbelt.sampleCache.show)
    }

    tree
  }

  def materialize[T : WeakTypeTag]: Tree = {
    macroImpl(weakTypeOf[T])
  }
}

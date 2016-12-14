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

import com.outworkers.util.macros.AnnotationToolkit

@macrocompat.bundle
class SamplerMacro(override val c: scala.reflect.macros.blackbox.Context) extends AnnotationToolkit(c) {

  import c.universe._

  val prefix = q"com.outworkers.util.testing"
  val domainPkg = q"com.outworkers.util.domain.GenerationDomain"
  val collectionPkg = q"scala.collection.immutable"

  // val example: String => gen[String]
  // val firstName: String => gen[FirstName].value
  // val lastName: Option[String] => genOpt[LastName].map(_.value)
  // val emails: List[String] => genList[EmailAddress].map(_.value)
  // val sample: List[String] => genList[String]

  def extract(exp: Tree): Option[TypeName] = {
    Some(c.typecheck(exp, c.TYPEmode).tpe.typeSymbol.name.toTypeName)
  }

  object KnownField {
    def unapply(nm: TermName): Option[TypeName] = {
      val str = nm.decodedName.toString
      str.toLowerCase() match {
        case "first_name" | "firstname" => extract(q"$domainPkg.FirstName")
        case "last_name" | "lastname" => extract(q"$domainPkg.LastName")
        case "name" | "fullname" | "full_name" => extract(q"$domainPkg.FullName")
        case "email" | "email_address" | "emailaddress" => extract(q"$domainPkg.EmailAddress")
        case "country" => extract(q"$domainPkg.CountryCode")
        case _ => None
      }
    }

    def unapply(str: String): Option[TypeName] = {
      str.toLowerCase() match {
        case "first_name" | "firstname" => extract(q"$domainPkg.FirstName")
        case "last_name" | "lastname" => extract(q"$domainPkg.LastName")
        case "name" | "fullname" | "full_name" => extract(q"$domainPkg.FullName")
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

      if (arg.symbol == Symbols.mapSymbol) {
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
      if (arg.symbol == Symbols.listSymbol) {
        arg.typeArgs match {
          case sourceTpe :: Nil => Some(
            CollectionType(
              source = sourceTpe,
              applier = applied => TypeName(s"$collectionPkg.List[..$applied]"),
              generator = tpe => q"$prefix.genList[$tpe]($prefix.defaultGeneration)"
            )
          )
          case _ => c.abort(c.enclosingPosition, "Could not extract inner type argument of List.")
        }
      } else if (arg.symbol == Symbols.setSymbol) {
        arg.typeArgs match {
          case sourceTpe :: Nil => Some(
            CollectionType(
              source = sourceTpe,
              applier = applied => TypeName(s"$collectionPkg.Set[..$applied]"),
              generator = tpe => q"$prefix.genSet[$tpe]($prefix.defaultGeneration)"
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

      if (arg.symbol == Symbols.optSymbol) {
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
      case OptionType(opt) => {
        accessor.name match {
          case KnownField(derived) => opt.generator(derived)
          case _ => opt.default
        }
      }
      case CollectionType(col) => accessor.name match {
        case KnownField(derived) => col.generator(derived)
        case _ => col.default
      }

      case _ => accessor.name match {
        case KnownField(derived) => q"$prefix.gen[$derived].value"
        case _ => q"$prefix.gen[${accessor.typeName}]"
      }
    }
  }

  def makeSample(
    typeName: c.TypeName,
    name: c.TermName,
    params: Seq[ValDef]
  ): List[Tree] = {

    val fresh = c.freshName(name)

    val applies = accessors(params).map { accessor => q"${accessor.name} = ${deriveSamplerType(accessor)}" }

    val tree = q"""implicit object $fresh extends $prefix.Sample[$typeName] {
      override def sample: $typeName = $name.apply(..$applies)
    }"""

    tree :: Nil
  }

  def macroImpl(annottees: c.Expr[Any]*): Tree = {
    annottees.map(_.tree) match {
      case tree@(classDef@q"$mods class $tpname[..$tparams] $ctorMods(...$params) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: Nil if mods.hasFlag(Flag.CASE) =>
        val name = tpname.toTermName

        q"""
          $classDef
          object $name {
            ..${makeSample(tpname.toTypeName, name, params.head)}
          }
        """

      case tree@(classDef@q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: q"object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf => ..$objDefs }"
        :: Nil if mods.hasFlag(Flag.CASE) =>

        q"""
         $classDef
         object $objName extends { ..$objEarlyDefs} with ..$objParents { $objSelf =>
           ..${makeSample(tpname.toTypeName, tpname.toTermName, paramss.head)}
           ..$objDefs
         }
         """

      case _ => c.abort(c.enclosingPosition, "Invalid annotation target, Sample must be a case classes")
    }
  }
}

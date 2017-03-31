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
package com.outworkers.util.macros

import scala.reflect.macros.blackbox

@macrocompat.bundle
trait AnnotationToolkit {

  val c: blackbox.Context

  import c.universe._

  val collectionPkg = q"_root_.scala.collection.immutable"

  def typed[A : c.WeakTypeTag]: Symbol = weakTypeOf[A].typeSymbol

  /**
    * Retrieves the accessor fields on a case class and returns an iterable of tuples of the form Name -> Type.
    * For every single field in a case class, a reference to the string name and string type of the field are returned.
    *
    * Example:
    *
    * {{{
    *   case class Test(id: UUID, name: String, age: Int)
    *
    *   accessors(Test) = Iterable("id" -> "UUID", "name" -> "String", age: "Int")
    * }}}
    *
    * @param tpe The input type of the case class definition.
    * @return An iterable of tuples where each tuple encodes the string name and string type of a field.
    */
  def caseFields(tpe: Type): Iterable[Accessor] = {
    object CaseField {
      def unapply(arg: TermSymbol): Option[(Name, Type)] = {
        if (arg.isVal && arg.isCaseAccessor) {
          Some(TermName(arg.name.toString.trim) -> arg.typeSignature.dealias)
        } else {
          None
        }
      }
    }

    tpe.decls.collect { case CaseField(name, fType) => {
      Accessor(name.toTermName, fType)
    }}
  }

  def printType(tpe: Type): String = {
    showCode(tq"${tpe.dealias}")
  }

  def tupleFields(tpe: Type): Iterable[Accessor] = {
    val sourceTerm = TermName("source")

    tpe.typeArgs.zipWithIndex.map {
      case (argTpe, i) =>
        val currentTerm = TermName(s"tp${i + 1}")
        val tupleRef = TermName("_" + (i + 1).toString)
        Accessor(tupleRef, argTpe)
    }
  }

  def fields(tpe: Type): Iterable[Accessor] = {
    if (isCaseClass(tpe)) {
      caseFields(tpe)
    } else if (isTuple(tpe)) {
      tupleFields(tpe)
    } else {
      c.abort(c.enclosingPosition, "")
    }
  }

  def isCaseClass(tpe: Type): Boolean = {
    tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isCaseClass
  }

  def isCaseClass(sym: Symbol): Boolean = {
    sym.isClass && sym.asClass.isCaseClass
  }

  def isTuple(tpe: Type): Boolean = {
    tpe.typeSymbol.fullName startsWith "scala.Tuple"
  }

  def isTuple(sym: Symbol): Boolean = {
    sym.fullName startsWith "scala.Tuple"
  }

  object Symbols {
    val listSymbol: Symbol = typed[scala.collection.immutable.List[_]]
    val setSymbol: Symbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol: Symbol = typed[scala.collection.immutable.Map[_, _]]
    val optSymbol: Symbol = typed[scala.Option[_]]
  }

  case class Accessor(
    name: TermName,
    paramType: Type
  ) {

    def typeName: TypeName = paramType.typeSymbol.name.toTypeName

    def typeArgs: List[Type] = paramType.typeArgs

    def tpe: Type = paramType

    def symbol: Symbol = paramType.typeSymbol
  }

  /**
    * Retrieves the accessor fields on a case class and returns an iterable of tuples of the form Name -> Type.
    * For every single field in a case class, a reference to the string name and string type of the field are returned.
    *
    * Example:
    *
    * {{{
    *   case class Test(id: UUID, name: String, age: Int)
    *
    *   accessors(Test) = Iterable("id" -> "UUID", "name" -> "String", age: "Int")
    * }}}
    *
    * @param params The list of params retrieved from the case class.
    * @return An iterable of tuples where each tuple encodes the string name and string type of a field.
    */
  def accessors(
    params: Seq[ValDef]
  ): Iterable[Accessor] = {
    params.map {
      case ValDef(_, name: TermName, tpt: Tree, _) => {
        Accessor(name, c.typecheck(tq"$tpt", c.TYPEmode).tpe)
      }
    }
  }
}

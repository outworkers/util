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

@macrocompat.bundle
class AnnotationToolkit(val c: scala.reflect.macros.blackbox.Context) {
  import c.universe._

  def typed[A : c.WeakTypeTag]: Symbol = weakTypeOf[A].typeSymbol

  object Symbols {
    val listSymbol = typed[scala.collection.immutable.List[_]]
    val setSymbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol = typed[scala.collection.immutable.Map[_, _]]
    val optSymbol = typed[scala.Option[_]]
  }

  case class Accessor(
    name: TermName,
    paramType: Type
  ) {

    def typeName: TypeName = paramType.typeSymbol.name.toTypeName

    def typeArgs: List[Type] = paramType.typeArgs

    def tpe: TypeName = symbol.name.toTypeName

    def symbol: Symbol = paramType.typeSymbol
  }

  case class ListAccessor(
    accessor: Accessor
  )

  object ListAccessor {
    def unapply(arg: Accessor): Option[ListAccessor] = {
      if (arg.paramType.typeSymbol == Symbols.listSymbol) {
        Some(ListAccessor(arg))
      } else {
        None
      }
    }
  }

  case class SetAccessor(
    accessor: Accessor
  )

  object SetAccessor {
    def unapply(arg: Accessor): Option[SetAccessor] = {
      if (arg.paramType.typeSymbol == Symbols.setSymbol) {
        Some(SetAccessor(arg))
      } else {
        None
      }
    }
  }

  case class OptionAccessor(
    accessor: Accessor
  )

  object OptionAccessor {
    def unapply(arg: Accessor): Option[OptionAccessor] = {
      if (arg.paramType.typeSymbol == Symbols.optSymbol) {
        Some(OptionAccessor(arg))
      } else {
        None
      }
    }
  }

  case class MapAccessor(
    accessor: Accessor
  )

  object MapAccessor {
    def unapply(arg: Accessor): Option[MapAccessor] = {
      if (arg.paramType.typeSymbol == Symbols.mapSymbol) {
        Some(MapAccessor(arg))
      } else {
        None
      }
    }
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

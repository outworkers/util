package com.outworkers.util.macros

class AnnotationToolkit(val c: scala.reflect.macros.blackbox.Context) {
  import c.universe._

  def typed[A : c.WeakTypeTag]: Symbol = weakTypeOf[A].typeSymbol

  object Symbols {
    val listSymbol = typed[scala.collection.immutable.List[_]]
    val setSymbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol = typed[scala.collection.immutable.Map[_, _]]
    val optSymbol = typed[scala.Option[_]]
  }

  case class ListAccessor(
    accessor: Accessor
  )

  object ListAccessor {
    def unapply(arg: Accessor): Option[ListAccessor] = {
      if (arg.origin.typeSymbol == Symbols.listSymbol) {
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
      if (arg.origin.typeSymbol == Symbols.setSymbol) {
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
      if (arg.origin.typeSymbol == Symbols.optSymbol) {
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
      if (arg.origin.typeSymbol == Symbols.mapSymbol) {
        Some(MapAccessor(arg))
      } else {
        None
      }
    }
  }

  case class Accessor(
    name: TermName,
    origin: Type
  ) {
    def symbol: Symbol = origin.typeSymbol

    def tpe: TypeName = newTypeName(origin.typeSymbol.asType.name.decodedName.toString)

    def typeName: TypeName = tpe
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
    *   accessors(Test) = Iterable(Accessor(id, UUID, java.util.UUID), etc)
    * }}}
    *
    * @param params The list of params retrieved from the case class.
    * @return An iterable of tuples where each tuple encodes the string name and string type of a field.
    */
  def accessors(
    params: Seq[ValDef]
  ): Iterable[Accessor] = {
    params.map(valDef => Accessor(valDef.name, c.typecheck(tq"${valDef.tpt}", c.TYPEmode).tpe))
  }
}

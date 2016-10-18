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

  case class Accessor(
    name: TermName,
    tpe: TypeName,
    paramType: Type
  ) {
    def symbol: Symbol = paramType.typeSymbol

    def typeName: TypeName = symbol.asType.name
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
    params.map(valDef => {
      val ex = c.typecheck(tq"${valDef.tpt}", c.TYPEmode)
      Accessor(
        valDef.name,
        TypeName(valDef.tpt.toString),
        ex.tpe
      )
    }
    )
  }
}

package com.outworkers.util.testing

@macrocompat.bundle
class SamplerMacro(val c: scala.reflect.macros.blackbox.Context) {

  import c.universe._

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
    params: Seq[c.universe.ValDef]
  ): Iterable[(c.universe.TermName, c.universe.TypeName)] = {
    params.map {
      case ValDef(_, name: TermName, tpt: Tree, _) => name -> TypeName(tpt.toString)
    }
  }

  val prefix = q"com.outworkers.util.testing"


  def makeSample(
    typeName: c.TypeName,
    name: c.TermName,
    params: Seq[ValDef]
  ): Tree = {

    val fresh = c.freshName(typeName)
    val applies = accessors(params).map {
      case (nm, tp) => q"""$prefix.Sample[$tp]"""
    }

    q"""implicit object $fresh extends $prefix.Sample[$typeName] {
      override def sample: $typeName = $typeName(..$applies)
    }"""
  }

  def macroImpl(annottees: c.Expr[Any]*): Tree =
    annottees.map(_.tree) match {
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$params) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: Nil if mods.hasFlag(Flag.CASE) =>
        val name = tpname.toTermName

        val res = q"""
       $classDef
       object $name {
         ..${makeSample(tpname.toTypeName, name, params.head)}
       }
       """
        println(showCode(res))
        c.Expr[Any](res)

      case _ => c.abort(c.enclosingPosition, "Invalid annotation target, UDTs must be a case classes")
  }
}

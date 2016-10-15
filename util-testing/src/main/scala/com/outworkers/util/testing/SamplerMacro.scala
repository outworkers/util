package com.outworkers.util.testing

@macrocompat.bundle
class SamplerMacro(val c: scala.reflect.macros.blackbox.Context) {

  import c.universe._

  val prefix = q"com.outworkers.util.testing"

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
  ): Iterable[(TermName, TypeName)] = {
    params.map {
      case ValDef(_, name: TermName, tpt: Tree, _) => name -> TypeName(tpt.toString)
    }
  }

  def fieldNameInference(nm: TermName): Option[TypeName] = {
    case "first_name" | "firstname" => Some(tq"$prefix.FirstName")
    case "last_name" | "lastname" => Some(tq"$prefix.LastName")
    case "name" | "fullname" | "full_name" => Some(tq"""$prefix.FullName""")
    case "email" | "email_address" | "emailaddress" => Some(tq"""$prefix.EmailAddress""")
    case "country" => Some(tq"""$prefix.CountryCode""")
    case _ => None
  }

  object KnownField {
    def unapply(arg: TermName): Option[TypeName] = fieldNameInference(arg)
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
  case class CollectionType(
    sources: List[TypeName],
    applier: List[TypeName] => TypeName
  )

  object CollectionType {
    def apply(
      source: TypeName,
      applier: List[TypeName] => TypeName
    ): Option[CollectionType] = Some(new CollectionType(source :: Nil, applier))

    def unapply(arg: TypeName): Option[CollectionType] = collectionType(arg)
  }


  private[this] def collectionType(
    source: TypeName
  ): Option[CollectionType] = {
    val strTpe = source.decodedName.toString

    if (strTpe.startsWith("List[")) {
      CollectionType.apply(
        source = TypeName(strTpe.drop(5).dropRight(1)),
        applier = applied => TypeName(s"scala.collection.immutable.List[..$applied]")
      )
    } else if (strTpe.startsWith("Option[")) {
      CollectionType(
        source = TypeName(strTpe.drop(4).dropRight(1)),
        applier = applied => TypeName(s"scala.Option[$applied]")
      )
    } else if (strTpe.startsWith("Set[")) {
      CollectionType(
        source = TypeName(strTpe.drop(4).dropRight(1)),
        applied => TypeName(s"scala.collection.immutable.Set[..$applied]")
      )
    } else if (strTpe.startsWith("Map[")) {
      val parts = strTpe.replaceAll(" ", "").drop(4).split(",")

      Some(
        CollectionType(
          List(TypeName(parts(0)), TypeName(parts(1))),
          applied => TypeName(s"scala.collection.immutable.Map[..$applied]")
        )
      )
    } else {
      None
    }
  }

  object CollectionSample {
    def unapply(arg: TypeName): Option[CollectionType] = collectionType(arg)
  }

  private[this] def deriveSamplerType(
    field: TermName,
    tpe: TypeName
  ): TypeName = {
    val strTpe = tpe.decodedName.toString

    tpe match {
      case col @ CollectionType(tp) => {

      }
    }



    if (strTpe.startsWith("List[")) {
      tq"$prefix.genList[${TypeName(strTpe.drop(5).dropRight(1))}]()"
    } else if(strTpe.startsWith("Option[")) {
      tq"$prefix.genOpt[${TypeName(strTpe.drop(4).dropRight(1))}]()"
    } else if (strTpe.startsWith("Set[")) {
      tq"$prefix.getSet[${TypeName(strTpe.drop(4).dropRight(1))}]()"
    } else if (strTpe.startsWith("Map[")) {
      val parts = strTpe.replaceAll(" ", "").drop(4).split(",")
      tq"$prefix.genMap[${TypeName(parts(0))}, ${TypeName(parts(1))}]()"
    } else {
      tq"$tpe"
    }
  }

  def inferType(nm: TermName, tp: TypeName): Tree = {
    val str = nm.decodedName.toString
    str.toLowerCase() match {
      case "first_name" | "firstname" => q"""$prefix.gen[$prefix.FirstName].value"""
      case "last_name" | "lastname" => q"""$prefix.gen[$prefix.LastName].value"""
      case "name" | "fullname" | "full_name" => q"""$prefix.gen[$prefix.FullName].value"""
      case "email" | "email_address" | "emailaddress" => q"""$prefix.gen[$prefix.EmailAddress].value"""
      case "country" => q"""$prefix.gen[$prefix.CountryCode].value"""
      case _ => q"""$prefix.Sample[$tp].sample"""
    }

  }

  def makeSample(
    typeName: c.TypeName,
    name: c.TermName,
    params: Seq[ValDef]
  ): Tree = {

    val fresh = c.freshName(name)
    val applies = accessors(params).map {
      case (nm, tp) => inferType(nm, tp)
    }

    q"""implicit object $fresh extends $prefix.Sample[$typeName] {
      override def sample: $typeName = $name(..$applies)
    }"""
  }

  def macroImpl(annottees: c.Expr[Any]*): Tree =
    annottees.map(_.tree) match {
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$params) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
        :: Nil if mods.hasFlag(Flag.CASE) =>
        val name = tpname.toTermName

        q"""
          $classDef
          object $name {
            ..${makeSample(tpname.toTypeName, name, params.head)}
          }
        """

      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }")
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

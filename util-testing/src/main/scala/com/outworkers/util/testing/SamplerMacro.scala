package com.outworkers.util.testing

@macrocompat.bundle
class SamplerMacro(val c: scala.reflect.macros.blackbox.Context) {

  import c.universe._

  val prefix = q"com.outworkers.util.testing"


  // val example: String => gen[String]
  // val firstName: String => gen[FirstName].value
  // val lastName: Option[String] => genOpt[LastName].map(_.value)
  // val emails: List[String] => genList[EmailAddress].map(_.value)
  // val sample: List[String] => genList[String]

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

  object KnownField {
    def unapply(nm: TermName): Option[TypeName] = {
      val str = nm.decodedName.toString
      str.toLowerCase() match {
        case "first_name" | "firstname" => Some(TypeName(s"$prefix.FirstName"))
        case "last_name" | "lastname" => Some(TypeName(s"$prefix.LastName"))
        case "name" | "fullname" | "full_name" => Some(TypeName(s"$prefix.FullName"))
        case "email" | "email_address" | "emailaddress" => Some(TypeName(s"$prefix.EmailAddress"))
        case "country" => Some(TypeName(s"$prefix.CountryCode"))
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
    sources: List[TypeName],
    applier: List[TypeName] => TypeName,
    generator: List[TypeName] => Tree
  ) {
    def infer: TypeName = applier(sources)

    def default: Tree = generator(sources)
  }

  object MapType {
    def apply(
      source: TypeName,
      applier: List[TypeName] => TypeName,
      generator: List[TypeName] => Tree
    ): Option[MapType] = Some(new MapType(source :: Nil, applier, generator))

    def unapply(arg: TypeName): Option[MapType] = {
      val strTpe = arg.decodedName.toString

      if (strTpe.startsWith("Map[")) {
        val parts = strTpe.replaceAll(" ", "").drop(4).split(",")
        val keyTpe = TypeName(parts(0))
        val valueTpe = TypeName(parts(1).dropRight(1))

        Some(
          MapType(
            List(keyTpe, valueTpe),
            applied => TypeName(s"scala.collection.immutable.Map[..$applied]"),
            generator = types => q"$prefix.genMap[..$types]($prefix.defaultGeneration)"
          )
        )
      } else {
        None
      }
    }
  }

  case class CollectionType(
    source: TypeName,
    applier: TypeName => TypeName,
    generator: TypeName => Tree
  ) {
    def infer: TypeName = applier(source)

    def default: Tree = generator(source)
  }

  object CollectionType {
    def unapply(arg: TypeName): Option[CollectionType] = {
      val strTpe = arg.decodedName.toString

      if (strTpe.startsWith("List[")) {
        val sourceTpe = TypeName(strTpe.drop(5).dropRight(1))
        Some(
          CollectionType(
            source = sourceTpe,
            applier = applied => TypeName(s"scala.collection.immutable.List[..$applied]"),
            generator = tpe => q"$prefix.genList[$tpe]($prefix.defaultGeneration)"
          )
        )
      } else if (strTpe.startsWith("Set[")) {
        val sourceTpe = TypeName(strTpe.drop(4).dropRight(1))
        Some(
          CollectionType(
            source = sourceTpe,
            applied => TypeName(s"scala.collection.immutable.Set[..$applied]"),
            generator = tpe => q"$prefix.genSet[$tpe]($prefix.defaultGeneration)"
          )
        )
      } else {
        None
      }
    }
  }

  case class OptionType(
    source: TypeName,
    applier: TypeName => TypeName,
    generator: TypeName => Tree
  ) {
    def infer: TypeName = applier(source)

    def default: Tree = generator(source)
  }

  object OptionType {
    def unapply(arg: TypeName): Option[(OptionType)] = {
      val strTpe = arg.decodedName.toString

      if (strTpe.startsWith("Option[")) {
        val sourceTpe = TypeName(strTpe.drop(4).dropRight(1))
        Some(
          OptionType(
            source = sourceTpe,
            applier = applied => TypeName(s"scala.Option[$applied]"),
            generator = tpe => q"$prefix.genOpt[$sourceTpe]"
          )
        )
      } else {
        None
      }
    }
  }

  private[this] def deriveSamplerType(
    field: TermName,
    tpe: TypeName
  ): Tree = {
    tpe match {
      case MapType(col) => col.default
      case OptionType(opt) => field match {
        case KnownField(derived) => opt.generator(derived)
        case _ => opt.default
      }
      case CollectionType(col) => field match {
        case KnownField(derived) => col.generator(derived)
        case _ => col.default
      }
      case KnownField(derived) => q"$prefix.gen[$derived].value"
      case _ => q"$prefix.gen[$tpe]"
    }
  }

  def makeSample(
    typeName: c.TypeName,
    name: c.TermName,
    params: Seq[ValDef]
  ): Tree = {

    val fresh = c.freshName(name)
    val applies = accessors(params).map {
      case (nm, tp) => q"$nm = ${deriveSamplerType(nm, tp)}"
    }

    val tree = q"""implicit object $fresh extends $prefix.Sample[$typeName] {
      override def sample: $typeName = $name(..$applies)
    }"""

    println(showCode(tree))

    tree
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

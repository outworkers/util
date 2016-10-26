package com.outworkers.util.testing

import scala.annotation.implicitNotFound

@implicitNotFound("Could not emit trace for type")
trait Tracer[T] {
  def trace(instance: T): String
}

object Tracer {
  implicit def macroMaterialize[T]: Tracer[T] = macro TracerMacro.macroImpl[T]

  def apply[T : Tracer]: Tracer[T] = implicitly[Tracer[T]]
}

@macrocompat.bundle
class TracerMacro(val c: scala.reflect.macros.blackbox.Context) {
  import c.universe._

  def typed[A : c.WeakTypeTag]: Symbol = weakTypeOf[A].typeSymbol

  object CaseField {
    def unapply(sym: TermSymbol): Option[(Name, Type)] = {
      if (sym.isVal && sym.isCaseAccessor) {
        Some(sym.name -> sym.typeSignature)
      } else {
        None
      }
    }
  }

  object Symbols {
    val listSymbol = typed[scala.collection.immutable.List[_]]
    val setSymbol = typed[scala.collection.immutable.Set[_]]
    val mapSymbol = typed[scala.collection.immutable.Map[_, _]]
  }

  val packagePrefix = q"com.outworkers.util.testing"

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
  def fields(tpe: Type): Iterable[(Name, Type)] = {
    tpe.decls.collect { case CaseField(nm, tp) => nm -> tp }
  }

  def macroImpl[T : WeakTypeTag]: Tree = {
    val sym = weakTypeOf[T].typeSymbol

    if (sym.isClass && sym.asClass.isCaseClass) {
      caseClassImpl[T]
    } else {
      q"""new $packagePrefix.Tracers.StringTracer[$sym]"""
    }
  }

  def caseClassImpl[T : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]
    val flds = fields(tpe)
    val cmp = tpe.typeSymbol.name

    val appliers = flds.map {
      case (nm, tp) => q""" "  " + ${nm.toString} + "= " + $packagePrefix.Tracer[$tp].trace(${c.parse(s"instance.$nm")})"""
    }

    q"""
      new $packagePrefix.Tracer[$tpe] {
        def trace(instance: $tpe): String = {
          ${cmp.toString} + "(\n" + scala.collection.immutable.List.apply(..$appliers).mkString("\n") + "\n)"
        }
      }
    """
  }
}
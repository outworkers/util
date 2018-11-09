package com.outworkers.util.tags

import scala.reflect.macros.blackbox

@macrocompat.bundle
class Tags(val c: blackbox.Context) {

  import c.universe._

  private[this] val pkg = q"com.outworkers.util.tags"

  def tagMacro[A: c.WeakTypeTag, T: c.WeakTypeTag]: c.Expr[Tags.Aux[A, T]] = {
    import c.universe._
    val aTpe = weakTypeOf[Tags.Aux[A, T]]
    val a = c.prefix.tree match {
      case Apply(_, List(x)) => x
      case t => c.abort(c.enclosingPosition, s"Cannot extract .tag target (tree = $t)")
    }
    c.Expr[Tags.Aux[A, T]](q"$a.asInstanceOf[$aTpe]")
  }

  def untagMacro[A: c.WeakTypeTag, T: c.WeakTypeTag]: c.Expr[A] = {
    import c.universe._
    val aTpe = weakTypeOf[A]
    val at = c.prefix.tree match {
      case Apply(_, List(x)) => x
      case t => c.abort(c.enclosingPosition, s"Cannot extract .untag target (tree = $t)")
    }
    c.Expr[A](q"$at.asInstanceOf[$aTpe]")
  }

  def wrapMacro[A: c.WeakTypeTag, T: c.WeakTypeTag](a: c.Expr[A]): c.Expr[Tags.Aux[A, T]] = {
    val aTpe = weakTypeOf[A]
    val tTpe = weakTypeOf[T]
    c.Expr[Tags.Aux[A, T]](q"$a.asInstanceOf[$pkg.Tags.Aux[$aTpe, $tTpe]]")
  }

  def unwrapMacro[A: c.WeakTypeTag, T](at: c.Expr[Tags.Aux[A, T]]): c.Expr[A] = {
    val aTpe = weakTypeOf[A]
    c.Expr[A](q"$at.asInstanceOf[$aTpe]")
  }

  def wrapfMacro[F[_], A: c.WeakTypeTag, T: c.WeakTypeTag](fa: c.Expr[F[A]])(
    implicit fTag: c.WeakTypeTag[F[_]]
  ): c.Expr[F[Tags.Aux[A, T]]] = {
    val aTpe = appliedType(typeOf[Tags.Aux[_, _]], List(weakTypeOf[A], weakTypeOf[T]))
    val faTpe = appliedType(fTag.tpe.typeConstructor, aTpe :: Nil)
    val t = q"$fa.asInstanceOf[$faTpe]"
    c.Expr[F[Tags.Aux[A, T]]](t)
  }

  def unwrapfMacro[F[_], A: c.WeakTypeTag, T](fat: c.Expr[F[Tags.Aux[A, T]]])(
    implicit fTag: c.WeakTypeTag[F[_]]
  ): c.Expr[F[A]] = {
    val faTpe = appliedType(fTag.tpe.typeConstructor, weakTypeOf[A] :: Nil)
    c.Expr[F[A]](q"$fat.asInstanceOf[$faTpe]")
  }
}

object Tags {
  type Aux[A, T] = { type Data = A; type Tag = T }

  def wrap[A, T](a: A): A @@ T = macro Tags.wrapMacro[A, T]
  def unwrap[A, T](at: A @@ T): A = macro Tags.unwrapMacro[A, T]
}
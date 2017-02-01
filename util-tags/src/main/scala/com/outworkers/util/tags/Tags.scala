package com.outworkers.util.tags

import scala.reflect.macros.blackbox

@macrocompat.bundle
class Tags(val c: blackbox.Context) {

  import c.universe._

  private[this] val pkg = q"com.outworkers.util.tags"

  def tagMacro[A: c.WeakTypeTag, T: c.WeakTypeTag]: c.Expr[Tags.Aux[A, T]] = {
    import c.universe._
    val AT = weakTypeOf[Tags.Aux[A, T]]
    val a = c.prefix.tree match {
      case Apply(_, List(x)) => x
      case t => c.abort(c.enclosingPosition, s"Cannot extract .tag target (tree = $t)")
    }
    c.Expr[Tags.Aux[A, T]](q"$a.asInstanceOf[$AT]")
  }

  def untagMacro[A: c.WeakTypeTag, T: c.WeakTypeTag]: c.Expr[A] = {
    import c.universe._
    val A = weakTypeOf[A]
    val at = c.prefix.tree match {
      case Apply(_, List(x)) => x
      case t => c.abort(c.enclosingPosition, s"Cannot extract .untag target (tree = $t)")
    }
    c.Expr[A](q"$at.asInstanceOf[$A]")
  }

  def wrapMacro[A: c.WeakTypeTag, T: c.WeakTypeTag](a: c.Expr[A]): c.Expr[Tags.Aux[A, T]] = {
    val A = weakTypeOf[A]
    val T = weakTypeOf[T]
    val AT = weakTypeOf[Tags.Aux[A, T]]
    c.Expr[Tags.Aux[A, T]](q"$a.asInstanceOf[$pkg.Tags.Aux[$A, $T]]")
  }

  def unwrapMacro[A: c.WeakTypeTag, T](at: c.Expr[Tags.Aux[A, T]]): c.Expr[A] = {
    val A = weakTypeOf[A]
    c.Expr[A](q"$at.asInstanceOf[$A]")
  }

  def wrapfMacro[F[_], A: c.WeakTypeTag, T: c.WeakTypeTag](fa: c.Expr[F[A]])(
    implicit F: c.WeakTypeTag[F[_]]
  ): c.Expr[F[Tags.Aux[A, T]]] = {
    val AT = appliedType(typeOf[Tags.Aux[_, _]], List(weakTypeOf[A], weakTypeOf[T]))
    val FAT = appliedType(F.tpe.typeConstructor, AT :: Nil)
    val t = q"$fa.asInstanceOf[$FAT]"
    c.Expr[F[Tags.Aux[A, T]]](t)
  }

  def unwrapfMacro[F[_], A: c.WeakTypeTag, T](fat: c.Expr[F[Tags.Aux[A, T]]])(
    implicit F: c.WeakTypeTag[F[_]]
  ): c.Expr[F[A]] = {
    val FA = appliedType(F.tpe.typeConstructor, weakTypeOf[A] :: Nil)
    c.Expr[F[A]](q"$fat.asInstanceOf[$FA]")
  }
}

object Tags {
  type Aux[A, T] = { type Data = A; type Tag = T }

  def wrap[A, T](a: A): A @@ T = macro Tags.wrapMacro[A, T]
  def unwrap[A, T](at: A @@ T): A = macro Tags.unwrapMacro[A, T]
}
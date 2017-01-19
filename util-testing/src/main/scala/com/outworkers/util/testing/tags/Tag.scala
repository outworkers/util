package com.outworkers.util.testing.tags

object Tag {

  type @@[A, T] = Tags#Tagged[A, T]

  def wrap[A, T](a: A): A @@ T = macro Tags.wrapMacro[A, T]
  def unwrap[A, T](at: A @@ T): A = macro Tags.unwrapMacro[A, T]

  def wrapf[F[_], A, T](fa: F[A]): F[A @@ T] = macro Tags.wrapfMacro[F, A, T]
  def unwrapf[F[_], A, T](fat: F[A @@ T]): F[A] = macro Tags.unwrapfMacro[F, A, T]
}
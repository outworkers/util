package com.outworkers.util.testing.tags

object Tag {

  type @@[A, T] = Tags#Tagged[A, T]

  implicit class TagOps[A](a: A) {
    def tag[T]: A @@ T = macro Tags.tagMacro[A, T]
  }

  implicit class UntagOps[A, T](at: A @@ T) {
    def untag: A = macro Tags.untagMacro[A, T]
  }

  implicit class TagfOps[F[_], A](val fa: F[A]) extends AnyVal {
    def tagf[T]: F[A @@ T] = wrapf[F, A, T](fa)
  }

  implicit class UntagfOps[F[_], A, T](val fat: F[A @@ T]) {
    def untagf: F[A] = unwrapf[F, A, T](fat)
  }

  // implicit class TagkOps[F[_[_]], G[_]](val fg: F[G]) extends AnyVal {
  //   def tagk[T]: F[λ[α => G[α] @@ T]] = wrapk[F, G, T](fg)
  // }
  //
  // implicit class UntagkOps[F[_[_]], G[_], T](val fgt: F[λ[α => G[α] @@ T]]) extends AnyVal {
  //   def untagk: F[G] = unwrapk[F, G, T](fgt)
  // }

  def wrap[A, T](a: A): A @@ T = macro Tags.wrapMacro[A, T]
  def unwrap[A, T](at: A @@ T): A = macro Tags.unwrapMacro[A, T]

  def wrapf[F[_], A, T](fa: F[A]): F[A @@ T] = macro Tags.wrapfMacro[F, A, T]
  def unwrapf[F[_], A, T](fat: F[A @@ T]): F[A] = macro Tags.unwrapfMacro[F, A, T]

  // def wrapk[F[_[_]], G[_], T](fg: F[G]): F[λ[α => G[α] @@ T]] = macro Tags.wrapkMacro[F, G, T]
  // def unwrapk[F[_[_]], G[_], T](fgt: F[λ[α => G[α] @@ T]]): F[G] = macro Tags.unwrapkMacro[F, G, T]

}
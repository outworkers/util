package com.outworkers.util.testing

package object tags {

  implicit class TagOps[A](a: A) {
    def tag[T]: A @@ T = macro Tags.tagMacro[A, T]
  }

  implicit class UntagOps[A, T](at: A @@ T) {
    def untag: A = macro Tags.untagMacro[A, T]
  }

  implicit class TagfOps[F[_], A](val fa: F[A]) extends AnyVal {
    def tagf[T]: F[A @@ T] =  macro Tags.wrapfMacro[F, A, T](fa)
  }

  implicit class UntagfOps[F[_], A, T](val fat: F[A @@ T]) {
    def untagf: F[A] = macro Tags.unwrapfMacro[F, A, T](fat)
  }

}

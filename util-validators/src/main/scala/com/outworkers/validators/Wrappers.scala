package com.outworkers.validators

import cats.Apply
import cats.data.ValidatedNel

case class ParseError(label: String, errors: List[String])

case class ValidationError(
  errors: List[ParseError]
)

trait Wrappers {
  type Nel[T] = ValidatedNel[(String, String), T]

  trait Wrapper[TP <: Product] {
    type In = TP

    def map[R](fn: (TP) => R): Nel[R]
  }

  case class Wrapper2[T1, T2](v1: Nel[T1], v2: Nel[T2]) extends Wrapper[(T1, T2)] {
    def and[T3](v3: Nel[T3]): Wrapper3[T1, T2, T3] = new Wrapper3[T1, T2, T3](v1, v2, v3)

    override def map[R](fn: ((T1, T2)) => R): ValidatedNel[(String, String), R] = {
      Apply[ValidatedNel[(String, String), ?]].map2[T1, T2, R](v1, v2) {
        case tp => fn(tp)
      }
    }
  }

  case class Wrapper3[T1, T2, T3](v1: Nel[T1], v2: Nel[T2], v3: Nel[T3]) extends Wrapper[(T1, T2, T3)] {
    def and[T4](v4: Nel[T4]): Wrapper4[T1, T2, T3, T4] = new Wrapper4(v1, v2, v3, v4)

    override def map[R](fn: ((T1, T2, T3)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map3[T1, T2, T3, R](v1, v2, v3) {
        case tp => fn(tp)
      }
    }
  }

  case class Wrapper4[T1, T2, T3, T4](v1: Nel[T1], v2: Nel[T2], v3: Nel[T3], v4: Nel[T4])
    extends Wrapper[(T1, T2, T3, T4)] {
    def and[T5](v5: Nel[T5]): Wrapper5[T1, T2, T3, T4, T5] = new Wrapper5(v1, v2, v3, v4, v5)

    override def map[R](fn: ((T1, T2, T3, T4)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map4[T1, T2, T3, T4, R](v1, v2, v3, v4) {
        case tp => fn(tp)
      }
    }
  }

  case class Wrapper5[T1, T2, T3, T4, T5](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5]
  ) extends Wrapper[(T1, T2, T3, T4, T5)] {

    def and[T6](v6: Nel[T6]): Wrapper6[T1, T2, T3, T4, T5, T6] = new Wrapper6(v1, v2, v3, v4, v5, v6)

    override def map[R](fn: ((T1, T2, T3, T4, T5)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map5[T1, T2, T3, T4, T5, R](v1, v2, v3, v4, v5) {
        case tp => fn(tp)
      }
    }
  }

  case class Wrapper6[T1, T2, T3, T4, T5, T6](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6)] {

    def and[T7](v7: Nel[T7]): Wrapper7[T1, T2, T3, T4, T5, T6, T7] = new Wrapper7(v1, v2, v3, v4, v5, v6, v7)

    override def map[R](fn: ((T1, T2, T3, T4, T5, T6)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map6[T1, T2, T3, T4, T5, T6, R](v1, v2, v3, v4, v5, v6) {
        case tp => fn(tp)
      }
    }
  }

  case class Wrapper7[T1, T2, T3, T4, T5, T6, T7](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map7[T1, T2, T3, T4, T5, T6, T7, R](v1, v2, v3, v4, v5, v6, v7) {
        case tp => fn(tp)
      }
    }
  }
}

package com.outworkers.util.validators

import cats.Apply
import cats.data.ValidatedNel
import shapeless.Generic

case class ParseError(property: String, messages: List[String])

case class ValidationError(
  errors: List[ParseError]
) {
  def add(other: ValidationError): ValidationError = ValidationError(errors ++ other.errors)
  def ++(other: ValidationError): ValidationError = add(other)
}

trait Wrappers {
  type Nel[T] = ValidatedNel[(String, String), T]

  trait Wrapper[TP <: Product] {
    type In = TP

    def as[R](obj: TP)(implicit gen: Generic.Aux[TP, R]): Nel[R] = map[R](source => gen to source)

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
    def and[T4](v4: Nel[T4]): Wrapper4[T1, T2, T3, T4] = Wrapper4(v1, v2, v3, v4)

    override def map[R](fn: ((T1, T2, T3)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map3[T1, T2, T3, R](v1, v2, v3) {
        case tp => fn(tp)
      }
    }
  }

  case class Wrapper4[T1, T2, T3, T4](v1: Nel[T1], v2: Nel[T2], v3: Nel[T3], v4: Nel[T4])
    extends Wrapper[(T1, T2, T3, T4)] {
    def and[T5](v5: Nel[T5]): Wrapper5[T1, T2, T3, T4, T5] = Wrapper5(v1, v2, v3, v4, v5)

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

    def and[T6](v6: Nel[T6]): Wrapper6[T1, T2, T3, T4, T5, T6] = Wrapper6(v1, v2, v3, v4, v5, v6)

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

    def and[T7](v7: Nel[T7]): Wrapper7[T1, T2, T3, T4, T5, T6, T7] = Wrapper7(v1, v2, v3, v4, v5, v6, v7)

    override def map[R](fn: ((T1, T2, T3, T4, T5, T6)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map6[T1, T2, T3, T4, T5, T6, R](v1, v2, v3, v4, v5, v6) {
        case x => fn(x)
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
        case x => fn(x)
      }
    }

    def and[T8](v8: Nel[T8]): Wrapper8[T1, T2, T3, T4, T5, T6, T7, T8] = Wrapper8(v1, v2, v3, v4, v5, v6, v7, v8)
  }

  case class Wrapper8[T1, T2, T3, T4, T5, T6, T7, T8](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map8[T1, T2, T3, T4, T5, T6, T7, T8, R](v1, v2, v3, v4, v5, v6, v7, v8) {
        case x => fn(x)
      }
    }

    def and[T9](v9: Nel[T9]): Wrapper9[T1, T2, T3, T4, T5, T6, T7, T8, T9] = Wrapper9(v1, v2, v3, v4, v5, v6, v7, v8, v9)
  }

  case class Wrapper9[T1, T2, T3, T4, T5, T6, T7, T8, T9](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9
      ) { case x => fn(x) }
    }

    def and[T10](v10: Nel[T10]): Wrapper10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10] = {
      Wrapper10(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)
    }
  }

  case class Wrapper10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9],
    v10: Nel[T10]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9, v10
      ) { case x => fn(x) }
    }

    def and[T11](v11: Nel[T11]): Wrapper11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11] = {
      Wrapper11(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)
    }
  }

  case class Wrapper11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9],
    v10: Nel[T10],
    v11: Nel[T11]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11
      ) { case x => fn(x) }
    }

    def and[T12](v12: Nel[T12]): Wrapper12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12] = {
      Wrapper12(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)
    }
  }

  case class Wrapper12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9],
    v10: Nel[T10],
    v11: Nel[T11],
    v12: Nel[T12]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12,R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12
      ) { case x => fn(x) }
    }

    def and[T13](v13: Nel[T13]): Wrapper13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13] = {
      Wrapper13(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13)
    }
  }

  case class Wrapper13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9],
    v10: Nel[T10],
    v11: Nel[T11],
    v12: Nel[T12],
    v13: Nel[T13]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13
      ) { case x => fn(x) }
    }

    def and[T14](v14: Nel[T14]): Wrapper14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14] = {
      Wrapper14(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)
    }
  }

  case class Wrapper14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9],
    v10: Nel[T10],
    v11: Nel[T11],
    v12: Nel[T12],
    v13: Nel[T13],
    v14: Nel[T14]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14
      ) { case x => fn(x) }
    }

    def and[T15](v15: Nel[T15]): Wrapper15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15] = {
      Wrapper15(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)
    }
  }

  case class Wrapper15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9],
    v10: Nel[T10],
    v11: Nel[T11],
    v12: Nel[T12],
    v13: Nel[T13],
    v14: Nel[T14],
    v15: Nel[T15]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15
      ) { case x => fn(x) }
    }

    def and[T16](v16: Nel[T16]): Wrapper16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16] = {
      Wrapper16(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16)
    }
  }

  case class Wrapper16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9],
    v10: Nel[T10],
    v11: Nel[T11],
    v12: Nel[T12],
    v13: Nel[T13],
    v14: Nel[T14],
    v15: Nel[T15],
    v16: Nel[T16]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16
      ) { case x => fn(x) }
    }

    def and[T17](v17: Nel[T17]): Wrapper17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17] = {
      Wrapper17(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17)
    }
  }

  case class Wrapper17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](
    v1: Nel[T1],
    v2: Nel[T2],
    v3: Nel[T3],
    v4: Nel[T4],
    v5: Nel[T5],
    v6: Nel[T6],
    v7: Nel[T7],
    v8: Nel[T8],
    v9: Nel[T9],
    v10: Nel[T10],
    v11: Nel[T11],
    v12: Nel[T12],
    v13: Nel[T13],
    v14: Nel[T14],
    v15: Nel[T15],
    v16: Nel[T16],
    v17: Nel[T17]
  ) extends Wrapper[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)] {
    override def map[R](fn: ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)) => R): Nel[R] = {
      Apply[ValidatedNel[(String, String), ?]].map17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](
        v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17
      ) { case x => fn(x) }
    }
  }

}

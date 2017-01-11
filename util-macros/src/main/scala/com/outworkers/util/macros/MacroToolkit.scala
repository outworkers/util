package com.outworkers.util.macros

import scala.reflect.macros.blackbox

@macrocompat.bundle
class MacroToolkit(override val c: blackbox.Context) extends AnnotationToolkit(c) {

  import c.universe._

  object TupleSymbols {
    val tuple1 = typed[Tuple1[_]]
    val tuple2 = typed[Tuple2[_, _]]
    val tuple3 = typed[Tuple3[_, _, _]]
    val tuple4 = typed[Tuple4[_, _, _, _]]
    val tuple5 = typed[Tuple5[_, _, _, _, _]]
    val tuple6 = typed[Tuple6[_, _, _, _, _, _]]
    val tuple7 = typed[Tuple7[_, _, _, _, _, _, _]]
    val tuple8 = typed[Tuple8[_, _, _, _, _, _, _, _]]
    val tuple9 = typed[Tuple9[_, _, _, _, _, _, _, _, _]]
    val tuple10 = typed[Tuple10[_, _, _, _, _, _, _, _, _, _]]
    val tuple11 = typed[Tuple11[_, _, _, _, _, _, _, _, _, _, _]]
    val tuple12 = typed[Tuple12[_, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple13 = typed[Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple14 = typed[Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple15 = typed[Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple16 = typed[Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple17 = typed[Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple18 = typed[Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple19 = typed[Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple20 = typed[Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple21 = typed[Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
    val tuple22 = typed[Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]]
  }

}

package com.outworkers.util.macros

import scala.reflect.macros.blackbox

@macrocompat.bundle
class MacroToolkit(val c: blackbox.Context) {

  import c.universe._


}

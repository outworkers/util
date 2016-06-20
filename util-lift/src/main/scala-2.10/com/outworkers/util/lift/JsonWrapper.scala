package com.outworkers.util.lift

import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{pretty, render}

object JsonWrapper {
  def prettyRender(json: JValue): String = {
    pretty(render(json))
  }
}

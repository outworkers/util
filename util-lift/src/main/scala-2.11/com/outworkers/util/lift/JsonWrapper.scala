package com.outworkers.util.lift

import net.liftweb.json.{prettyRender => nativePretty}
import net.liftweb.json.JsonAST.JValue

object JsonWrapper {
  def prettyRender(json: JValue): String = nativePretty(json)
}

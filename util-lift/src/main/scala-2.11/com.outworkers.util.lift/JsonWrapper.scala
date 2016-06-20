package com.outworkers.util.lift

import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.{prettyRender => nativePretty}

object JsonWrapper {
  def prettyRender(json: JValue): String = nativePretty(json)
}
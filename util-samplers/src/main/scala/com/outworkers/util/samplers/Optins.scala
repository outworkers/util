package com.outworkers.util.samplers

trait FillOptions

object Options {
  implicit val alwaysFillOptions: FillOptions = new FillOptions {}
}

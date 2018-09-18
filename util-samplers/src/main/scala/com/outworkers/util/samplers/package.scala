package com.outworkers.util

import com.outworkers.util.domain.Definitions

package object samplers extends Generators with Definitions {

  implicit class Printer[T](val obj: T) extends AnyVal {
    def trace()(implicit tracer: Tracer[T]): String = tracer.trace(obj)
  }
}

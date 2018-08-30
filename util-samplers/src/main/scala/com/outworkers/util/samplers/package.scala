package com.outworkers.util

import com.outworkers.util.domain.GenerationDomain

package object samplers extends Generators with GenerationDomain {

  implicit class Printer[T](val obj: T) extends AnyVal {
    def trace()(implicit tracer: Tracer[T]): String = tracer.trace(obj)
  }

  

}

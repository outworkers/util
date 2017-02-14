package com.outworkers.util

import com.outworkers.util.domain.GenerationDomain
import com.outworkers.util.tags.DefaultTaggedTypes

package object samplers extends Generators with GenerationDomain with DefaultTaggedTypes {

  implicit class Printer[T](val obj: T) extends AnyVal {
    def trace()(implicit tracer: Tracer[T]): String = tracer.trace(obj)
  }

}

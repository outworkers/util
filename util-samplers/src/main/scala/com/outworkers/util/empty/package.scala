package com.outworkers.util

import com.outworkers.util.domain.Definitions
import com.outworkers.util.samplers.Tracer

package object empty extends EmptyGenerators with Definitions {
  implicit class Printer[T](val obj: T) extends AnyVal {
    def trace()(implicit tracer: Tracer[T]): String = tracer.trace(obj)
  }
}




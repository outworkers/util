package com.outworkers.util.testing

object Tracers {

  class StringTracer[T] extends Tracer[T] {
    override def trace(instance: T): String = instance.toString
  }

}

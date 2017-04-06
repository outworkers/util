/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.util.samplers

object Tracers {

  class StringTracer[T] extends Tracer[T] {
    override def trace(instance: T): String = instance.toString
  }

  class OptionTracer[T : Tracer] extends Tracer[Option[T]] {
    override def trace(instance: Option[T]): String = {
      instance.fold("None")(e => Tracer[T].trace(e))
    }
  }

  class MapLikeTracer[
    M[A, B] <: TraversableOnce[(A, B)],
    Key,
    Value
  ]()(
    implicit kTracer: Tracer[Key],
    vTracer: Tracer[Value]
  ) extends Tracer[M[Key, Value]] {
    override def trace(m: M[Key, Value]): String = m.map { case (key, value) =>
      Tracer[Key].trace(key) + " " + Tracer[Value].trace(value)
    } mkString("\n")
  }

  class TraversableTracers[M[X] <: TraversableOnce[X], RR]()(
    implicit tracer: Tracer[RR]
  ) extends Tracer[M[RR]] {
    override def trace(instance: M[RR]): String = instance.map(e => tracer.trace(e)).mkString("\n")
  }

}

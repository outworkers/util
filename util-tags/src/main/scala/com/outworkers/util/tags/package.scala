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
package com.outworkers.util

package object tags {
  type @@[A, T] = Tags.Aux[A, T]

  implicit class TagOps[A](a: A) {
    def tag[T]: A @@ T = macro Tags.tagMacro[A, T]
  }

  implicit class UntagOps[A, T](at: A @@ T) {
    def untag: A = macro Tags.untagMacro[A, T]
  }
}

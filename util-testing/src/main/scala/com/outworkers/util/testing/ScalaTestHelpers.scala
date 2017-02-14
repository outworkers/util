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
package com.outworkers.util.testing

import org.scalatest.Assertions
import org.scalatest.exceptions.TestFailedException

import scala.util.control.NonFatal

trait ScalaTestHelpers {

  def shouldNotThrow[T](pf: => T): Unit = {
    try {
      pf
    } catch {
      case NonFatal(e) => {
        if (e.isInstanceOf[TestFailedException]) {
          throw e
        } else {
          Assertions.fail(s"Expected no errors to be thrown but got ${e.getMessage}")
        }
      }
    }
  }

  def mustNotThrow[T](pf: => T): Unit = shouldNotThrow[T](pf)
}


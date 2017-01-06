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

import org.scalatest.Tag

trait DefaultTags {
  object DatabaseTest extends Tag("com.outworkers.testing.tags.DatabaseTest")
  object ApiTest extends Tag("com.outworkers.testing.tags.ApiTest")
  object RequestParserTest extends Tag("com.outworkers.testing.tags.RequestParserTest")
  object UnstableTest extends Tag("com.outworkers.testing.tags.UnstableTest")
}



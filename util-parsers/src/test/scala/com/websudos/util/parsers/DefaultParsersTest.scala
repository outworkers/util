/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.util.parsers

import org.scalatest.{Matchers, FlatSpec}

class DefaultParsersTest extends FlatSpec with Matchers {

  it should "parse a long as an applicative from a valid string" in {
    val parser = parse[Long]("124")
    parser.isSuccess shouldEqual true

    parser.toOption.isDefined shouldEqual true
    parser.toOption.get shouldEqual 124L
  }

  it should "parse a long as an option from a valid string" in {
    val parser = parseOpt[Long]("124")

    parser.isDefined shouldEqual true
    parser.get shouldEqual 124L
  }

  it should "parse a long as an applicative from an optional string" in {
    val parser = parse[Long](Some("124"))
    parser.isSuccess shouldEqual true

    parser.toOption.isDefined shouldEqual true
    parser.toOption.get shouldEqual 124L
  }

  it should "fail parsing an applicative from an empty option" in {
    val parser = parse[Long](None)
    parser.isSuccess shouldEqual false
    parser.toOption.isDefined shouldEqual false
  }
}


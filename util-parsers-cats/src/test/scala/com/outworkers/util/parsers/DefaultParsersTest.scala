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
package com.outworkers.util.parsers

import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.util.{Failure, Try}
import com.outworkers.util.testing._

class DefaultParsersTest extends FlatSpec with Matchers with OptionValues {

  it should "not attempt to validate options if parseNonEmpty is used" in {
    val source = Option.empty[String]
    parseNonEmpty[Long](source).isValid shouldEqual true
  }

  it should "convert a successful scala.util.Try to a successful validation" in {
    val attempt = Try("5".toInt).asValidation
    attempt.isValid shouldEqual true
  }

  it should "convert a failed scala.util.Try to a failed validation with the respective error message" in {
    val msg = gen[String]
    val attempt: Try[String] = Failure(new Exception(msg))

    attempt.isFailure shouldEqual true
    attempt.asValidation.isInvalid shouldEqual true

    val expected = msg.invalidNel[String]
    //attempt.asValidation.compare(expected) shouldEqual 0
  }

  it should "parse a long as an applicative from a valid string" in {
    val parser = parse[Long]("124")
    parser.isValid shouldEqual true

    parser.toOption.isDefined shouldEqual true
    parser.toOption.get shouldEqual 124L
  }

  it should "parse a long as an option from a valid string" in {
    val parser = parseOpt[Long]("124")

    parser.isDefined shouldEqual true
    parser.value shouldEqual 124L
  }

  it should "parse a long as an applicative from an optional string" in {
    val parser = parse[Long](Some("124"))
    parser.isValid shouldEqual true

    parser.toOption.isDefined shouldEqual true
    parser.toOption.get shouldEqual 124L
  }

  it should "fail parsing an applicative from an empty option" in {
    val parser = parse[Long](None)
    parser.isValid shouldEqual false
    parser.toOption.isDefined shouldEqual false
  }

  "The email parser" should "parse a valid email address" in {
    val parser = parse[EmailAddress]("boti@google.com")
    parser.isValid shouldEqual true
    parser.toOption.isDefined shouldEqual true
  }

  "The email parser" should "fail to parse an invalid email address" in {
    val parser = parse[EmailAddress]("botigoogle.com")
    parser.isValid shouldEqual false
    parser.toOption.isDefined shouldEqual false
  }
}
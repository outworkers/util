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
package com.outworkers.util.play

import java.util.concurrent.TimeUnit

import cats.data.Validated.Invalid
import org.scalacheck.Gen
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}
import play.api.mvc.Results

class PlayAugmenterTests extends FlatSpec with Matchers with ScalaFutures with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  protected[this] val defaultScalaTimeoutSeconds = 25

  private[this] val defaultScalaInterval = 50L

  implicit val defaultScalaTimeout = scala.concurrent.duration.Duration(defaultScalaTimeoutSeconds, TimeUnit.SECONDS)

  private[this] val defaultTimeoutSpan = Span(defaultScalaTimeoutSeconds, Seconds)

  implicit val defaultTimeout: PatienceConfiguration.Timeout = timeout(defaultTimeoutSpan)

  override implicit val patienceConfig = PatienceConfig(
    timeout = defaultTimeoutSpan,
    interval = Span(defaultScalaInterval, Millis)
  )

  val responsesList = List(
    Results.Ok,
    Results.BadRequest,
    Results.BadGateway,
    Results.Created,
    Results.Conflict,
    Results.Continue,
    Results.Forbidden,
    Results.Unauthorized,
    Results.ServiceUnavailable,
    Results.SwitchingProtocols,
    Results.Accepted,
    Results.NonAuthoritativeInformation,
    Results.NoContent,
    Results.NotFound,
    Results.NotAcceptable,
    Results.NotImplemented,
    Results.NotModified
  )

  it should "create a successful future out of a play response" in {
    forAll(Gen.oneOf(responsesList)) { res =>
      res.future.futureValue shouldEqual res
    }
  }

  it should "correctly create an error response from an invalid Nel" in {
    "Something went wrong".invalidNel.toList
  }

}

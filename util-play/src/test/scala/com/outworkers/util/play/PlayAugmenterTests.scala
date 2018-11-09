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

import com.outworkers.util.domain.ApiError
import com.outworkers.util.domain.ShortString
import com.outworkers.util.samplers.Sample
import org.scalacheck.Gen
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Results

class PlayAugmenterTests extends FlatSpec
  with Matchers
  with ScalaFutures
  with GeneratorDrivenPropertyChecks
  with OptionValues {

  val apiErrorGen = for {
    code <- Gen.choose(200, 400)
    messages <- Gen.nonEmptyListOf(Sample.generator[ShortString]).map(_.map(_.value))
  } yield ApiError.fromArgs(code, messages)

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
    Results.Forbidden,
    Results.Unauthorized,
    Results.ServiceUnavailable,
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

  it should "serialise and deserialise an ApiError to and from JSON" in {
    forAll(apiErrorGen) { err =>
      val json = Json.toJson(err).toString()
      val parsed = Json.parse(json).validate[ApiError]
      parsed.isSuccess shouldEqual true
      parsed.asOpt.value shouldEqual err
    }
  }

  it should "serialise and deserialise an ApiError to and from JSON using the JsonHelpers" in {
    forAll(apiErrorGen) { err =>
      val json = err.json()
      val parsed = Json.parse(json).validate[ApiError]
      parsed.isSuccess shouldEqual true
      parsed.asOpt.value shouldEqual err
    }
  }

  it should "serialise and deserialise an ApiError to and from JSON using the JsonHelpers jsonString" in {
    forAll(apiErrorGen) { err =>
      val json = err.jsValue().toString()
      val parsed = Json.parse(json).validate[ApiError]
      parsed.isSuccess shouldEqual true
      parsed.asOpt.value shouldEqual err
    }
  }
}

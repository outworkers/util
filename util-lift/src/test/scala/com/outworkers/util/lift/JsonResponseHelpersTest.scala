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
package com.outworkers.util.lift

class JsonResponseHelpersTest extends LiftTest {

  it should "create a 204 response from an empty product list" in {
    val list = List.empty[Product with Serializable]

    shouldNotThrow {
      list.asResponse().toResponse.code shouldEqual 204
    }
  }

  it should "create a 200 response from a valid non-empty product list" in {
    val list = genList[TestClass]()

    shouldNotThrow {
      list.asResponse().toResponse.code shouldEqual 200
    }
  }

  it should "create a JSON error response from an empty product list" in {
    val resp = JsonErrorResponse("This went wrong", 400)

    resp.toResponse.code shouldEqual 400
    info(resp.toResponse.toString)
  }

  it should "create a JSON Unauthorised response" in {
    val resp = JsonUnauthorizedResponse()

    resp.toResponse.code shouldEqual 401
    info(resp.toResponse.toString)
  }
}

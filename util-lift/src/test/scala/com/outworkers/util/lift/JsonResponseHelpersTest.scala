package com.outworkers.util.lift

import com.outworkers.util.testing._


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

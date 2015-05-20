package com.websudos.util.lift

import com.websudos.util.testing._


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
}

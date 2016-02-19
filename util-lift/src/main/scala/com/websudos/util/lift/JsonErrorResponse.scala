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
package com.websudos.util.lift

import com.websudos.util.domain.ApiError
import net.liftweb.http.js.JsExp
import net.liftweb.http.provider.HTTPCookie
import net.liftweb.http.{InMemoryResponse, JsonResponse, LiftResponse, LiftRules, S}
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._

case class JsonErrorResponse(
  json: JsExp,
  headers: List[(String, String)],
  cookies: List[HTTPCookie] = Nil) {

  protected[this] val defaultErrorResponse = 400

  def toResponse: LiftResponse = {
    val bytes = json.toJsCmd.getBytes
    InMemoryResponse(
      bytes,
      ("Content-Length", bytes.length.toString) :: ("Content-Type", "application/json; charset=utf-8") :: headers,
      cookies,
      defaultErrorResponse
    )
  }
}

object JsonErrorResponse {

  implicit val formats = net.liftweb.json.DefaultFormats

  def headers: List[(String, String)] = S.getResponseHeaders(Nil)
  def cookies: List[HTTPCookie] = S.responseCookies

  def apply(json: JsExp, code: Int): LiftResponse =
    JsonResponse(json, headers, cookies, code)

  def apply(msg: String, code: Int): LiftResponse = {
    val resp = ApiError.fromArgs(code, List(msg))
    JsonResponse(decompose(resp), code)
  }

  def apply(ex: Exception, code: Int): LiftResponse = {
    apply(ex.getMessage, code)
  }

  def apply(_json: JsonAST.JValue, _headers: List[(String, String)], _cookies: List[HTTPCookie], _code: Int): LiftResponse = {
    new JsonResponse(new JsExp {
      lazy val toJsCmd = jsonPrinter(JsonAST.compactRender(_json))
    }, _headers, _cookies, _code)
  }

  lazy val jsonPrinter: JValue => String = LiftRules.jsonOutputConverter.vend
}

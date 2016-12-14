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

import java.nio.charset.StandardCharsets

import com.outworkers.util.domain.ApiErrorResponse
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http._
import net.liftweb.http.js.JsExp
import net.liftweb.http.provider.HTTPCookie
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._

case class JsonUnauthorizedResponse(
 json: JsExp,
 headers: List[(String, String)],
 cookies: List[HTTPCookie] = Nil) {
   def toResponse: InMemoryResponse = {
     val bytes = json.toJsCmd.getBytes(StandardCharsets.UTF_8)

     InMemoryResponse(bytes,
       "Content-Length" -> bytes.length.toString :: "Content-Type" -> "application/json; charset=utf-8" :: headers,
       cookies,
       401
     )
   }
 }

object JsonUnauthorizedResponse {

  protected[this] implicit val formats = net.liftweb.json.DefaultFormats
  protected[this] final val unauthorizedCode = 401

  implicit def jsonUnauthorizedToLiftResponse(resp: JsonUnauthorizedResponse): LiftResponse = {
    resp.toResponse
  }

  def headers: List[(String, String)] = S.getResponseHeaders(Nil)
  def cookies: List[HTTPCookie] = S.responseCookies

  def apply(json: JsExp): LiftResponse =
    JsonResponse(json, headers, cookies, unauthorizedCode)

  def apply(): LiftResponse = {
    val resp = ApiErrorResponse(unauthorizedCode, List("Unauthorized request"))
    val json = "error" -> decompose(resp)
    JsonResponse(json, unauthorizedCode)
  }

  def apply(msg: String): LiftResponse = {
    val resp = ApiErrorResponse(unauthorizedCode, List(msg))
    val json = "error" -> decompose(resp)
    JsonResponse(json, unauthorizedCode)
  }

  def apply(_json: JsonAST.JValue, _headers: List[(String, String)], _cookies: List[HTTPCookie]): LiftResponse = {
    new JsonResponse(new JsExp {
      lazy val toJsCmd = jsonPrinter(JsonAST.render(_json))
    }, _headers, _cookies, unauthorizedCode)
  }

  lazy val jsonPrinter: scala.text.Document => String = LiftRules.jsonOutputConverter.vend
}

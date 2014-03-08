package com.newzly.util.lift

import net.liftweb.http.js.JsExp
import net.liftweb.http.{ InMemoryResponse, LiftResponse, LiftRules, JsonResponse, S }
import net.liftweb.http.provider.HTTPCookie
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.json.JsonAST
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonDSL._

case class JsonUnauthorizedResponse(
 json: JsExp,
 headers: List[(String, String)],
 cookies: List[HTTPCookie] = Nil) {
  def toResponse = {
    val bytes = json.toJsCmd.getBytes("UTF-8")
    InMemoryResponse(bytes, ("Content-Length", bytes.length.toString) :: ("Content-Type", "application/json; charset=utf-8") :: headers, cookies, 401)
  }
}

object JsonUnauthorizedResponse {

  implicit val formats = net.liftweb.json.DefaultFormats

  implicit def jsonUnauthorizedToLiftResponse(resp: JsonUnauthorizedResponse): LiftResponse = {
    resp.toResponse
  }

  def headers: List[(String, String)] = S.getResponseHeaders(Nil)
  def cookies: List[HTTPCookie] = S.responseCookies

  def apply(json: JsExp): LiftResponse =
    JsonResponse(json, headers, cookies, 401)

  def apply(): LiftResponse = {
    val resp = ApiErrorResponse("Unauthorized request", 401)
    val json = "error" -> decompose(resp)
    JsonResponse(json, 401)
  }

  def apply(msg: String): LiftResponse = {
    val resp = ApiErrorResponse(msg, 401)
    val json = "error" -> decompose(resp)
    JsonResponse(json, 401)
  }

  def apply(_json: JsonAST.JValue, _headers: List[(String, String)], _cookies: List[HTTPCookie]): LiftResponse = {
    new JsonResponse(new JsExp {
      lazy val toJsCmd = jsonPrinter(JsonAST.render(_json))
    }, _headers, _cookies, 401)
  }

  lazy val jsonPrinter: scala.text.Document => String =
    LiftRules.jsonOutputConverter.vend
}


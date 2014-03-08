package com.newzly.util.lift

import net.liftweb.http.js.JsExp
import net.liftweb.http.provider.HTTPCookie
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonDSL._
import net.liftweb.http.{ InMemoryResponse, JsonResponse, LiftResponse, LiftRules, S }

case class ApiErrorResponse(
 message: String,
 code: Int
)

case class JsonErrorResponse(
  json: JsExp,
  headers: List[(String, String)],
  cookies: List[HTTPCookie] = Nil) {
  def toResponse = {
    val bytes = json.toJsCmd.getBytes("UTF-8")
    InMemoryResponse(bytes, ("Content-Length", bytes.length.toString) :: ("Content-Type", "application/json; charset=utf-8") :: headers, cookies, 401)
  }
}

object JsonErrorResponse {

  implicit val formats = net.liftweb.json.DefaultFormats

  def headers: List[(String, String)] = S.getResponseHeaders(Nil)
  def cookies: List[HTTPCookie] = S.responseCookies

  def apply(json: JsExp): LiftResponse =
    JsonResponse(json, headers, cookies, 401)

  def apply(msg: String): LiftResponse = {
    val resp = ApiErrorResponse(msg, 406)
    val json = "error" -> decompose(resp)
    JsonResponse(json, 406)
  }

  def apply(msg: String, code: Int): LiftResponse = {
    val resp = ApiErrorResponse(msg, code)
    val json = "error" -> decompose(resp)
    JsonResponse(json, code)
  }

  def apply(ex: Exception, code: Int): LiftResponse = {
    apply(ex.getMessage, code)
  }

  def apply(_json: JsonAST.JValue, _headers: List[(String, String)], _cookies: List[HTTPCookie]): LiftResponse = {
    new JsonResponse(new JsExp {
      lazy val toJsCmd = jsonPrinter(JsonAST.render(_json))
    }, _headers, _cookies, 401)
  }

  lazy val jsonPrinter: scala.text.Document => String =
    LiftRules.jsonOutputConverter.vend
}

package com.websudos.util.http

object HttpExtractor {

  def extractTokenResponse(response: String): Map[String, String] = {
    val params = (for {
      x <- response split "&"
      Array(k, v) = x split "="
    } yield k -> v).toMap
    params
  }
}

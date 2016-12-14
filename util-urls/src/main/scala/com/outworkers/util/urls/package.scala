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
package com.outworkers.util

import java.net.{URL, URLDecoder, URLEncoder}

package object urls {

  final val Utf8 = "UTF_8"

  implicit class URLBuilder(val url: String) extends AnyVal {

    final def subpath(path: String): String = if (url.last != '/') url + "/" + path else url + path

    final def /(path: String): String = subpath(path)

    def queryParam(param: (String, String)): String = {
      if (url.indexOf("?") == -1 && url.length > 0) {
        url + "?" + param._1 + "=" + param._2
      } else if (url.length > 0) {
        url + "&" + param._1 + "=" + param._2
      } else {
        url + param._1 + "=" + param._2
      }
    }

    def <<?(param: (String, String)): String = queryParam(param)

    final def queryParams(params: (String, String)*): String = {
      url + params.foldRight("") {
        case ((key, value), acc) => acc match {
          case "" => s"$acc?$key=$value"
          case _ => s"$acc&$key=$value"
        }
      }
    }

    final def asUri: URL = new URL(url)

    final def utf8: String = URLEncoder.encode(url, Utf8)

    final def fromUtf8: String = URLDecoder.decode(url, Utf8)

  }

  implicit class RichURL(val url: URL) extends AnyVal {
    final def /(path: String): URL = new URL(url.toString + "/" + path)

    final def <<?(param: (String, String)): URL = {
      val uri = url.toString
      if (uri.indexOf("?") == -1) {
        new URL(uri + "?" + param._1 + "=" + param._2)
      } else {
        new URL(uri + "&" + param._1 + "=" + param._2)
      }
    }
  }

  def :/(uri: String): String = if (uri.indexOf("http") == -1) "https://" + uri else uri
  def https(uri: String): String = if (uri.indexOf("http") == -1) "https://" + uri else uri

  def ::/(uri: String): String = if (uri.indexOf("http") == -1) "http://" + uri else uri
  def http(uri: String): String = if (uri.indexOf("http") == -1) "http://" + uri else uri


}

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
package com.websudos

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
  def ::/(uri: String): String = if (uri.indexOf("http") == -1) "http://" + uri else uri


}

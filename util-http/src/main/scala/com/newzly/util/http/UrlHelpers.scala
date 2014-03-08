package com.newzly.util.http

import java.net.{URLDecoder, URL, URLEncoder}
import java.util.UUID
import scala.collection.breakOut

import org.jboss.netty.handler.codec.http.HttpResponse

object URLHelpers {

  /**
   * Implicit value class used to simplify extracting responses from a Netty HTTP response.
   * @param response The Http response to augment.
   */
  implicit class RichHttpResponse(val response: HttpResponse) extends AnyVal {

    def body: String = {
      new String(response.getContent.array)
    }
  }

  implicit class UUIDExtractor(val id: String) extends AnyVal {
    final def extractUUID: Option[UUID] = {
      try {
        Some(UUID.fromString(id))
      } catch {
        case e: IllegalArgumentException => None
      }
    }
  }

  implicit class URLBuilder(val url: String) extends AnyVal {

    final def /(path: String): String = if (url.last != '/') url + "/" + path else url + path

    def <<?(param: (String, String)): String =
      if (url.indexOf("?") == -1 && url.length > 0)
        url + "?" + param._1 + "=" + param._2
      else if (url.length > 0) {
        url + "&" + param._1 + "=" + param._2
      } else {
        url + param._1 + "=" + param._2
      }

    final def <<?(params: List[(String, String)]): String =
      url + params.tail.flatMap(param => { "&" + param._1 + "=" + param._2 })(breakOut)

    final def asUri: URL = new URL(url)

    final def utf8: String = URLEncoder.encode(url, UTF_8)

    final def fromUtf8: String = URLDecoder.decode(url, UTF_8)

  }

  implicit class RichURL(val url: URL) extends AnyVal {
    final def /(path: String): URL = new URL(url.toString + "/" + path)

    final def <<?(param: (String, String)): URL = {
      val uri = url.toString
      if (uri.indexOf("?") == -1)
        new URL(uri + "?" + param._1 + "=" + param._2)
      else
        new URL(uri + "&" + param._1 + "=" + param._2)

    }
  }

  final val UTF_8 = "UTF-8"

  def :/(uri: String): String = if (uri.indexOf("http") == -1) "https://" + uri else uri
  def ::/(uri: String): String = if (uri.indexOf("http") == -1) "http://" + uri else uri
}


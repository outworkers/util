package com.outworkers.util

import java.net.{URL, URLDecoder, URLEncoder}

import com.twitter.finagle.http.RequestBuilder
import com.twitter.io.Charsets.Utf8
import com.twitter.util.{Future => TwitterFuture, Promise => TwitterPromise, Return, Throw}
import org.jboss.netty.handler.codec.http.HttpResponse

import scala.collection.breakOut
import scala.concurrent.{Future => ScalaFuture, Promise => ScalaPromise, ExecutionContext}
import scala.util.{Failure, Success}

package object http {

  implicit class RichResponseBuilder[X, Y](val builder: RequestBuilder[X, Y]) extends AnyVal {
    def asJson(): RequestBuilder[X, Y] = {
      builder.setHeader("Content-Type", "application/json")
    }
  }

  /**
   * Implicit value class used to simplify extracting responses from a Netty HTTP response.
    *
    * @param resp The Http response to augment.
   */
  implicit class RichHttpResponse(val resp: HttpResponse) extends AnyVal {

    def buffer: Array[Byte] = {
      val channelBuffer = resp.getContent

      val length = channelBuffer.readableBytes()
      val bytes = new Array[Byte](length)
      channelBuffer.getBytes(channelBuffer.readerIndex(), bytes, 0, length)
      bytes
    }

    def responseBody: String = {
      new String(buffer)
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

    final def utf8: String = URLEncoder.encode(url, Utf8.displayName())

    final def fromUtf8: String = URLDecoder.decode(url, Utf8.displayName())

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

  def :/(uri: String): String = if (uri.indexOf("http") == -1) "https://" + uri else uri
  def ::/(uri: String): String = if (uri.indexOf("http") == -1) "http://" + uri else uri


  implicit class TwitterFutureConverter[T](val future: TwitterFuture[T]) extends AnyVal {
    def asScala: ScalaFuture[T] = {
      val promise = ScalaPromise[T]()

      future respond {
        case Return(value) => promise success value
        case Throw(err) => promise failure err
      }

      promise.future
    }
  }

  implicit class ScalaFutureConverter[T](val future: ScalaFuture[T]) extends AnyVal {
    def asTwitter()(implicit context: ExecutionContext): TwitterFuture[T] = {
      val promise = TwitterPromise[T]()

      future onComplete {
        case Success(value) => promise become TwitterFuture.value(value)
        case Failure(err) => promise raise err
      }

      promise
    }
  }

}

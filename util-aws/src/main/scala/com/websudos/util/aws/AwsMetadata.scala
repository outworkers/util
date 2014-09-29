package com.websudos.util.aws

import java.net.InetSocketAddress

import scala.annotation.switch

import org.jboss.netty.handler.codec.http.HttpResponseStatus

import com.twitter.conversions.time._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{Http, RequestBuilder}
import com.twitter.util.Future
import com.websudos.util.http.URLHelpers._


object AwsMetadata {

  /**
   * This URI is not hard coded by our implementation, it is indeed hard coded by Amazon Web Services through their own conventions.
   * This convention is backed up by their documentation and the fixed IP is outside of our network.
   *
   * It's the same IP used by all AWS customers, assuming there is always a service running on that port that gives instance details.
   */
  val awsUri = "169.254.169.254"

  lazy val client = ClientBuilder()
    .codec(Http())
    .hosts(new InetSocketAddress(awsUri, 80))
    .tcpConnectTimeout(5.seconds)
    .hostConnectionLimit(2)
    .retries(2)
    .failFast(onOrOff = false)
    .build()

  def getMetadata: Future[Option[String]] = {
    val url = ::/(awsUri) / "latest" / "dynamic" / "instance-identity" / "document"
    val req = RequestBuilder().url(url.asUri).buildGet()

    client(req) map  {
      response => {
        (response.getStatus: @switch) match {
          case HttpResponseStatus.OK => Some(response.body)
          case _ => None
        }
      }
    }
  }
}

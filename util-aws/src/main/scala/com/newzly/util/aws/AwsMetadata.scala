package com.newzly.util.aws

import java.net.InetSocketAddress
import scala.annotation.switch
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import com.websudos.util.http.URLHelpers
import URLHelpers._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{ Http, RequestBuilder }
import com.twitter.util.{Try, Duration, Future}


object AwsMetadata {

  val awsUri = "169.254.169.254"

  lazy val client = ClientBuilder()
    .codec(Http())
    .hosts(new InetSocketAddress(awsUri, 80))
    .tcpConnectTimeout(Duration.fromSeconds(5))
    .hostConnectionLimit(2)
    .retries(2)
    .failFast(onOrOff = false)
    .build()


  def getMetadata: Future[Option[String]] = {

    Try {
      val url = ::/(awsUri) / "latest" / "dynamic" / "instance-identity" / "document"
      val req = RequestBuilder().url(url.asUri)
        .buildGet()
      client(req) map  {
        response => {
          (response.getStatus: @switch) match {
            case HttpResponseStatus.OK => Some(response.body)
            case _ => None
          }
        }
      }
    }
  } getOrElse {
    Future.value(None)
  }
}

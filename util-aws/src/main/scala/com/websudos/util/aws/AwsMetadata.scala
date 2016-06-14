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
package com.websudos.util.aws

import java.io.{BufferedInputStream, BufferedOutputStream, InputStream, StringReader}
import java.net.InetSocketAddress

import com.twitter.conversions.time._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{Http, RequestBuilder}
import com.twitter.util.Future
import com.websudos.urls._
import org.jboss.netty.handler.codec.http.HttpResponseStatus

object AwsMetadata {

  /**
   * This URI is not hard coded by our implementation, it is instead hard coded by Amazon Web Services through their own conventions.
   * This convention is backed up by their documentation and the fixed IP is outside of our network.
   *
   * It's the same IP used by all AWS customers, assuming there is always a service running on that port that gives instance details.
   */
  val AwsStaticConfigurationIP = "169.254.169.254"

  lazy val client = ClientBuilder()
    .codec(Http())
    .hosts(new InetSocketAddress(AwsStaticConfigurationIP, 80))
    .tcpConnectTimeout(5.seconds)
    .hostConnectionLimit(2)
    .retries(2)
    .failFast(enabled = false)
    .build()

  def metadata: Future[Option[String]] = {
    val url = ::/(AwsStaticConfigurationIP) / "latest" / "dynamic" / "instance-identity" / "document"
    val req = RequestBuilder().url(url.asUri).buildGet()

    client(req) map  {
      response => {
        if (response.status.code == HttpResponseStatus.OK.getCode) {
          Some(response.contentString)
        } else {
          None
        }
      }
    }
  }
}

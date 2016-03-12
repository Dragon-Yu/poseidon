package https.client;/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import config.BaseTestConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 */
public final class HttpsClient {
  static long startTime, endTime;
  static Logger logger = LoggerFactory.getLogger(HttpsClient.class);

  public static void main(String[] args) throws Exception {
    URI uri = new URI(BaseTestConfig.URI);
    int port = uri.getPort();
    String host = uri.getHost();

    // Configure SSL context if necessary.
    final SslContext sslCtx;
    sslCtx = SslContextBuilder.forClient()
      .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

    // Configure the client.
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
        .channel(NioSocketChannel.class)
        .handler(new HttpsInitializer(sslCtx));

      // Make the connection attempt.
      startTime = System.nanoTime();
      Channel ch = b.connect(host, port).sync().channel();

      // Prepare the HTTP request.
      HttpRequest request = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
      request.headers().set(HttpHeaderNames.HOST, host);
      request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
      request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

      // Send the HTTP request.
      ch.writeAndFlush(request);

      // Wait for the server to close the connection.
      ch.closeFuture().sync();
      endTime = System.nanoTime();
      long duration = endTime - startTime;
      logger.info(String.format("connection duration: %,dns (%d)", duration, duration));
    } finally {
      // Shut down executor threads to exit.
      group.shutdownGracefully();
    }
  }
}

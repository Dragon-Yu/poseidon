package https.client;

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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 */
public final class HttpsClient {
  static long startTime, endTime;
  static Logger logger = LoggerFactory.getLogger(HttpsClient.class);
  static AtomicInteger counter = new AtomicInteger(1);
  HttpsInitializer httpsInitializer;

  public void run(URI uri) throws Exception {
    int port = uri.getPort() > 0 ? uri.getPort() : BaseTestConfig.HTTPS_PORT;
    String host = uri.getHost();

    // Configure SSL context if necessary.
    final SslContext sslCtx;
    sslCtx = SslContextBuilder.forClient()
      .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

    // Configure the client.
    EventLoopGroup group = new NioEventLoopGroup();
    httpsInitializer = new HttpsInitializer(sslCtx);
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
        .channel(NioSocketChannel.class)
        .handler(httpsInitializer);

      // Make the connection attempt.
      startTime = System.nanoTime();
      Channel ch = b.connect(host, port).sync().channel();
      logger.info("target host: " + ch.remoteAddress());

      for (int i = 0; i < BaseTestConfig.REQUEST_TIMES; i++) {
        HttpRequest request = new DefaultFullHttpRequest(
          HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        ch.writeAndFlush(request);
      }

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

  public long getTimeElapsed() {
    return endTime - startTime;
  }

  public long getRequestSize() {
    return httpsInitializer.channelTrafficShapingHandler.trafficCounter().cumulativeWrittenBytes();
  }

  public long getResponseSize() {
    return httpsInitializer.channelTrafficShapingHandler.trafficCounter().cumulativeReadBytes();
  }
}

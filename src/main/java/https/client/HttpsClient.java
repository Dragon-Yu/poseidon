package https.client;

import config.BaseTestConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 */
public class HttpsClient {
  static long startTime, endTime;
  static Logger logger = LoggerFactory.getLogger(HttpsClient.class);
  static AtomicInteger counter = new AtomicInteger(1);
  protected int requestTimes = BaseTestConfig.REQUEST_TIMES;
  protected HttpsInitializer httpsInitializer;
  SocketAddress remoteAddress;
  SocketAddress localAddress;

  public void run(URI uri) throws Exception {
    int port = uri.getPort() > 0 ? uri.getPort() : BaseTestConfig.HTTPS_PORT;
    String host = uri.getHost();

    // Configure SSL context if necessary.
    final SslContext sslCtx;
    sslCtx = SslContextBuilder.forClient()
      .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

    // Configure the client.
    EventLoopGroup group = new NioEventLoopGroup();
    httpsInitializer = generateHttpsInitializer(sslCtx);
    try {
      Bootstrap b = new Bootstrap();
      b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
      b.group(group)
        .channel(NioSocketChannel.class)
        .handler(httpsInitializer);

      // Make the connection attempt.
      startTime = System.nanoTime();
      Channel ch = b.connect(host, port).sync().channel();
      localAddress = ch.localAddress();
      remoteAddress = ch.remoteAddress();

      sendRequests(uri.toURL(), ch);

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

  protected void sendRequests(URL url, Channel channel) {
    for (int i = 0; i < requestTimes; i++) {
      HttpRequest request = RequestUtil.generateHttpsRequest(url);
      channel.writeAndFlush(request);
    }
  }

  protected HttpsInitializer generateHttpsInitializer(SslContext sslContext) {
    return new HttpsInitializer(sslContext);
  }

  public InetSocketAddress getRemoteAddress() {
    return (InetSocketAddress) remoteAddress;
  }

  public InetSocketAddress getLocalAddress() {
    return (InetSocketAddress) localAddress;
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

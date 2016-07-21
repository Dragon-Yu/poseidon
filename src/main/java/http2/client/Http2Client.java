package http2.client;

import config.BaseTestConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import main.Http2SupportChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestUtil;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Http2 client starter
 * Created by johnson on 16/1/11.
 */
public class Http2Client {
  private static String HOST;
  private static int PORT;
  private static Logger logger = LoggerFactory.getLogger(Http2Client.class);
  private static long startTime, endTime;
  private static int REQUEST_TIMES = BaseTestConfig.REQUEST_TIMES;
  protected Http2ClientInitializer initializer;
  private SocketAddress localAddress;
  private SocketAddress remoteAddress;

  public void run(URI uri) throws Exception {
    HOST = uri.getHost();
    PORT = uri.getPort() > 0 ? uri.getPort() : BaseTestConfig.HTTPS_PORT;

    final SslContext sslCtx;
    SslProvider provider = SslProvider.JDK;
    sslCtx = SslContextBuilder.forClient()
      .sslProvider(provider)
        /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
         * Please refer to the HTTP/2 specification for cipher requirements. */
      .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
      .trustManager(InsecureTrustManagerFactory.INSTANCE)
      .applicationProtocolConfig(new ApplicationProtocolConfig(
        ApplicationProtocolConfig.Protocol.ALPN,
//           NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
//           ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
        ApplicationProtocolNames.HTTP_2,
        ApplicationProtocolNames.HTTP_1_1))
      .build();


    EventLoopGroup workerGroup = new NioEventLoopGroup();
    initializer = generateInitializer(sslCtx, Integer.MAX_VALUE);

    try {
      // Configure the client.
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(workerGroup);
      bootstrap.channel(NioSocketChannel.class);
      bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
      bootstrap.remoteAddress(HOST, PORT);
      bootstrap.handler(initializer);
      bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

      startTime = System.nanoTime();
      // Start the client.
      Channel channel = bootstrap.connect().syncUninterruptibly().channel();
      Http2SupportChecker.logSocketAddress(channel.remoteAddress(), uri);
      localAddress = channel.localAddress();
      remoteAddress = channel.remoteAddress();
      logger.info("Connected to [" + HOST + ':' + PORT + ']');

      // Wait for the HTTP/2 upgrade to occur.
      Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
      http2SettingsHandler.awaitSettings(60, TimeUnit.SECONDS);

      Http2ResponseHandler responseHandler = initializer.responseHandler();
      sendInitRequests(responseHandler, channel, uri);
      responseHandler.awaitResponses(300, TimeUnit.SECONDS);
      logger.info("Finished HTTP/2 request(s)");

      // Wait until the connection is closed.
      channel.close().syncUninterruptibly();
      endTime = System.nanoTime();
      long duration = endTime - startTime;
      logger.info(String.format("connection duration: %,dns (%d)", duration, duration));
    } finally {
      logger.info("shutting down");
      workerGroup.shutdownGracefully();
    }
  }

  public void sendInitRequests(Http2ResponseHandler responseHandler, Channel channel, URI uri)
    throws MalformedURLException {
    logger.info("Sending request(s)...");
    for (int i = 0; i < REQUEST_TIMES; i++) {
      // Create a simple GET request.
      FullHttpRequest request = RequestUtil.generateHttpRequest(uri.toURL());
      responseHandler.put(RequestUtil.getStreamId(request), channel.writeAndFlush(request), channel.newPromise());
    }
  }

  public void setRequestTimes(int requestTimes) {
    REQUEST_TIMES = requestTimes;
  }

  public Http2ClientInitializer generateInitializer(SslContext sslCtx, int maxContentLength) {
    return new Http2ClientInitializer(sslCtx, maxContentLength);
  }

  public InetSocketAddress getLocalAddress() {
    return (InetSocketAddress) localAddress;
  }

  public InetSocketAddress getRemoteAddress() {
    return (InetSocketAddress) remoteAddress;
  }

  public long getTimeElapsed() {
    return endTime - startTime;
  }

  public long getRequestSize() {
    return initializer.channelTrafficShapingHandler.trafficCounter().cumulativeWrittenBytes();
  }

  public long getResponseSize() {
    return initializer.channelTrafficShapingHandler.trafficCounter().cumulativeReadBytes();
  }
}

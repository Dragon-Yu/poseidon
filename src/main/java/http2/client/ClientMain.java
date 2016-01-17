package http2.client;

import config.BaseTestConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Http2 client starter
 * Created by johnson on 16/1/11.
 */
public class ClientMain {
  static final boolean SSL = BaseTestConfig.SSL;
  static final String HOST = BaseTestConfig.HOST;
  static final int PORT = BaseTestConfig.PORT;
  static final String URL = BaseTestConfig.URL;
  static Logger logger = LoggerFactory.getLogger(ClientMain.class);
  static long startTime, endTime;
  static int REQUEST_TIMES = BaseTestConfig.REQUEST_TIMES;

  public static void main(String[] args) throws Exception {
    // Configure SSL.
    final SslContext sslCtx;
    if (SSL) {
      SslProvider provider = SslProvider.JDK;
      sslCtx = SslContextBuilder.forClient()
        .sslProvider(provider)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .applicationProtocolConfig(new ApplicationProtocolConfig(
          ApplicationProtocolConfig.Protocol.ALPN,
          // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
          ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
          // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
          ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
          ApplicationProtocolNames.HTTP_2,
          ApplicationProtocolNames.HTTP_1_1))
        .build();
    } else {
      sslCtx = null;
    }

    EventLoopGroup workerGroup = new NioEventLoopGroup();
    Http2ClientInitializer initializer = new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE);

    try {
      // Configure the client.
      Bootstrap b = new Bootstrap();
      b.group(workerGroup);
      b.channel(NioSocketChannel.class);
      b.option(ChannelOption.SO_KEEPALIVE, true);
      b.remoteAddress(HOST, PORT);
      b.handler(initializer);

      startTime = System.nanoTime();
      // Start the client.
      Channel channel = b.connect().syncUninterruptibly().channel();
      logger.info("Connected to [" + HOST + ':' + PORT + ']');

      // Wait for the HTTP/2 upgrade to occur.
      Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
      http2SettingsHandler.awaitSettings(500, TimeUnit.SECONDS);

      HttpResponseHandler responseHandler = initializer.responseHandler();
      int streamId = 3;
      HttpScheme scheme = SSL ? HttpScheme.HTTPS : HttpScheme.HTTP;
      AsciiString hostName = new AsciiString(HOST + ':' + PORT);
      logger.info("Sending request(s)...");
      if (URL != null) {
        for (int i = 0; i < REQUEST_TIMES; i++) {
          // Create a simple GET request.
          FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, URL);
          request.headers().add(HttpHeaderNames.HOST, hostName);
          request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
          request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
          request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
          responseHandler.put(streamId, channel.writeAndFlush(request), channel.newPromise());
          streamId += 2;
        }
      }
      responseHandler.awaitResponses(500, TimeUnit.SECONDS);
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
}

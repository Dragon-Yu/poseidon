package http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Http2 client starter
 * Created by johnson on 16/1/11.
 */
public class ClientMain {
  static final boolean SSL = true;
//  static final String HOST = "http2.akamai.com";
  static final String HOST = "http://isports-1093.appspot.com";
  static final int PORT = 443;
//  static final String URL = "/demo";
  static final String URL = "/ping";
  static Logger logger = LoggerFactory.getLogger(ClientMain.class);
  static long startTime, endTime;

  public static void main(String[] args) throws Exception {
    // Configure SSL.
    final SslContext sslCtx;
    if (SSL) {
      SslProvider provider = SslProvider.OPENSSL;
      sslCtx = SslContextBuilder.forClient()
        .sslProvider(provider)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
//        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
//        .applicationProtocolConfig(new ApplicationProtocolConfig(
//          ApplicationProtocolConfig.Protocol.ALPN,
//           NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
//          ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
//           ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
//          ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
//          ApplicationProtocolNames.HTTP_1_1))
        .build();
    } else {
      sslCtx = null;
    }

    EventLoopGroup workerGroup = new NioEventLoopGroup();
    HttpClientInitializer initializer = new HttpClientInitializer(sslCtx, Integer.MAX_VALUE);

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

//      HttpResponseHandler responseHandler = initializer.responseHandler();
      AsciiString hostName = new AsciiString(HOST + ':' + PORT);
      logger.info("Sending request(s)...");
      if (URL != null) {
        // Create a simple GET request.
        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, URL);
        request.headers().add(HttpHeaderNames.HOST, hostName);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
        channel.writeAndFlush(request);
      }
//      responseHandler.awaitResponses(500, TimeUnit.SECONDS);
      logger.info("Finished HTTP/2 request(s)");

      // Wait until the connection is closed.
      channel.close().syncUninterruptibly();
//      channel.closeFuture().sync();
      endTime = System.nanoTime();
      long duration = endTime - startTime;
      logger.info(String.format("connection duration: %,d (%d)", duration, duration));
    } finally {

      logger.info("shutting down");
      workerGroup.shutdownGracefully();
    }
  }
}

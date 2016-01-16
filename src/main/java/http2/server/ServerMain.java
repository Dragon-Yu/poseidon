package http2.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.example.http2.helloworld.server.Http2ServerInitializer;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Created by johnson on 16/1/11.
 */
public class ServerMain {
  static final boolean SSL = true;

  static final int PORT = 5555;

  public static void main(String[] args) throws Exception {
    // Configure SSL.
    final SslContext sslCtx;
    if (SSL) {
      SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
      SelfSignedCertificate ssc = new SelfSignedCertificate();
      sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
        .sslProvider(provider)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
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
    // Configure the server.
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.option(ChannelOption.SO_BACKLOG, 1024);
      b.group(group)
        .channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new Http2ServerInitializer(sslCtx));

      Channel ch = b.bind(PORT).sync().channel();

      System.err.println("Open your HTTP/2-enabled web browser and navigate to " +
        (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

      ch.closeFuture().sync();
    } finally {
      group.shutdownGracefully();
    }
  }
}

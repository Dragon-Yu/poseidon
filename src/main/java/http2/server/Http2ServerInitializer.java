package http2.server;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Johnson on 16/5/3.
 */
public class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {
  private static final Logger logger = LoggerFactory.getLogger(Http2ServerInitializer.class);

  private static final HttpServerUpgradeHandler.UpgradeCodecFactory upgradeCodecFactory = new HttpServerUpgradeHandler.UpgradeCodecFactory() {
    @Override
    public HttpServerUpgradeHandler.UpgradeCodec newUpgradeCodec(CharSequence protocol) {
      if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
        return new Http2ServerUpgradeCodec(new HelloWorldHttp2HandlerBuilder().build());
      } else {
        return null;
      }
    }
  };

  private final SslContext sslCtx;
  private final int maxHttpContentLength;

  public Http2ServerInitializer(SslContext sslCtx) {
    this(sslCtx, 16 * 1024);
  }

  public Http2ServerInitializer(SslContext sslCtx, int maxHttpContentLength) {
    if (maxHttpContentLength < 0) {
      throw new IllegalArgumentException("maxHttpContentLength (expected >= 0): " + maxHttpContentLength);
    }
    this.sslCtx = sslCtx;
    this.maxHttpContentLength = maxHttpContentLength;
  }

  @Override
  public void initChannel(SocketChannel ch) {
    if (sslCtx != null) {
      configureSsl(ch);
    } else {
      configureClearText(ch);
    }
  }

  /**
   * Configure the pipeline for TLS NPN negotiation to HTTP/2.
   */
  private void configureSsl(SocketChannel ch) {
    ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), new Http2OrHttpHandler());
  }

  /**
   * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.0
   */
  private void configureClearText(SocketChannel ch) {
    final ChannelPipeline p = ch.pipeline();
    final HttpServerCodec sourceCodec = new HttpServerCodec();

    p.addLast(sourceCodec);
    p.addLast(new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory));
    p.addLast(new SimpleChannelInboundHandler<HttpMessage>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
        // If this handler is hit then no upgrade has been attempted and the client is just talking HTTP.
        logger.info("Directly talking: " + msg.protocolVersion() + " (no upgrade was attempted)");
        ChannelPipeline pipeline = ctx.pipeline();
        ChannelHandlerContext thisCtx = pipeline.context(this);
        pipeline.addAfter(thisCtx.name(), null, new HelloWorldHttp1Handler("Direct. No Upgrade Attempted."));
        pipeline.replace(this, null, new HttpObjectAggregator(maxHttpContentLength));
        ctx.fireChannelRead(msg);
      }
    });

    p.addLast(new Http2ServerInitializer.UserEventLogger());
  }

  /**
   * Class that logs any User Events triggered on this channel.
   */
  private static class UserEventLogger extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
      logger.info("User Event Triggered: " + evt);
      ctx.fireUserEventTriggered(evt);
    }
  }
}

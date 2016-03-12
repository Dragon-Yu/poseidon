package http2.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by johnson on 16/1/11.
 */
public class Http2ClientInitializer extends ChannelInitializer<SocketChannel> {
  static Logger logger = LoggerFactory.getLogger(Http2ClientInitializer.class);

  private final SslContext sslCtx;
  private final int maxContentLength;
  private HttpToHttp2ConnectionHandler connectionHandler;
  private Http2ResponseHandler responseHandler;
  private Http2SettingsHandler settingsHandler;

  public Http2ClientInitializer(SslContext sslCtx, int maxContentLength) {
    this.sslCtx = sslCtx;
    this.maxContentLength = maxContentLength;
  }

  @Override
  public void initChannel(SocketChannel ch) throws Exception {
    logger.info("init channel");
    final Http2Connection connection = new DefaultHttp2Connection(false);
    connectionHandler = new HttpToHttp2ConnectionHandlerBuilder()
      .frameListener(new DelegatingDecompressorFrameListener(
        connection,
        new InboundHttp2ToHttpAdapterBuilder(connection)
          .maxContentLength(maxContentLength)
          .propagateSettings(true)
          .build()))
      .connection(connection)
      .build();
    responseHandler = new Http2ResponseHandler();
    settingsHandler = new Http2SettingsHandler(ch.newPromise());
    if (sslCtx != null) {
      configureSsl(ch);
    } else {
      configureClearText(ch);
    }
  }

  public Http2ResponseHandler responseHandler() {
    return responseHandler;
  }

  public Http2SettingsHandler settingsHandler() {
    return settingsHandler;
  }

  protected void configureEndOfPipeline(ChannelPipeline pipeline) {
    pipeline.addLast(settingsHandler, responseHandler);
  }

  /**
   * Configure the pipeline for TLS NPN negotiation to HTTP/2.
   */
  private void configureSsl(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(sslCtx.newHandler(ch.alloc()));
    // We must wait for the handshake to finish and the protocol to be negotiated before configuring
    // the HTTP/2 components of the pipeline.
    pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
      @Override
      protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
          ChannelPipeline p = ctx.pipeline();
          p.addLast(connectionHandler);
          configureEndOfPipeline(p);
          return;
        }
        ctx.close();
        throw new IllegalStateException("unknown protocol: " + protocol);
      }
    });
  }

  /**
   * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.
   */
  private void configureClearText(SocketChannel ch) {
    HttpClientCodec sourceCodec = new HttpClientCodec();
    Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(connectionHandler);
    HttpClientUpgradeHandler upgradeHandler = new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, 65536);

    ch.pipeline().addLast(sourceCodec,
      upgradeHandler,
      new UpgradeRequestHandler(),
      new UserEventLogger());
  }

  /**
   * Class that logs any User Events triggered on this channel.
   */
  private static class UserEventLogger extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      System.out.println("User Event Triggered: " + evt);
      ctx.fireUserEventTriggered(evt);
    }
  }

  /**
   * A handler that triggers the cleartext upgrade to HTTP/2 by sending an initial HTTP request.
   */
  private final class UpgradeRequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      DefaultFullHttpRequest upgradeRequest =
        new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
      ctx.writeAndFlush(upgradeRequest);

      ctx.fireChannelActive();

      // Done with this handler, remove it from the pipeline.
      ctx.pipeline().remove(this);

      configureEndOfPipeline(ctx.pipeline());
    }
  }
}

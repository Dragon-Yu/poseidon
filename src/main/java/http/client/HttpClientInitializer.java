package http.client;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.logging.LogLevel.INFO;

/**
 * Created by johnson on 16/1/11.
 */
public class HttpClientInitializer extends ChannelInitializer<SocketChannel> {
  private static final Http2FrameLogger HTTP_2_FRAME_LOGGER = new Http2FrameLogger(INFO, HttpClientInitializer.class);
  private static final int maxInitialLineLength = 2048;
  private static final int maxChunkSize = 65536;
  static Logger logger = LoggerFactory.getLogger(HttpClientInitializer.class);
  private final SslContext sslCtx;
  private final int maxContentLength;
  private HttpToHttp2ConnectionHandler connectionHandler;
  private HttpResponseHandler responseHandler;

  public HttpClientInitializer(SslContext sslCtx, int maxContentLength) {
    this.sslCtx = sslCtx;
    this.maxContentLength = maxContentLength;
  }

  @Override
  public void initChannel(SocketChannel ch) throws Exception {
    logger.info("init channel");

//    if (sslCtx != null) {
      configureSsl(ch);
//    } else {
//      configureClearText(ch);
//    }
  }

  protected void configureEndOfPipeline(ChannelPipeline pipeline) {
    pipeline.addLast(responseHandler);
  }

  /**
   * Configure the pipeline for TLS NPN negotiation to HTTP/2.
   */
  private void configureSsl(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(sslCtx.newHandler(ch.alloc()));
    pipeline.addLast(new HttpClientCodec(maxInitialLineLength, maxChunkSize, maxChunkSize));
    pipeline.addLast(new HttpClientCodec(maxInitialLineLength, maxChunkSize, maxChunkSize));
    pipeline.addLast(new SimpleChannelInboundHandler<String>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        logger.info(msg);
      }
    });
  }

  /**
   * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.
   */
  private void configureClearText(SocketChannel ch) {
    if (true) {
      throw new RuntimeException("not finished yet");
    }
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

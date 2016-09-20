package poseidon;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SslUtil;

/**
 * Created by Johnson on 16/9/10.
 */
public class ChannelPoolInitializer extends AbstractChannelPoolHandler {
  private static final Logger logger = LoggerFactory.getLogger(ChannelPoolInitializer.class);
  private static final String PROTOCOL_ATTR = "protocol";
  public static final AttributeKey<String> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.newInstance(PROTOCOL_ATTR);
  final Context context;

  public ChannelPoolInitializer(Context context) {
    this.context = context;
  }

  private HttpToHttp2ConnectionHandler createHttpToHttp2ConnectionHandler() {
    Http2Connection http2Connection = new DefaultHttp2Connection(false);
    return new HttpToHttp2ConnectionHandlerBuilder()
      .frameListener(new DelegatingDecompressorFrameListener(http2Connection,
        new InboundHttp2ToHttpAdapterBuilder(http2Connection).maxContentLength(Integer.MAX_VALUE)
          .propagateSettings(true).build())).connection(http2Connection).build();
  }

  @Override
  public void channelCreated(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    // measure the tcp traffic
    pipeline.addLast(TcpTrafficRecorder.getInstance(context).getChannelTrafficShapingHandler(ch));
    if (context.httpsOnly) {
      pipeline.addLast(SslUtil.getSslContextForHttp1().newHandler(ch.alloc()));
    } else {
      pipeline.addLast(SslUtil.getSslContextForHttp2().newHandler(ch.alloc()));
    }
    pipeline.addLast(new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {
      @Override
      protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
//        logger.info("protocol: " + protocol);
        ctx.channel().attr(PROTOCOL_ATTRIBUTE_KEY).set(protocol);
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
          pipeline.addLast(createHttpToHttp2ConnectionHandler());
          pipeline.addLast(new Http2SettingsHandler());
          pipeline.addLast(new Http2InboundHandler());
        } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
          if (!context.httpsOnly) {
            context.setHttp2Unsupported(true);
            Http1ContentRecorder.getInstance(context)
              .logVisitUrl(ctx.channel().attr(ChannelManager.TARGET_URL_KEY).get());
          }
          pipeline.addLast(new HttpClientCodec());
          pipeline.addLast(new HttpContentDecompressor());
          pipeline.addLast(new Http1InboundHandler());
          HandshakeManager.getInstance(context).completeHandshake(ctx.channel());
        } else {
          context.addOtherProtocol(protocol);
          logger.error("unknown protocol " + protocol + " for channel: " + ctx.channel().remoteAddress().toString());
          logger.warn("handle unknown protocol as http/1.1");
//          HandshakeManager.getInstance(context).failHandshake(ch,
//            new IllegalStateException("unknown protocol: " + protocol));
          ctx.channel().attr(PROTOCOL_ATTRIBUTE_KEY).set(ApplicationProtocolNames.HTTP_1_1);
          if (!context.httpsOnly) {
            context.setHttp2Unsupported(true);
            Http1ContentRecorder.getInstance(context)
              .logVisitUrl(ctx.channel().attr(ChannelManager.TARGET_URL_KEY).get());
          }
          pipeline.addLast(new HttpClientCodec());
          pipeline.addLast(new HttpContentDecompressor());
          pipeline.addLast(new Http1InboundHandler());
          HandshakeManager.getInstance(context).completeHandshake(ctx.channel());
        }
      }
    });
  }
}

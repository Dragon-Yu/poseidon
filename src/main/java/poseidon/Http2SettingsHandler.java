package poseidon;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http2SettingsHandler extends SimpleChannelInboundHandler<Http2Settings> {
  private static final Logger logger = LoggerFactory.getLogger(Http2SettingsHandler.class);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Http2Settings msg) throws Exception {
    Context context = ctx.channel().attr(ChannelManager.CONTEXT_ATTRIBUTE_KEY).get();
    // Only care about the first settings message
    ctx.pipeline().remove(this);
    HandshakeManager.getInstance(context).completeHandshake(ctx.channel());
  }
}

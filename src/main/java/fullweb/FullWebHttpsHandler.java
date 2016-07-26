package fullweb;

import https.client.HttpsHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parse.HtmlParser;
import util.RequestUtil;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Johnson on 16/7/23.
 */
public class FullWebHttpsHandler extends HttpsHandler {
  private static Logger logger = LoggerFactory.getLogger(FullWebHttpsHandler.class);
  private Map<Channel, FullWebContext> channelFullWebContextMap = new ConcurrentHashMap<>();
  private TraceController traceController;

  public FullWebHttpsHandler(TraceController traceController) {
    this.traceController = traceController;
  }

  private FullWebContext getContext(ChannelHandlerContext ctx) {
    FullWebContext context = channelFullWebContextMap.putIfAbsent(ctx.channel(), new FullWebContext());
    if (context == null) {
      context = channelFullWebContextMap.get(ctx.channel());
    }
    return context;
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
    FullWebContext context = getContext(ctx);
    if (msg instanceof HttpMessage) {
      HttpMessage message = (HttpMessage) msg;
      context.setHttpMessage(message);
    } else if (msg instanceof HttpContent) {
      HttpContent httpContent = (HttpContent) msg;
      context.addContent(ctx.alloc(), httpContent.content());
      if (msg instanceof LastHttpContent) {
        traceController.completeVisit(ctx.channel(), null);
        ByteBuf content = context.getContent();
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        handleContent(bytes, ctx,
          ContentType.parse(context.getHttpMessage().headers().get(HttpHeaderNames.CONTENT_TYPE)));
        content.clear();
      }
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//    super.channelReadComplete(ctx);
  }


  private void handleContent(byte[] content, ChannelHandlerContext ctx, ContentType contentType) {
    if (!contentType.getMimeType().equals(ContentType.TEXT_HTML.getMimeType())) {
      return;
    }
    String html = new String(content, contentType.getCharset());
    HtmlParser parser = new HtmlParser();
    Set<URL> urlSet = parser.getLinks(html,
      "https://" + ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
    for (URL url : urlSet) {
      if (traceController.urlVisited(url)) {
        continue;
      }
      if (!url.getHost().equals(((InetSocketAddress) ctx.channel().remoteAddress()).getHostName())) {
        logger.warn("Ignore outer resource: " + url.toString());
        continue;
      }
      traceController.visitUrl(url, ctx.channel());
      FullHttpRequest request = RequestUtil.generateHttp2Request(url);
      ctx.channel().writeAndFlush(request);
    }
  }

}

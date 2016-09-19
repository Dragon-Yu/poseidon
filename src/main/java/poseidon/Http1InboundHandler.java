package poseidon;

import config.BaseTestConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parse.HtmlParser;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Johnson on 16/9/10.
 */
public class Http1InboundHandler extends SimpleChannelInboundHandler<HttpObject> {
  private static final Logger logger = LoggerFactory.getLogger(Http1InboundHandler.class);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
    Context context = ctx.channel().attr(ChannelManager.CONTEXT_ATTRIBUTE_KEY).get();
    if (msg instanceof HttpMessage) {
      HttpMessage message = (HttpMessage) msg;
      Http1ContentRecorder.getInstance(context).setHttpMessage(ctx.channel(), message);
    } else if (msg instanceof HttpContent) {
      HttpContent httpContent = (HttpContent) msg;
      Http1ContentRecorder.getInstance(context).appendHttpContent(ctx.channel(), ctx.alloc(), httpContent.content());
      if (msg instanceof LastHttpContent) {
        Http1ContentRecorder.getInstance(context).logCompleteUrl(ctx.channel());
        ByteBuf content = Http1ContentRecorder.getInstance(context).popContent(ctx.channel());
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        ChannelManager.getInstance(context).release(ctx.channel());
        handleContent(bytes, ctx, ContentType.parse(Http1ContentRecorder.getInstance(context)
          .getHttpMessage(ctx.channel()).headers().get(HttpHeaderNames.CONTENT_TYPE)), context);
        Http1ContentRecorder.getInstance(context).updateCompleteStatus();
      }
    }
  }

  private void handleContent(byte[] content, ChannelHandlerContext ctx, ContentType contentType, Context context) {
//    logger.info("content size: " + content.length + " for url: " + ctx.channel().attr(ChannelManager.TARGET_URL_KEY));
    if (!contentType.getMimeType().equals(ContentType.TEXT_HTML.getMimeType())) {
      return;
    }
    Charset charset = contentType.getCharset();
    if (charset == null) {
      charset = Charset.defaultCharset();
    }
    String html = new String(content, charset);
    HtmlParser parser = new HtmlParser();
    String targetHost = ctx.channel().attr(ChannelManager.TARGET_URL_KEY).get().getHost();
    Set<URL> urlSet = parser.getLinks(html, "https://" + targetHost);
    for (URL url : urlSet) {
      if (!BaseTestConfig.IGNORE_OUTER_LINK || url.getHost().equals(targetHost)) {
        try {
          context.client.visit(url, context);
        } catch (InterruptedException | ExecutionException e) {
          logger.error(e.getMessage(), e);
        }
      } else {
//        logger.info("ignore url: " + url);
      }
    }
  }
}

package poseidon;

import config.BaseTestConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.HttpConversionUtil;
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
public class Http2InboundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
  private static final Logger logger = LoggerFactory.getLogger(Http2InboundHandler.class);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
    Context context = ctx.channel().attr(ChannelManager.CONTEXT_ATTRIBUTE_KEY).get();
    Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
    Http2ContentRecorder.getInstance(context).logCompleteUrl(streamId, ctx.channel());
    ByteBuf content = msg.content();
    ContentType contentType = null;
    if (msg.headers().contains(HttpHeaderNames.CONTENT_TYPE)) {
      contentType = ContentType.parse(msg.headers().get(HttpHeaderNames.CONTENT_TYPE));
    }
    if (content.isReadable()) {
      int contentLength = content.readableBytes();
      byte[] arr = new byte[contentLength];
      content.readBytes(arr);
      logger.debug("content size: " + arr.length + " for stream: " + streamId);

      //only parse html content
      if (contentType != null && contentType.getMimeType().equals(ContentType.TEXT_HTML.getMimeType())) {
        Charset charset = contentType.getCharset();
        if (charset == null) {
          charset = Charset.defaultCharset();
        }
        String html = new String(arr, charset);
        HtmlParser parser = new HtmlParser();
        String targetHost = ctx.channel().attr(ChannelManager.TARGET_URL_KEY).get().getHost();
        Set<URL> urlSet = parser.getLinks(html, "https://" + targetHost);
        for (URL url : urlSet) {
          if (!BaseTestConfig.IGNORE_OUTER_LINK || url.getHost().equals(targetHost)) {
            try {
              ctx.channel().attr(ChannelManager.CONTEXT_ATTRIBUTE_KEY).get().client.visit(url, context);
            } catch (InterruptedException | ExecutionException e) {
              logger.error(e.getMessage(), e);
            }
          } else {
//        logger.info("ignore outer link: " + url);
          }
        }
      }
    }
    Http2ContentRecorder.getInstance(context).updateCompleteStatus();
  }
}

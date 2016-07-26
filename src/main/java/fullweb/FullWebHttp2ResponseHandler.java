package fullweb;

import http2.client.Http2ResponseHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parse.HtmlParser;
import util.RequestUtil;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Set;

/**
 * Created by Johnson on 16/7/19.
 */
public class FullWebHttp2ResponseHandler extends Http2ResponseHandler {
  private static Logger logger = LoggerFactory.getLogger(FullWebHttp2ResponseHandler.class);
  private TraceController traceController;

  public FullWebHttp2ResponseHandler(TraceController traceController) {
    this.traceController = traceController;
  }

  @Override
  protected void onResponseReceived(ChannelHandlerContext ctx, URL url) {
    traceController.completeVisit(ctx.channel(), url);
  }

  @Override
  public void handleContent(byte[] content, ChannelHandlerContext ctx, ContentType contentType) {
    // only handle html content
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
      put(RequestUtil.getStreamId(request), url, ctx.channel().writeAndFlush(request), ctx.channel().newPromise());
    }
  }
}

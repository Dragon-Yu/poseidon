package util;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Johnson on 16/7/19.
 */
public class RequestUtil {

  private static AtomicInteger streamId = new AtomicInteger(3);

  private static int getStreamId() {
    return streamId.getAndAdd(2);
  }

  public static FullHttpRequest generateHttp2Request(URL url) {
    FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, url.toString());
    request.headers().add(HttpHeaderNames.HOST, url.getHost());
    request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), HttpScheme.HTTPS);
    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
    request.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), getStreamId());
    return request;
  }

  public static HttpRequest generateHttpsRequest(URL url) {
    HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url.toString());
    request.headers().set(HttpHeaderNames.HOST, url.getHost());
    request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
    return request;
  }

  public static int getStreamId(FullHttpRequest request) {
    return request.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
  }
}

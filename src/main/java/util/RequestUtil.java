package util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Johnson on 16/7/19.
 */
public class RequestUtil {
  private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);
  private static LoadingCache<SimpleUrl, AtomicInteger> streamIdCache =
    CacheBuilder.newBuilder().build(new CacheLoader<SimpleUrl, AtomicInteger>() {
      @Override
      public AtomicInteger load(SimpleUrl key) throws Exception {
        return new AtomicInteger(3);
      }
    });

  private static int getStreamId(URL url) {
    try {
      return streamIdCache.get(new SimpleUrl(url)).getAndAdd(2);
    } catch (ExecutionException e) {
      logger.error(e.getMessage(), e);
      return -1;
    }
  }

  public static FullHttpRequest generateHttp2Request(URL url) {
    FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, url.toString());
    request.headers().add(HttpHeaderNames.HOST, url.getHost());
    request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), HttpScheme.HTTPS);
    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
    request.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), getStreamId(url));
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

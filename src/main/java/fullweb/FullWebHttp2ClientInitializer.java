package fullweb;

import com.google.common.collect.ImmutableSet;
import http2.client.Http2ClientInitializer;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.internal.ConcurrentSet;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Johnson on 16/7/19.
 */
public class FullWebHttp2ClientInitializer extends Http2ClientInitializer implements TraceController {

  private Set<URL> visitedUrls = new ConcurrentSet<>();
  private Map<URL, TraceInfo> tracerInfoMap = new ConcurrentHashMap<>();

  public FullWebHttp2ClientInitializer(SslContext sslCtx, int maxContentLength) {
    super(sslCtx, maxContentLength);
  }

  @Override
  public void initChannel(SocketChannel ch) throws Exception {
    super.initChannel(ch);
    setResponseHandler(new FullWebHttp2ResponseHandler(this));
  }

  public Set<URL> getVisitedUrls() {
    return ImmutableSet.copyOf(visitedUrls);
  }

  public Map<URL, TraceInfo> getTraces() {
    return tracerInfoMap;
  }

  public synchronized boolean urlVisited(URL url) {
    return visitedUrls.contains(url);
  }

  public synchronized void visitUrl(URL url, Channel channel) {
    visitedUrls.add(url);
    TraceInfo info = new TraceInfo(url);
    info.setRequestTimeStamp(System.nanoTime());
    tracerInfoMap.put(url, info);
  }

  @Override
  public void completeVisit(Channel channel, URL url) {
    tracerInfoMap.get(url).setResponseTimeStamp(System.nanoTime());
  }

  @Override
  public void onContentHandled() {
  }
}

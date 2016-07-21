package fullweb;

import com.google.common.collect.ImmutableSet;
import http2.client.Http2ClientInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.internal.ConcurrentSet;

import java.net.URL;
import java.util.Set;

/**
 * Created by Johnson on 16/7/19.
 */
public class FullWebHttp2ClientInitializer extends Http2ClientInitializer
  implements FullWebHttp2ResponseHandler.TraceController {

  private Set<URL> visitedUrls = new ConcurrentSet<>();

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

  public synchronized boolean urlVisited(URL url) {
    return visitedUrls.contains(url);
  }

  public synchronized void visitUrl(URL url) {
    visitedUrls.add(url);
  }
}

package fullweb;

import com.google.common.collect.ImmutableSet;
import https.client.HttpsInitializer;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;

import java.net.URL;
import java.util.Set;

/**
 * Created by Johnson on 16/7/21.
 */
public class FullWebHttpsInitializer extends HttpsInitializer implements TraceController {
  public static final AttributeKey<FullWebTracer> tracerAttributeKey = AttributeKey.valueOf("tracer");
  private Set<URL> visitedUrls = new ConcurrentSet<>();

  public FullWebHttpsInitializer(SslContext sslCtx) {
    super(sslCtx);
  }

  @Override
  public void initChannel(SocketChannel ch) {
    httpsHandler = new FullWebHttpsHandler(this);
    super.initChannel(ch);
  }

  @Override
  public boolean urlVisited(URL url) {
    return visitedUrls.contains(url);
  }

  @Override
  public void completeVisit(Channel channel, URL url) {
    FullWebTracer tracer = getTracer(channel);
    tracer.complete();
  }

  @Override
  public void visitUrl(URL url, Channel channel) {
    visitedUrls.add(url);
    getTracer(channel).start(url);
  }

  public Set<URL> getVisitedUrls() {
    return ImmutableSet.copyOf(visitedUrls);
  }

  public FullWebTracer getTracer(Channel channel) {
    FullWebTracer tracer = channel.attr(tracerAttributeKey).setIfAbsent(new FullWebTracer());
    if (tracer == null) {
      return channel.attr(tracerAttributeKey).get();
    }
    return tracer;
  }
}

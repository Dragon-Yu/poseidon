package fullweb;

import https.client.HttpsClient;
import https.client.HttpsInitializer;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Created by Johnson on 16/7/21.
 */
public class FullWebHttpsClient extends HttpsClient {
  Channel channel;

  @Override
  public void run(URI uri) throws Exception {
    requestTimes = 1;
    super.run(uri);
  }

  @Override
  protected void sendRequests(URL url, Channel channel) {
    this.channel = channel;
    ((FullWebHttpsInitializer) httpsInitializer).visitUrl(url, channel);
    super.sendRequests(url, channel);
  }

  @Override
  protected HttpsInitializer generateHttpsInitializer(SslContext sslContext) {
    return new FullWebHttpsInitializer(sslContext);
  }

  public Set<URL> getVisitedUrls() {
    return ((FullWebHttpsInitializer) httpsInitializer).getVisitedUrls();
  }

  public List<TraceInfo> getTraces() {
    return ((FullWebHttpsInitializer) httpsInitializer).getTracer(channel).getTraces();
  }
}

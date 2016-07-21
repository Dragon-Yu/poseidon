package fullweb;

import http2.client.Http2Client;
import http2.client.Http2ClientInitializer;
import http2.client.Http2ResponseHandler;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;

/**
 * Created by Johnson on 16/7/19.
 */
public class FullWebHttp2Client extends Http2Client {

  @Override
  public Http2ClientInitializer generateInitializer(SslContext sslCtx, int maxContentLength) {
    return new FullWebHttp2ClientInitializer(sslCtx, maxContentLength);
  }

  @Override
  public void sendInitRequests(Http2ResponseHandler responseHandler, Channel channel, URI uri) throws MalformedURLException {
    ((FullWebHttp2ClientInitializer) initializer).visitUrl(uri.toURL());
    super.sendInitRequests(responseHandler, channel, uri);
  }

  public Set<URL> getVisitedUrls() {
    return ((FullWebHttp2ClientInitializer) initializer).getVisitedUrls();
  }
}

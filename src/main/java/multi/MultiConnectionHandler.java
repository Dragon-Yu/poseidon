package multi;

import https.client.HttpsHandler;
import https.client.HttpsInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Johnson on 16/7/31.
 */
public class MultiConnectionHandler extends AbstractChannelPoolHandler {

  private final SslContext sslCtx;
  protected HttpsHandler httpsHandler = new MultiHttpsHandler(ContextManager.getInstance());
  Logger logger = LoggerFactory.getLogger(HttpsInitializer.class);

  public MultiConnectionHandler(SslContext sslCtx) {
    this.sslCtx = sslCtx;
  }

  @Override
  public void channelCreated(Channel ch) throws Exception {
    ChannelPipeline p = ch.pipeline();
    p.addLast(ContextManager.getInstance().createChannelTrafficShapingHandler(ch));
    if (sslCtx != null) {
      p.addLast(sslCtx.newHandler(ch.alloc()));
    }
    p.addLast(new HttpClientCodec());
    p.addLast(new HttpContentDecompressor());
    p.addLast(httpsHandler);
  }
}

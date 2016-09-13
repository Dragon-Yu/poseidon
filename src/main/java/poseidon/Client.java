package poseidon;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestUtil;

import javax.net.ssl.SSLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by Johnson on 16/9/7.
 */
public class Client {
  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  final boolean https_only;

  public Client(boolean https_only) throws SSLException {
    this.https_only = https_only;
  }

  public void visit(URL url, Context context) throws InterruptedException, ExecutionException {
    if (Http1ContentRecorder.getInstance(context).visited(url) ||
      Http2ContentRecorder.getInstance(context).visited(url)) {
      logger.warn("duplicate url visitation: " + url);
      return;
    }
    if (context.httpsOnly) {
      Http1ContentRecorder.getInstance(context).logVisitUrl(url);
      ChannelManager.getInstance(context).getChannel(url, future -> {
        Channel channel = future.get();
        if (!HandshakeManager.getInstance(context).handshakeCompleted(channel)) {
          HandshakeManager.getInstance(context).initHandshakeContext(channel);
          HandshakeManager.getInstance(context).waitHandshake(channel,
            future1 -> channel.writeAndFlush(RequestUtil.generateHttpsRequest(url)));
        } else {
          channel.writeAndFlush(RequestUtil.generateHttpsRequest(url));
        }
      });
    } else {
      FullHttpRequest request = RequestUtil.generateHttp2Request(url);
      Http2ContentRecorder.getInstance(context).logVisitUrl(url, RequestUtil.getStreamId(request));
      Channel channel = ChannelManager.getInstance(context).getChannelAndRelease(url, context);
      if (!HandshakeManager.getInstance(context).handshakeCompleted(channel)) {
        HandshakeManager.getInstance(context).initHandshakeContext(channel);
        HandshakeManager.getInstance(context).waitHandshake(channel,
          future1 -> channel.writeAndFlush(request));
      } else {
        channel.writeAndFlush(request);
      }
    }
  }

  public void await(Context context) throws InterruptedException, ExecutionException {
    Http1ContentRecorder.getInstance(context).waitCompletion();
    Http2ContentRecorder.getInstance(context).waitCompletion();
    ChannelManager.getInstance(context).closeAll();
    ThreadManager.getInstance(context).getWorkingGroup().shutdownGracefully();
  }
}

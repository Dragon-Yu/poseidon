package poseidon;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.ApplicationProtocolNames;
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
      ChannelManager.getInstance(context).getChannel(url, future -> {
        Channel channel = future.get();
        if (!HandshakeManager.getInstance(context).handshakeCompleted(channel)) {
          HandshakeManager.getInstance(context).initHandshakeContext(channel);
          HandshakeManager.getInstance(context).waitHandshake(channel,
            future1 -> sendRequest(url, channel, context));
        } else {
          sendRequest(url, channel, context);
        }
      });
    } else {
      Channel channel = ChannelManager.getInstance(context).getChannelAndRelease(url, context);
      if (!HandshakeManager.getInstance(context).handshakeCompleted(channel)) {
        HandshakeManager.getInstance(context).initHandshakeContext(channel);
        HandshakeManager.getInstance(context).waitHandshake(channel,
          future1 -> sendRequest(url, channel, context));
      } else {
        sendRequest(url, channel, context);
      }
    }

  }

  public void sendRequest(URL url, Channel channel, Context context) {
    String protocol = channel.attr(ChannelPoolInitializer.PROTOCOL_ATTRIBUTE_KEY).get();
    HttpRequest request;
    if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
      Http1ContentRecorder.getInstance(context).logVisitUrl(url);
      request = RequestUtil.generateHttpsRequest(url);
    } else if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
      request = RequestUtil.generateHttp2Request(url);
      Http2ContentRecorder.getInstance(context).logVisitUrl(url, RequestUtil.getStreamId((FullHttpRequest) request));
    } else {
      logger.error("illegal protocol: " + protocol);
//      throw new IllegalArgumentException("illegal protocol: " + protocol);
      Http1ContentRecorder.getInstance(context).logVisitUrl(url);
      request = RequestUtil.generateHttpsRequest(url);
    }
    channel.writeAndFlush(request);
  }

  public void await(Context context) throws InterruptedException, ExecutionException {
    HandshakeManager.getInstance(context).waitHandshake();
    Http1ContentRecorder.getInstance(context).waitCompletion();
    Http2ContentRecorder.getInstance(context).waitCompletion();
//    Http1ContentRecorder.getInstance(context).waitCompletion();
    ChannelManager.getInstance(context).closeAll();
    ThreadManager.getInstance(context).getWorkingGroup().shutdownGracefully();
  }
}

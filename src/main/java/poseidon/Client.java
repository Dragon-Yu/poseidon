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
//      logger.debug("duplicate url visitation: " + url);
      return;
    }
    if (ChannelManager.getInstance(context).isBlocked(url)) {
      logger.debug("ignore blocked url: " + url);
      return;
    }
    if (url.getDefaultPort() == 80) {
      logger.debug("ignore http request: " + url);
      return;
    }
    if (context.httpsOnly) {
      Http1ContentRecorder.getInstance(context).logVisitUrl(url);
      ChannelManager.getInstance(context).getChannel(url, future -> {
        if (future.isSuccess()) {
          Channel channel = future.get();
          HandshakeManager.getInstance(context).waitHandshake(channel, future1 -> {
            if (future1.isSuccess()) {
              sendHttp1Request(channel, url, context);
            } else {
              logger.warn("handshake failed for channel: " + channel + ", url: " + url);
              Http1ContentRecorder.getInstance(context).clearTraceThenUpdateStatus(url);
            }
          });
        } else {
          logger.error("get channel failed for url: " + url);
          Http1ContentRecorder.getInstance(context).clearTraceThenUpdateStatus(url);
        }
      });
    } else {
      Http2ContentRecorder.getInstance(context).logPreVisitUrl(url);
      ChannelManager.getInstance(context).getChannelAndRelease(url, future -> {
        if (future.isSuccess()) {
          Channel channel = future.get();
          if (channel != null) {
            HandshakeManager.getInstance(context).waitHandshake(channel, future1 -> {
              if (future1.isSuccess()) {
                sendRequest(url, channel, context);
              } else {
                logger.warn("handshake failed for channel: " + channel + ", url: " + url);
                Http2ContentRecorder.getInstance(context).clearTraceThenUpdateStatus(url);
              }
            });
          } else {
            logger.error("get channel failed for url: " + url);
            Http2ContentRecorder.getInstance(context).clearTraceThenUpdateStatus(url);
          }
        } else {
          logger.error("get channel failed for url: " + url);
          Http2ContentRecorder.getInstance(context).clearTraceThenUpdateStatus(url);
        }
      });
    }
  }

  public void sendRequest(URL url, Channel channel, Context context) {
    String protocol = channel.attr(ChannelPoolInitializer.PROTOCOL_ATTRIBUTE_KEY).get();
    if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
      sendHttp1Request(url, context);
    } else if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
      sendHttp2Request(channel, url, context);
    } else {
//      throw new IllegalArgumentException("illegal protocol: " + protocol);
      logger.error("illegal protocol: " + protocol + " for channel: " + channel);
      sendHttp1Request(url, context);
    }
  }

  private void sendHttp1Request(Channel channel, URL url, Context context) {
    logger.debug("send http1 request to channel: " + channel + ", url: " + url);
    Http1ContentRecorder.getInstance(context).logVisitUrl(url);
    Http2ContentRecorder.getInstance(context).clearTraceThenUpdateStatus(url);
    HttpRequest request = RequestUtil.generateHttpsRequest(url);
    channel.writeAndFlush(request);
  }

  /**
   * send http/1.x request when the server do not supports http/2
   */
  private void sendHttp1Request(URL url, Context context) {
    ChannelManager.getInstance(context).getChannel(url, future -> {
      Channel channel = future.get();
      HandshakeManager.getInstance(context).waitHandshake(channel, f -> {
        if (f.isSuccess()) {
          sendHttp1Request(channel, url, context);
        } else {
          logger.warn("handshake failed for channel: " + channel + ", with url: " + url);
          Http1ContentRecorder.getInstance(context).clearTraceThenUpdateStatus(url);
        }
      });
    });
  }

  private void sendHttp2Request(Channel channel, URL url, Context context) {
    logger.debug("send http2 request to channel: " + channel + ", url: " + url);
    FullHttpRequest request = RequestUtil.generateHttp2Request(url);
    Http2ContentRecorder.getInstance(context).logVisitUrl(channel, url, RequestUtil.getStreamId(request));
    channel.writeAndFlush(request);
  }

  public void await(Context context) throws InterruptedException, ExecutionException {
//    HandshakeManager.getInstance(context).waitHandshake();
    Http2ContentRecorder.getInstance(context).waitCompletion();
    Http1ContentRecorder.getInstance(context).waitCompletion();
    Http2ContentRecorder.getInstance(context).waitCompletion();
    ChannelManager.getInstance(context).closeAll();
    ThreadManager.getInstance(context).getWorkingGroup().shutdownGracefully();
  }
}

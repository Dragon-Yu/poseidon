package util;

import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;
import io.netty.handler.codec.http.HttpRequest;
import multi.ContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Created by Johnson on 16/7/31.
 */
public class ChannelPoolUtil {
  private static final ContextManager contextManager = ContextManager.getInstance();
  static ChannelPool channelPool;
  private static Logger logger = LoggerFactory.getLogger(ChannelPoolUtil.class);

  public static void setChannelPool(ChannelPool channelPool) {
    ChannelPoolUtil.channelPool = channelPool;
  }

  public static void sendRequest(ChannelPool channelPool, URL url) {
    HttpRequest request = RequestUtil.generateHttpsRequest(url);
    channelPool.acquire().addListener(future -> {
      Channel channel = (Channel) future.get();
      contextManager.visitUrl(url, channel);
      channel.writeAndFlush(request);
    });
  }

  public static ChannelPool getChannelPool(Channel channel) {
    return channelPool;
  }
}

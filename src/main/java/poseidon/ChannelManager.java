package poseidon;

import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SimpleUrl;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Johnson on 16/9/10.
 */
public class ChannelManager {
  private static final String CONTEXT_ATTR = "context";
  public static final AttributeKey<Context> CONTEXT_ATTRIBUTE_KEY = AttributeKey.newInstance(CONTEXT_ATTR);
  private static final String TARGET_URL_ATTR = "target url";
  public static final AttributeKey<URL> TARGET_URL_KEY = AttributeKey.newInstance(TARGET_URL_ATTR);
  private static final String CHANNEL_POOL_ATTR = "channel pool";
  public static final AttributeKey<ChannelPool> CHANNEL_POOL_ATTRIBUTE_KEY = AttributeKey.newInstance(CHANNEL_POOL_ATTR);
  private static final Logger logger = LoggerFactory.getLogger(ChannelManager.class);
  private static ChannelManager ourInstance;
  private Map<SimpleUrl, Channel> singleChannelMap = new ConcurrentHashMapV8<>();
  private Context context = null;

  private ChannelManager(Context context) {
    this.context = context;
  }

  public static synchronized ChannelManager getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new ChannelManager(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new ChannelManager(context);
    }
    return ourInstance;
  }

  public Context getContext() {
    return context;
  }

  private void initChannelContext(Channel channel, ChannelPool channelPool, Context context, URL url) {
    if (!channel.hasAttr(CONTEXT_ATTRIBUTE_KEY)) {
      channel.attr(CONTEXT_ATTRIBUTE_KEY).set(context);
    }
    if (!channel.hasAttr(CHANNEL_POOL_ATTRIBUTE_KEY)) {
      channel.attr(CHANNEL_POOL_ATTRIBUTE_KEY).set(channelPool);
    }
    channel.attr(TARGET_URL_KEY).set(url);
    AddressManager.getInstance(context).setLocalIp((InetSocketAddress) channel.localAddress());
  }

  public void getChannel(URL url, GenericFutureListener<Future<Channel>> listener) {
    try {
      ChannelPool channelPool = ChannelPoolManager.getInstance(context).getChannelPool(url);
      channelPool.acquire()
        .addListeners(future -> initChannelContext((Channel) future.get(), channelPool, context, url), listener);
    } catch (ExecutionException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public synchronized Channel getChannelAndRelease(URL url, Context context)
    throws InterruptedException, ExecutionException {
    if (singleChannelMap.containsKey(new SimpleUrl(url))) {
      return singleChannelMap.get(new SimpleUrl(url));
    }
    ChannelPool channelPool = ChannelPoolManager.getInstance(context).getChannelPool(url);
    Channel channel = channelPool.acquire().get();
    channelPool.release(channel);
    singleChannelMap.put(new SimpleUrl(url), channel);
    initChannelContext(channel, channelPool, context, url);
    return channel;
  }

  public Channel getChannel(URL url, Context context) throws InterruptedException, ExecutionException {
    ChannelPool channelPool = ChannelPoolManager.getInstance(context).getChannelPool(url);
    Channel channel = channelPool.acquire().get();
    initChannelContext(channel, channelPool, context, url);
    return channel;
  }

  public void release(Channel channel) {
    ChannelPool channelPool = channel.attr(CHANNEL_POOL_ATTRIBUTE_KEY).get();
    channelPool.release(channel);
  }

  public void closeAll() {
    ChannelPoolManager.getInstance(context).closeAll();
  }
}

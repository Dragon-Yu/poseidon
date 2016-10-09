package poseidon;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.ConcurrentSet;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SimpleUrl;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.Set;
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
  Set<SimpleUrl> blockedHost = new ConcurrentSet<>();
  private Context context = null;
  private Map<SimpleUrl, SettableFuture<Channel>> getAndReleaseFutureMap = new ConcurrentHashMapV8<>();

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
    if (!channel.hasAttr(CONTEXT_ATTRIBUTE_KEY) || channel.attr(CONTEXT_ATTRIBUTE_KEY).get() == null) {
      channel.attr(CONTEXT_ATTRIBUTE_KEY).set(context);
    }
    if (!channel.hasAttr(CHANNEL_POOL_ATTRIBUTE_KEY) || channel.attr(CHANNEL_POOL_ATTRIBUTE_KEY).get() == null) {
      channel.attr(CHANNEL_POOL_ATTRIBUTE_KEY).set(channelPool);
    }
    channel.attr(TARGET_URL_KEY).set(url);
    AddressManager.getInstance(context).setLocalIp((InetSocketAddress) channel.localAddress());
  }

  public void addBlockedHost(SimpleUrl simpleUrl) {
    logger.info("add blocked host: " + simpleUrl);
    blockedHost.add(simpleUrl);
  }

  public Set<SimpleUrl> getBlockedHost() {
    return blockedHost;
  }

  public boolean isBlocked(URL url) {
    return blockedHost.contains(new SimpleUrl(url));
  }

  public void getChannel(URL url, GenericFutureListener<Future<Channel>> listener) {
    try {
      ChannelPool channelPool = ChannelPoolManager.getInstance(context).getChannelPool(url);
      channelPool.acquire().addListeners(future -> {
        if (future.isSuccess()) {
          initChannelContext((Channel) future.get(), channelPool, context, url);
        }
      }, listener);
    } catch (ExecutionException e) {
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * get channel and release as an atomic operation, this func ensures that there is always only one connection
   * created no matter the size of the channel pool.
   */
  public void getChannelAndRelease(URL url, GenericFutureListener<Future<Channel>> listener) {
    SimpleUrl simpleUrl = new SimpleUrl(url);
    try {
      SettableFuture<Channel> newFuture = SettableFuture.create();
      getAndReleaseFutureMap.putIfAbsent(simpleUrl, newFuture);
      if (getAndReleaseFutureMap.get(simpleUrl) != newFuture) {
        // the future already exists
        SettableFuture<Channel> future = getAndReleaseFutureMap.get(simpleUrl);
        Channel channel = future.get();
        listener.operationComplete(ThreadManager.getInstance(context).getEventExecutor().newSucceededFuture(channel));
      } else {
        // it's the first time to get the channel
        getAndReleaseFutureMap.put(simpleUrl, SettableFuture.create());
        ChannelPool channelPool = ChannelPoolManager.getInstance(context).getChannelPool(simpleUrl);
        channelPool.acquire().addListeners(future -> {
          if (future.isSuccess()) {
            Channel channel = (Channel) future.get();
            initChannelContext(channel, channelPool, context, url);
            channelPool.release(channel);
            getAndReleaseFutureMap.get(simpleUrl).set(channel);
          } else {
            getAndReleaseFutureMap.get(simpleUrl).set(null);
          }
        }, listener);
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  public synchronized Channel getChannelAndRelease(URL url, Context context)
    throws InterruptedException, ExecutionException {
    ChannelPool channelPool = ChannelPoolManager.getInstance(context).getChannelPool(url);
    Channel channel = channelPool.acquire().get();
    channelPool.release(channel);
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
    logger.debug("release channel: " + channel);
    ChannelPool channelPool = channel.attr(CHANNEL_POOL_ATTRIBUTE_KEY).get();
    channelPool.release(channel);
  }

  public void closeAll() {
    logger.info("close all");
    ChannelPoolManager.getInstance(context).closeAll();
  }
}

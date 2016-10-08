package poseidon;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import config.BaseTestConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SimpleUrl;

import java.net.URL;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Johnson on 16/9/6.
 */
public class ChannelPoolManager {
  private static final Logger logger = LoggerFactory.getLogger(ChannelPoolManager.class);
  private static ChannelPoolManager ourInstance;
  private LoadingCache<SimpleUrl, ChannelPool> channelPoolCache;
  private Set<SimpleUrl> targetSet = new ConcurrentSet<>();
  private Context context;

  private ChannelPoolManager(Context context) {
    this.context = context;
    channelPoolCache = CacheBuilder.newBuilder().build(new CacheLoader<SimpleUrl, ChannelPool>() {
      @Override
      public ChannelPool load(SimpleUrl key) throws Exception {
        logger.info("create new connection to " + key.toString());
        targetSet.add(key);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        bootstrap.group(ThreadManager.getInstance(context).getWorkingGroup())
          .channel(NioSocketChannel.class)
          .remoteAddress(key.getAddr(), key.getPort());
//        int channelPoolSize =
//          context.httpsOnly ? BaseTestConfig.CHANNEL_POOL_SIZE : BaseTestConfig.HTTP2_CHANNEL_POOL_SIZE;
        int channelPoolSize = BaseTestConfig.CHANNEL_POOL_SIZE;
        ChannelPoolInitializer channelPoolHandler = new ChannelPoolInitializer(context);
        ChannelPool channelPool = new FixedChannelPool(bootstrap, channelPoolHandler, channelPoolSize);
        channelPoolHandler.setChannelPool(channelPool);
        return channelPool;
      }
    });
  }

  public static ChannelPoolManager getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new ChannelPoolManager(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new ChannelPoolManager(context);
    }
    return ourInstance;
  }

  public Set<SimpleUrl> getTargetSet() {
    return targetSet;
  }

  public ChannelPool getChannelPool(SimpleUrl simpleUrl) throws ExecutionException {
    return channelPoolCache.get(simpleUrl);
  }

  public ChannelPool getChannelPool(URL url) throws ExecutionException {
    return getChannelPool(new SimpleUrl(url));
  }

  public void closeAll() {
    for (ChannelPool channelPool : channelPoolCache.asMap().values()) {
      channelPool.close();
    }
  }
}

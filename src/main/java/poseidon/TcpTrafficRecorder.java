package poseidon;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import entity.TcpTrafficSize;
import io.netty.channel.Channel;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Johnson on 16/9/12.
 */
public class TcpTrafficRecorder {
  private static final Logger logger = LoggerFactory.getLogger(TcpTrafficRecorder.class);
  private static TcpTrafficRecorder ourInstance;
  private Context context = null;
  private LoadingCache<Channel, ChannelTrafficShapingHandler> channelTrafficShapingHandlerCache =
    CacheBuilder.newBuilder().build(new CacheLoader<Channel, ChannelTrafficShapingHandler>() {
      @Override
      public ChannelTrafficShapingHandler load(Channel key) throws Exception {
        return new ChannelTrafficShapingHandler(10);
      }
    });

  private TcpTrafficRecorder(Context context) {
    this.context = context;
  }

  public static synchronized TcpTrafficRecorder getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new TcpTrafficRecorder(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new TcpTrafficRecorder(context);
    }
    return ourInstance;
  }

  public ChannelTrafficShapingHandler getChannelTrafficShapingHandler(Channel channel) {
    try {
      return channelTrafficShapingHandlerCache.get(channel);
    } catch (ExecutionException e) {
      logger.error(e.getMessage(), e);
    }
    return new ChannelTrafficShapingHandler(10);
  }

  public TcpTrafficSize getTcpTrafficSize() {
    Map<String, Long> requestSizeMap = new HashMap<>();
    Map<String, Long> responseSizeMap = new HashMap<>();
    Map<Channel, ChannelTrafficShapingHandler> handlerMap = channelTrafficShapingHandlerCache.asMap();
    for (Channel channel : handlerMap.keySet()) {
      ChannelTrafficShapingHandler handler = handlerMap.get(channel);
      requestSizeMap.put(channel.id().asShortText(), handler.trafficCounter().cumulativeWrittenBytes());
      responseSizeMap.put(channel.id().asShortText(), handler.trafficCounter().cumulativeReadBytes());
    }
    return new TcpTrafficSize(requestSizeMap, responseSizeMap);
  }
}

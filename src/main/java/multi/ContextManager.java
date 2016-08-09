package multi;

import com.google.common.util.concurrent.SettableFuture;
import fullweb.TraceController;
import fullweb.TraceInfo;
import io.netty.channel.Channel;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ChannelPoolUtil;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Johnson on 16/8/7.
 * This context manager is based on the principle that for each connection, response order follow the order of request.
 * But HTTP/2 does not follow this rule, so it is only suitable for HTTP/1.x
 */
public class ContextManager implements TraceController {
  private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);
  private static ContextManager ourInstance = new ContextManager();
  private final Map<Channel, Queue<URL>> channelWaitingQueue = new ConcurrentHashMap<>();
  private final Map<URL, TraceInfo> traces = new ConcurrentHashMap<>();
  private final Set<URL> pendingUrl = new ConcurrentSet<>();
  private SettableFuture<Map<URL, TraceInfo>> future = SettableFuture.create();
  private Map<Channel, ChannelTrafficShapingHandler> channelTrafficShapingHandlerMap = new ConcurrentHashMap<>();


  private ContextManager() {
  }

  public static ContextManager getInstance() {
    return ourInstance;
  }

  public ChannelTrafficShapingHandler createChannelTrafficShapingHandler(Channel channel) {
    ChannelTrafficShapingHandler handler = new ChannelTrafficShapingHandler(10);
    channelTrafficShapingHandlerMap.put(channel, handler);
    return handler;
  }

  @Override
  public boolean urlVisited(URL url) {
    return traces.containsKey(url);
  }

  @Override
  public synchronized void visitUrl(URL url, Channel channel) {
    if (channel == null) {
      pendingUrl.add(url);
      return;
    }
//    logger.info("start: " + url + ", channel: " + channel.id());
    traces.put(url, new TraceInfo(url, System.nanoTime(), channel.id().asShortText()));
    synchronized (channelWaitingQueue) {
      Queue<URL> queue = channelWaitingQueue.get(channel);
      if (queue == null) {
        queue = new LinkedList<>();
      }
      queue.add(url);
      channelWaitingQueue.put(channel, queue);
    }
  }

  /**
   * @param channel channel that current response belows to
   * @param url     ignored, since url can be gotten from {@link ContextManager#channelWaitingQueue}
   */
  @Override
  public synchronized void completeVisit(Channel channel, URL url) {
    synchronized (channelWaitingQueue) {
      Queue<URL> queue = channelWaitingQueue.get(channel);
      url = queue.poll();
      if (queue.isEmpty()) {
        channelWaitingQueue.remove(channel);
      }
    }
    pendingUrl.remove(url);
    traces.get(url).setResponseTimeStamp(System.nanoTime());
//    logger.info("complete: " + url + ", channel: " + channel.id());
    ChannelPoolUtil.getChannelPool(channel).release(channel);
  }

  public Map<URL, TraceInfo> getTraces(int timeout) throws InterruptedException, ExecutionException, TimeoutException {
    return future.get(timeout, TimeUnit.SECONDS);
  }

  public Map<Channel, Long> getRequestSize() {
    Map<Channel, Long> res = new HashMap<>();
    for (Channel channel : channelTrafficShapingHandlerMap.keySet()) {
      res.put(channel, channelTrafficShapingHandlerMap.get(channel).trafficCounter().cumulativeWrittenBytes());
    }
    return res;
  }

  public Map<Channel, Long> getResponseSize() {
    Map<Channel, Long> res = new HashMap<>();
    for (Channel channel : channelTrafficShapingHandlerMap.keySet()) {
      res.put(channel, channelTrafficShapingHandlerMap.get(channel).trafficCounter().cumulativeReadBytes());
    }
    return res;
  }


  @Override
  public void onContentHandled() {
    if (pendingUrl.isEmpty()) {
      future.set(traces);
    }
  }
}

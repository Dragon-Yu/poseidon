package poseidon;

import com.google.common.util.concurrent.SettableFuture;
import fullweb.TraceInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.util.internal.ConcurrentSet;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Johnson on 16/9/10.
 */
public class Http1ContentRecorder {
  private static final Logger logger = LoggerFactory.getLogger(Http1ContentRecorder.class);
  private static final ByteToMessageDecoder.Cumulator cumulator = ByteToMessageDecoder.MERGE_CUMULATOR;
  private static Http1ContentRecorder ourInstance;
  private final SettableFuture<Void> completionFuture = SettableFuture.create();
  private Context context = null;
  private Map<Channel, HttpMessage> httpMessageMap = new ConcurrentHashMapV8<>();
  private Map<Channel, ByteBuf> contentMap = new ConcurrentHashMapV8<>();
  private Set<URL> urlOnTheAir = new ConcurrentSet<>();
  private Map<URL, TraceInfo> traceInfoMap = new ConcurrentHashMapV8<>();

  private Http1ContentRecorder(Context context) {
    this.context = context;
  }

  public static synchronized Http1ContentRecorder getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new Http1ContentRecorder(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new Http1ContentRecorder(context);
    }
    return ourInstance;
  }

  public List<TraceInfo> getTraceInfoList() {
    return new ArrayList<>(traceInfoMap.values());
  }

  public Map<URL, TraceInfo> getTraceInfoMap() {
    return traceInfoMap;
  }

  public void setHttpMessage(Channel channel, HttpMessage httpMessage) {
    httpMessageMap.put(channel, httpMessage);
  }

  public void appendHttpContent(Channel channel, ByteBufAllocator byteBufAllocator, ByteBuf byteBuf) {
    ByteBuf content = contentMap.get(channel);
    if (content == null) {
      content = byteBuf.retain();
    } else {
      content = cumulator.cumulate(byteBufAllocator, content, byteBuf.retain());
    }
    contentMap.put(channel, content);
  }

  public HttpMessage getHttpMessage(Channel channel) {
    return httpMessageMap.get(channel);
  }

  public ByteBuf popContent(Channel channel) {
    ByteBuf res = contentMap.get(channel);
    contentMap.remove(channel);
    return res;
  }

  public void logVisitUrl(URL url) {
    logger.debug("visit url: " + url);
    urlOnTheAir.add(url);
    traceInfoMap.put(url, new TraceInfo(url, ApplicationProtocolNames.HTTP_1_1));
  }

  public void clearTrace(URL url) {
    urlOnTheAir.remove(url);
    traceInfoMap.remove(url);
  }

  public void clearTraceThenUpdateStatus(URL url) {
    clearTrace(url);
    updateCompleteStatus();
  }

  public void logCompleteUrl(Channel channel) {
    URL url = channel.attr(ChannelManager.TARGET_URL_KEY).get();
    logger.debug("complete channel: " + channel + ", url: " + url);
    urlOnTheAir.remove(url);
    traceInfoMap.get(url).finish(channel.id().asShortText(), System.nanoTime());
  }

  public boolean visited(URL url) {
    return traceInfoMap.containsKey(url) || urlOnTheAir.contains(url);
  }

  public void updateCompleteStatus() {
//    logger.info("update complete status");
    logger.debug(urlOnTheAir.toString());
    if (urlOnTheAir.isEmpty()) {
      completionFuture.set(null);
    }
  }

  public void waitCompletion() throws InterruptedException, ExecutionException {
    // if there is no http/1.x traffic, just return
    if (!traceInfoMap.isEmpty()) {
      logger.info("wait http1 to complete");
      completionFuture.get();
    }
  }
}

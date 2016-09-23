package poseidon;

import com.google.common.util.concurrent.SettableFuture;
import fullweb.TraceInfo;
import io.netty.channel.Channel;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Johnson on 16/9/10.
 */
public class Http2ContentRecorder {
  private static final Logger logger = LoggerFactory.getLogger(Http2ContentRecorder.class);
  private static Http2ContentRecorder ourInstance;
  SettableFuture<Void> completeFuture = SettableFuture.create();
  Map<Integer, URL> urlOnTheAir = new ConcurrentHashMapV8<>();
  Map<URL, TraceInfo> traceInfoMap = new ConcurrentHashMapV8<>();
  private Context context = null;

  private Http2ContentRecorder(Context context) {
    this.context = context;
  }

  public static synchronized Http2ContentRecorder getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new Http2ContentRecorder(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new Http2ContentRecorder(context);
    }
    return ourInstance;
  }

  public void logVisitUrl(URL url, int streamId) {
//    logger.info("visit url: " + url + " on stream: " + streamId);
    urlOnTheAir.put(streamId, url);
    traceInfoMap.put(url, new TraceInfo(url));
  }

  public void logCompleteUrl(int streamId, Channel channel) {
//    logger.info("complete stream: " + streamId);
    URL url = urlOnTheAir.get(streamId);
    urlOnTheAir.remove(streamId);
    traceInfoMap.get(url).finish(channel.id().asShortText(), System.nanoTime());
  }

  public void waitCompletion() throws InterruptedException, ExecutionException {
    if (!traceInfoMap.isEmpty()) {
      logger.info("wait http2 to complete");
      completeFuture.get();
    }
  }

  public List<TraceInfo> getTraceInfoList() {
    return new ArrayList<>(traceInfoMap.values());
  }

  public Map<URL, TraceInfo> getTraceInfoMap() {
    return traceInfoMap;
  }

  public boolean visited(URL url) {
    return traceInfoMap.containsKey(url);
  }

  public void updateCompleteStatus() {
    if (urlOnTheAir.isEmpty()) {
      completeFuture.set(null);
    }
  }
}

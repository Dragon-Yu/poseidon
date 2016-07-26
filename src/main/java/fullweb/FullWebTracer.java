package fullweb;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Johnson on 16/7/23.
 */
public class FullWebTracer {

  private Queue<URL> urlWaitingQueue = new LinkedList<>();
  private List<URL> completedUrl = new LinkedList<>();
  private Map<URL, TraceInfo> trace = new ConcurrentHashMap<>();


  public synchronized void start(URL url) {
    urlWaitingQueue.add(url);
    TraceInfo info = new TraceInfo(url);
    info.setRequestTimeStamp(System.nanoTime());
    trace.put(url, info);
  }

  public synchronized void complete() {
    URL url = urlWaitingQueue.poll();
    completedUrl.add(url);
    TraceInfo info = trace.get(url);
    info.setResponseTimeStamp(System.nanoTime());
  }

  public List<TraceInfo> getTraces() {
    return new LinkedList<>(trace.values());
  }

}

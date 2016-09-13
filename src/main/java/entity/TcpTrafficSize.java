package entity;

import java.util.Map;

/**
 * Created by Johnson on 16/9/13.
 */
public class TcpTrafficSize {

  private Map<String, Long> requestTcpTrafficSizeMap;
  private Map<String, Long> responseTcpTrafficSizeMap;
  private Long requestTcpTrafficSizeAll;
  private Long responseTcpTrafficSizeAll;

  public TcpTrafficSize(Map<String, Long> requestTcpTrafficSizeMap, Map<String, Long> responseTcpTrafficSizeMap) {
    this.requestTcpTrafficSizeMap = requestTcpTrafficSizeMap;
    this.responseTcpTrafficSizeMap = responseTcpTrafficSizeMap;
    this.requestTcpTrafficSizeAll = requestTcpTrafficSizeMap.values().stream().mapToLong(Long::longValue).sum();
    this.responseTcpTrafficSizeAll = responseTcpTrafficSizeMap.values().stream().mapToLong(Long::longValue).sum();
  }
}

package entity;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import util.SimpleUrl;

import java.util.Map;

/**
 * Created by Johnson on 16/9/13.
 */
public class MeasuredTrafficSize {
  long responseTcpSizeAll;
  long responseIpSizeAll;
  long requestTcpSizeAll;
  long requestIpSizeAll;
  private Map<SimpleUrl, Long> responseTcpSizeMap = new ConcurrentHashMapV8<>();
  private Map<SimpleUrl, Long> responseIpSizeMap = new ConcurrentHashMapV8<>();
  private Map<SimpleUrl, Long> requestTcpSizeMap = new ConcurrentHashMapV8<>();
  private Map<SimpleUrl, Long> requestIpSizeMap = new ConcurrentHashMapV8<>();

  public MeasuredTrafficSize(Map<SimpleUrl, Long> responseTcpSizeMap, Map<SimpleUrl, Long> responseIpSizeMap,
                             Map<SimpleUrl, Long> requestTcpSizeMap, Map<SimpleUrl, Long> requestIpSizeMap) {
    this.responseTcpSizeMap = responseTcpSizeMap;
    this.responseIpSizeMap = responseIpSizeMap;
    this.requestTcpSizeMap = requestTcpSizeMap;
    this.requestIpSizeMap = requestIpSizeMap;
    this.responseTcpSizeAll = responseTcpSizeMap.values().stream().mapToLong(Long::longValue).sum();
    this.responseIpSizeAll = responseIpSizeMap.values().stream().mapToLong(Long::longValue).sum();
    this.requestTcpSizeAll = requestTcpSizeMap.values().stream().mapToLong(Long::longValue).sum();
    this.requestIpSizeAll = requestIpSizeMap.values().stream().mapToLong(Long::longValue).sum();
  }
}

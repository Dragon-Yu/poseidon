package entity;

import fullweb.TraceInfo;

import java.util.Collection;

/**
 * Created by Johnson on 16/9/13.
 */
public class ExperimentData {
  private Collection<TraceInfo> traceInfoList;
  private TcpTrafficSize tcpTrafficSize;
  private TcpdumpInfo tcpdumpInfo;
  private long timeAll;

  public ExperimentData(Collection<TraceInfo> traceInfoList, TcpTrafficSize tcpTrafficSize, TcpdumpInfo tcpdumpInfo,
                        long timeAll) {
    this.traceInfoList = traceInfoList;
    this.tcpTrafficSize = tcpTrafficSize;
    this.tcpdumpInfo = tcpdumpInfo;
    this.timeAll = timeAll;
  }
}

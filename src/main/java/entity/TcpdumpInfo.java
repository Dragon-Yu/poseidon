package entity;

/**
 * Created by Johnson on 16/9/13.
 */
public class TcpdumpInfo {

  final MeasuredTrafficSize measuredTrafficSize;
  final int tcpdumpPacketsDrop;

  public TcpdumpInfo(MeasuredTrafficSize measuredTrafficSize, int tcpdumpPacketsDrop) {
    this.measuredTrafficSize = measuredTrafficSize;
    this.tcpdumpPacketsDrop = tcpdumpPacketsDrop;
  }

  public MeasuredTrafficSize getMeasuredTrafficSize() {
    return measuredTrafficSize;
  }

  public int getTcpdumpPacketsDrop() {
    return tcpdumpPacketsDrop;
  }
}

package entity;

/**
 * Traffic size, including input and output
 * Created by Johnson on 16/6/19.
 */
public class TcpdumpTrafficSize {

  private final long ipInput;
  private final long ipOutput;
  private final long tcpInput;
  private final long tcpOutput;

  public TcpdumpTrafficSize(long ipInput, long ipOutput, long tcpInput, long tcpOutput) {
    this.ipInput = ipInput;
    this.ipOutput = ipOutput;
    this.tcpInput = tcpInput;
    this.tcpOutput = tcpOutput;
  }


  public long getInput() {
    return ipInput;
  }

  public long getOutput() {
    return ipOutput;
  }

  public long getIpInput() {
    return ipInput;
  }

  public long getIpOutput() {
    return ipOutput;
  }

  public long getTcpInput() {
    return tcpInput;
  }

  public long getTcpOutput() {
    return tcpOutput;
  }

  @Override
  public String toString() {
    return "TcpdumpTrafficSize{" +
      "ipInput=" + ipInput +
      ", ipOutput=" + ipOutput +
      ", tcpInput=" + tcpInput +
      ", tcpOutput=" + tcpOutput +
      '}';
  }
}

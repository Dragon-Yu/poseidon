package poseidon;

import config.BaseTestConfig;
import entity.MeasuredTrafficSize;
import entity.TcpdumpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ShellUtil;
import util.SimpleUrl;
import util.StringParseUtil;

import java.io.IOException;
import java.util.Set;

/**
 * Created by Johnson on 16/9/13.
 */
public class TcpdumpManager {

  private static final int PROCESS_WAITING_TIME = 5000;
  private static final Logger logger = LoggerFactory.getLogger(TcpdumpManager.class);
  private static TcpdumpManager ourInstance;
  Process process = createTcpdumpProcess();
  private Context context;
  private ShellUtil shellUtil = new ShellUtil();

  private TcpdumpManager(Context context) {
    this.context = context;
  }

  public static synchronized TcpdumpManager getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new TcpdumpManager(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new TcpdumpManager(context);
    }
    return ourInstance;
  }

  private Process createTcpdumpProcess() {
    String tcpdumpCmd = BaseTestConfig.TCPDUMP_CMD;
    logger.info("tcpdump cmd: " + tcpdumpCmd);
    try {
      return Runtime.getRuntime().exec(tcpdumpCmd);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  public void startMonitoring() throws InterruptedException {
    shellUtil.startReadingFromProcess(process);
    Thread.sleep(PROCESS_WAITING_TIME);
  }

  public TcpdumpInfo stopMonitoring(Set<SimpleUrl> targetUrls) {
    String tcpdumpOutput = shellUtil.getProcessOutputThenInterrupt(PROCESS_WAITING_TIME, process, "tcpdump");
    logger.info("tcpdump output size: " + tcpdumpOutput.length());
    if (BaseTestConfig.LOG_TCPDUMP_OUTPUT) {
      logger.info(tcpdumpOutput);
    }
    MeasuredTrafficSize trafficSize = new StringParseUtil().getTrafficSize(tcpdumpOutput,
      AddressManager.getInstance(context).getLocalIp(), targetUrls);
    int tcpdumpPacketsDrop = ShellUtil.getTcpdumpPacketDrop(process);
    return new TcpdumpInfo(trafficSize, tcpdumpPacketsDrop);
  }
}

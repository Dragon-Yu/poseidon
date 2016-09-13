package main;

import config.BaseTestConfig;
import entity.*;
import network.RedirectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poseidon.*;
import util.Uploader;

import java.net.URI;
import java.net.URL;

/**
 * Created by Johnson on 16/9/7.
 */
public class PoseidonMain {
  private static Logger logger = LoggerFactory.getLogger(MultiConnMain.class);

  public static void main(String[] args) throws Exception {
    URI uri = new URI(BaseTestConfig.URI);
    uri = new RedirectionDetector(uri.toURL()).autoRedirect().toURI();
    logger.info("uri redirected to: " + uri);

    ExperimentData http1ExperimentData = http1(uri.toURL());
    ExperimentData http2ExperimentData = http2(uri.toURL());
    PoseidonData poseidonData = new PoseidonData(http1ExperimentData, http2ExperimentData, new ConfigData());
    new Uploader().uploadPoseidonRequest(poseidonData);
  }

  private static ExperimentData http2(URL url) throws Exception {
    Client client = new Client(true);
    Context context = new Context(client, false);
    TcpdumpManager.getInstance(context).startMonitoring();
    long t3 = System.nanoTime();
    client.visit(url, context);
    client.await(context);
    long t4 = System.nanoTime();
    TcpdumpInfo tcpdumpInfo =
      TcpdumpManager.getInstance(context).stopMonitoring(ChannelPoolManager.getInstance(context).getTargetSet());
    TcpTrafficSize tcpTrafficSize = TcpTrafficRecorder.getInstance(context).getTcpTrafficSize();
    ExperimentData experimentData = new ExperimentData(Http2ContentRecorder.getInstance(context).getTraceInfoList(),
      tcpTrafficSize, tcpdumpInfo, t4 - t3);
    return experimentData;
  }

  private static ExperimentData http1(URL url) throws Exception {
    Client client = new Client(true);
    Context context = new Context(client, true);
    TcpdumpManager.getInstance(context).startMonitoring();
    long t1 = System.nanoTime();
    client.visit(url, context);
    client.await(context);
    long t2 = System.nanoTime();
    TcpdumpInfo tcpdumpInfo =
      TcpdumpManager.getInstance(context).stopMonitoring(ChannelPoolManager.getInstance(context).getTargetSet());
    TcpTrafficSize tcpTrafficSize = TcpTrafficRecorder.getInstance(context).getTcpTrafficSize();
    ExperimentData experimentData = new ExperimentData(Http1ContentRecorder.getInstance(context).getTraceInfoList(),
      tcpTrafficSize, tcpdumpInfo, t2 - t1);
    return experimentData;
  }
}

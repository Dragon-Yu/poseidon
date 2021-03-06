package main;

import com.google.common.collect.ImmutableMap;
import config.BaseTestConfig;
import entity.*;
import fullweb.TraceInfo;
import network.RedirectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poseidon.*;
import util.Uploader;
import util.UrlUtil;

import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Johnson on 16/9/7.
 */
public class PoseidonMain {
  private static boolean http2Unsupported = false;
  private static Logger logger = LoggerFactory.getLogger(MultiConnMain.class);

  public static void main(String[] args) throws Exception {
    URI uri = UrlUtil.getUri(BaseTestConfig.URI);
    uri = new RedirectionDetector(uri.toURL()).autoRedirect().toURI();
    logger.info("uri redirected to: " + uri);
    if (!uri.getScheme().equals("https")) {
      logger.warn("skip non-https uri: " + uri);
      return;
    }

    //warm up
    http1(uri.toURL());
    http2(uri.toURL());

    ExperimentData http1ExperimentData = http1(uri.toURL());
    ExperimentData http2ExperimentData = http2(uri.toURL());
    PoseidonData poseidonData =
      new PoseidonData(http1ExperimentData, http2ExperimentData, new ConfigData(), http2Unsupported);
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
    client.shutdownGracefullyAsync(context);
    logger.info(String.format("http2 completes in %,dns", t4 - t3));
    TcpdumpInfo tcpdumpInfo =
      TcpdumpManager.getInstance(context).stopMonitoring(ChannelPoolManager.getInstance(context).getTargetSet());
    TcpTrafficSize tcpTrafficSize = TcpTrafficRecorder.getInstance(context).getTcpTrafficSize();
    List<TraceInfo> traceInfoList = new LinkedList<>();
    traceInfoList.addAll(Http2ContentRecorder.getInstance(context).getTraceInfoList());
    traceInfoList.addAll(Http1ContentRecorder.getInstance(context).getTraceInfoList());
    ExperimentData experimentData = new ExperimentData(traceInfoList, tcpTrafficSize, tcpdumpInfo, t4 - t3);
    http2Unsupported = context.isHttp2Unsupported();
    if (!context.getOtherProtocols().isEmpty()) {
      logger.error(context.getOtherProtocols().toString());
    }
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
    client.shutdownGracefullyAsync(context);
    logger.info(String.format("http1 completes in %,dns", t2 - t1));
    TcpdumpInfo tcpdumpInfo =
      TcpdumpManager.getInstance(context).stopMonitoring(ChannelPoolManager.getInstance(context).getTargetSet());
    TcpTrafficSize tcpTrafficSize = TcpTrafficRecorder.getInstance(context).getTcpTrafficSize();
    ExperimentData experimentData = new ExperimentData(Http1ContentRecorder.getInstance(context).getTraceInfoList(),
      tcpTrafficSize, tcpdumpInfo, t2 - t1);
    return experimentData;
  }

  private static TcpdumpInfo emptyTcpdumpInfo() {
    MeasuredTrafficSize measuredTrafficSize =
      new MeasuredTrafficSize(ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());
    TcpdumpInfo tcpdumpInfo = new TcpdumpInfo(measuredTrafficSize, -1);
    return tcpdumpInfo;
  }
}

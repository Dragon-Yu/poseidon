package main;

import config.BaseTestConfig;
import entity.TrafficSize;
import http2.client.Http2Client;
import https.client.HttpsClient;
import network.RedirectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ShellUtil;
import util.StringParseUtil;
import util.Uploader;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.URI;

/**
 * Measure https/http2 traffic size in network layer
 * Created by Johnson on 16/6/19.
 */
public class TrafficSizeMain {

  private static final int PROCESS_WAITING_TIME = 5000;
  private static HttpsClient httpsClient = new HttpsClient();
  private static Http2Client http2Client = new Http2Client();
  private static Logger logger = LoggerFactory.getLogger(TrafficSizeMain.class);

  public static void main(String[] args) throws Exception {
    URI uri = new URI(BaseTestConfig.URI);
    uri = new RedirectionDetector(uri.toURL()).autoRedirect().toURI();
    ShellUtil shellUtil = new ShellUtil();

    String localAddress = Inet4Address.getLocalHost().getHostAddress();
    Process process = createTcpdumpProcess(localAddress);
    shellUtil.startReadingFromProcess(process);
    httpsClient.run(uri);
    String tcpdumpOutput = shellUtil.getProcessOutputThenInterrupt(PROCESS_WAITING_TIME, process, "tcpdump");
    logger.info("tcpdump output size: " + tcpdumpOutput.length());
    if (BaseTestConfig.LOG_TCPDUMP_OUTPUT) {
      logger.info(tcpdumpOutput);
    }
    TrafficSize httpsTrafficSize = new StringParseUtil().getTrafficSize(tcpdumpOutput,
      httpsClient.getLocalAddress().getAddress().getHostAddress(), httpsClient.getLocalAddress().getPort(),
      httpsClient.getRemoteAddress().getAddress().getHostAddress(), httpsClient.getRemoteAddress().getPort());
    TrafficSize httpsTrafficSizeTcp = new TrafficSize(httpsClient.getResponseSize(), httpsClient.getRequestSize());

    shellUtil = new ShellUtil();
    process = createTcpdumpProcess(localAddress);
    shellUtil.startReadingFromProcess(process);
    http2Client.run(uri);
    tcpdumpOutput = shellUtil.getProcessOutputThenInterrupt(PROCESS_WAITING_TIME, process, "tcpdump");
    logger.info("tcpdump output size: " + tcpdumpOutput.length());
    if (BaseTestConfig.LOG_TCPDUMP_OUTPUT) {
      logger.info(tcpdumpOutput);
    }
    TrafficSize http2TrafficSize = new StringParseUtil().getTrafficSize(tcpdumpOutput,
      http2Client.getLocalAddress().getAddress().getHostAddress(), http2Client.getLocalAddress().getPort(),
      http2Client.getRemoteAddress().getAddress().getHostAddress(), http2Client.getRemoteAddress().getPort());
    TrafficSize http2TrafficSizeTcp = new TrafficSize(http2Client.getResponseSize(), http2Client.getRequestSize());

    logger.info(String.format("Https traffic size(IP): %s", httpsTrafficSize));
    logger.info(String.format("Https traffic size(TCP), input: %d, output: %d",
      httpsClient.getResponseSize(), httpsClient.getRequestSize()));
    logger.info(String.format("Http2 traffic size(IP): %s", http2TrafficSize));
    logger.info(String.format("Http2 traffic size(TCP), input: %d, output: %d",
      http2Client.getResponseSize(), http2Client.getRequestSize()));

    Uploader uploader = new Uploader();
    uploader.uploadTrafficSizeComparison(uri.toASCIIString(), httpsTrafficSize, http2TrafficSize,
      httpsTrafficSizeTcp, http2TrafficSizeTcp);
  }

  private static Process createTcpdumpProcess(String localAddress) throws IOException {
    String tcpdumpCmd = String.format(BaseTestConfig.TCPDUMP_CMD, localAddress);
    logger.info("tcpdump cmd: " + tcpdumpCmd);
    return Runtime.getRuntime().exec(tcpdumpCmd);
  }
}

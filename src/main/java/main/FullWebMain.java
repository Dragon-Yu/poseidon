package main;

import config.BaseTestConfig;
import entity.TcpdumpTrafficSize;
import entity.TrafficSize;
import fullweb.FullWebHttp2Client;
import fullweb.FullWebHttpsClient;
import network.RedirectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ShellUtil;
import util.StringParseUtil;
import util.Uploader;

import java.net.Inet4Address;
import java.net.URI;

import static main.TrafficSizeMain.createTcpdumpProcess;

/**
 * Created by Johnson on 16/7/19.
 */
public class FullWebMain {
  private static final int PROCESS_WAITING_TIME = 5000;
  private static Logger logger = LoggerFactory.getLogger(FullWebMain.class);
  private static FullWebHttp2Client http2Client = new FullWebHttp2Client();
  private static FullWebHttpsClient httpsClient = new FullWebHttpsClient();

  public static void main(String[] args) throws Exception {
    URI uri = new URI(BaseTestConfig.URI);
    uri = new RedirectionDetector(uri.toURL()).autoRedirect().toURI();
    logger.info("uri redirected to: " + uri);
    String localAddress = Inet4Address.getLocalHost().getHostAddress();

    ShellUtil shellUtil = new ShellUtil();
    Process process = createTcpdumpProcess(localAddress);
    shellUtil.startReadingFromProcess(process);
    httpsClient.run(uri);
    TrafficSize httpsTrafficSizeTcp = new TrafficSize(httpsClient.getResponseSize(), httpsClient.getRequestSize());
    String tcpdumpOutput = shellUtil.getProcessOutputThenInterrupt(PROCESS_WAITING_TIME, process, "tcpdump");
    logger.info("tcpdump output size: " + tcpdumpOutput.length());
    if (BaseTestConfig.LOG_TCPDUMP_OUTPUT) {
      logger.info(tcpdumpOutput);
    }
    TcpdumpTrafficSize httpsTrafficSize = new StringParseUtil().getTrafficSize(tcpdumpOutput,
      httpsClient.getLocalAddress().getAddress().getHostAddress(), httpsClient.getLocalAddress().getPort(),
      httpsClient.getRemoteAddress().getAddress().getHostAddress(), httpsClient.getRemoteAddress().getPort());


    shellUtil = new ShellUtil();
    process = createTcpdumpProcess(localAddress);
    shellUtil.startReadingFromProcess(process);
    http2Client.setRequestTimes(1);
    http2Client.run(uri);
    TrafficSize http2TrafficSizeTcp = new TrafficSize(http2Client.getResponseSize(), http2Client.getRequestSize());
    tcpdumpOutput = shellUtil.getProcessOutputThenInterrupt(PROCESS_WAITING_TIME, process, "tcpdump");
    logger.info("tcpdump output size: " + tcpdumpOutput.length());
    if (BaseTestConfig.LOG_TCPDUMP_OUTPUT) {
      logger.info(tcpdumpOutput);
    }
    TcpdumpTrafficSize http2TrafficSize = new StringParseUtil().getTrafficSize(tcpdumpOutput,
      http2Client.getLocalAddress().getAddress().getHostAddress(), http2Client.getLocalAddress().getPort(),
      http2Client.getRemoteAddress().getAddress().getHostAddress(), http2Client.getRemoteAddress().getPort());

    logger.info(String.format("Https traffic size(IP): %s", httpsTrafficSize));
    logger.info(String.format("Https traffic size(TCP), input: %d, output: %d",
      httpsClient.getResponseSize(), httpsClient.getRequestSize()));
    logger.info(String.format("Http2 traffic size(IP): %s", http2TrafficSize));
    logger.info(String.format("Http2 traffic size(TCP), input: %d, output: %d",
      http2Client.getResponseSize(), http2Client.getRequestSize()));

    Uploader uploader = new Uploader();
    uploader.uploadFullWebRequest(uri.toASCIIString(), httpsClient.getTraces(), http2Client.getTraces(),
      httpsTrafficSize, http2TrafficSize, httpsTrafficSizeTcp, http2TrafficSizeTcp, httpsClient.getTimeElapsed(),
      http2Client.getTimeElapsed());
  }
}

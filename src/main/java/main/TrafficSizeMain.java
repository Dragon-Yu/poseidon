package main;

import config.BaseTestConfig;
import entity.TrafficSize;
import http2.client.Http2Client;
import https.client.HttpsClient;
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

  static HttpsClient httpsClient = new HttpsClient();
  static Http2Client http2Client = new Http2Client();
  private static Logger logger = LoggerFactory.getLogger(TrafficSizeMain.class);

  public static void main(String[] args) throws Exception {
    URI uri = new URI(BaseTestConfig.URI);
    ShellUtil shellUtil = new ShellUtil();

    String localAddress = Inet4Address.getLocalHost().getHostAddress();
    Process process = createTcpdumpProcess(localAddress);
    shellUtil.startReadingFromProcess(process);
    httpsClient.run(uri);
    String tcpdumpOutput = shellUtil.getProcessOutputThenInterrupt(3000, process);
    TrafficSize httpsTrafficSize = new StringParseUtil().getTrafficSize(tcpdumpOutput,
      httpsClient.getLocalAddress().getAddress().getHostAddress(), httpsClient.getLocalAddress().getPort(),
      httpsClient.getRemoteAddress().getAddress().getHostAddress(), httpsClient.getRemoteAddress().getPort());

    process = createTcpdumpProcess(localAddress);
    shellUtil.startReadingFromProcess(process);
    http2Client.run(uri);
    tcpdumpOutput = shellUtil.getProcessOutputThenInterrupt(3000, process);
    TrafficSize http2TrafficSize = new StringParseUtil().getTrafficSize(tcpdumpOutput,
      http2Client.getLocalAddress().getAddress().getHostAddress(), http2Client.getLocalAddress().getPort(),
      http2Client.getRemoteAddress().getAddress().getHostAddress(), http2Client.getRemoteAddress().getPort());

    System.out.println(httpsTrafficSize);
    System.out.println(http2TrafficSize);

    Uploader uploader = new Uploader();
    uploader.uploadTrafficSizeComparison(BaseTestConfig.URI, httpsTrafficSize, http2TrafficSize);
  }

  private static Process createTcpdumpProcess(String localAddress) throws IOException {
    return Runtime.getRuntime().exec(String.format("sudo tcpdump -nnv ip and host %s", localAddress));
  }
}
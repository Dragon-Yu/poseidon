package main;

import config.BaseTestConfig;
import fullweb.FullWebHttp2Client;
import network.RedirectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created by Johnson on 16/7/19.
 */
public class FullWebMain {
  static Logger logger = LoggerFactory.getLogger(FullWebMain.class);

  public static void main(String[] args) throws Exception {
    URI uri = new URI(BaseTestConfig.URI);
    uri = new RedirectionDetector(uri.toURL()).autoRedirect().toURI();
    logger.info("uri redirected to: " + uri);
    FullWebHttp2Client http2Client = new FullWebHttp2Client();
    http2Client.setRequestTimes(1);
    http2Client.run(uri);
  }
}

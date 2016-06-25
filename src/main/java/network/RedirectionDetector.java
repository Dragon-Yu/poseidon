package network;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Detect http 302 response
 * Created by Johnson on 16/6/22.
 */
public class RedirectionDetector {
  static Logger logger = LoggerFactory.getLogger(RedirectionDetector.class);
  private URL url;
  private URL redirectedUrl;

  public RedirectionDetector(URL url) {
    this.url = url;
  }

  public static void main(String[] args) throws Exception {
    URL url = new URL("https://jigsaw.w3.org/HTTP/300/302.html");
    RedirectionDetector detector = new RedirectionDetector(url);
    if (detector.detect()) {
      System.out.println(detector.getRedirectedUrl());
      System.out.println(detector.getRedirectedUrl().toURI().toASCIIString());
    }
  }

  public URL autoRedirect() throws IOException, URISyntaxException {
    if (detect()) {
      logger.info("redirected to: " + getRedirectedUrl());
      return getRedirectedUrl();
    }
    return url;
  }

  public boolean detect() throws IOException, URISyntaxException {
    HttpsURLConnection.setFollowRedirects(false);
    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    HttpURLConnection.setFollowRedirects(false);
    String urlStr = connection.getHeaderField("Location");
    if (StringUtils.isEmpty(urlStr)) {
      return false;
    } else {
      redirectedUrl = new URL(urlStr);
      return true;
    }
  }

  public URL getRedirectedUrl() {
    return redirectedUrl;
  }
}

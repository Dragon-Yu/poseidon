package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Johnson on 2016/10/14.
 */
public class UrlUtil {
  private static final Logger logger = LoggerFactory.getLogger(UrlUtil.class);

  public static URI url2Uri(URL url) throws URISyntaxException {
    return new URI(url.getProtocol(), null, url.getHost(),
      url.getPort(), url.getPath(), url.getQuery(), url.getRef());
  }

  public static URI getUri(String url) throws URISyntaxException, MalformedURLException {
    return url2Uri(new URL(url));
  }

  public static URL encodeUrl(URL url) {
    try {
      return url2Uri(url).toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      logger.error(e.getMessage(), e);
    }
    return url;
  }
}

package main;

import com.google.gson.Gson;
import config.BaseTestConfig;
import entity.Http2CheckResultData;
import http2.client.Http2Client;
import io.netty.channel.ConnectTimeoutException;
import network.RedirectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Uploader;

import java.net.SocketAddress;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Check given host supporting http2 or not
 * Created by Johnson on 16/6/16.
 */
public class Http2SupportChecker {
  private static Logger logger = LoggerFactory.getLogger(Http2SupportChecker.class);

  private static Map<SocketAddress, URI> socketAddressURIMap = new ConcurrentHashMap<>();
  private static Map<URI, Set<String>> errorsMap = new ConcurrentHashMap<>();

  static synchronized void reportError(URI uri, String error) {
    if (errorsMap.containsKey(uri)) {
      errorsMap.get(uri).add(error);
    } else {
      Set<String> errors = new HashSet<>();
      errors.add(error);
      errorsMap.put(uri, errors);
    }
  }

  public static synchronized void logSocketAddress(SocketAddress socketAddress, URI uri) {
    socketAddressURIMap.put(socketAddress, uri);
  }

  public static void main(String[] args) throws Exception {
    Gson gson = new Gson();
    Uploader uploader = new Uploader();
    BaseTestConfig.REQUEST_TIMES = 1;

    Http2SupportChecker http2SupportChecker = new Http2SupportChecker();

    String[] uris = gson.fromJson(BaseTestConfig.HOSTS_TO_CHECK, String[].class);
    for (String uriStr : uris) {
      URI uri = new URI(uriStr);
      uri = new RedirectionDetector(uri.toURL()).autoRedirect().toURI();
      try {
        http2SupportChecker.connect(uri);
      } catch (ConnectTimeoutException e) {
        logger.error("host unreachable: " + uri.toString());
        reportError(uri, "host unreachable");
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        if (e.getCause() != null) {
          reportError(uri, e.getCause().getMessage());
        } else {
          reportError(uri, e.getMessage());
        }
      }
      uploader.uploadHttp2CheckResult(new Http2CheckResultData(uri.toASCIIString(), !errorsMap.containsKey(uri), errorsMap.get(uri)));
    }
    logger.info(gson.toJson(errorsMap));
  }

  void connect(URI uri) throws Exception {
    Http2Client http2Client = new Http2Client();
    http2Client.run(uri);
  }
}

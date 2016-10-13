package util;

import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by Johnson on 16/9/10.
 */
public class SimpleUrl {
  private static final Logger logger = LoggerFactory.getLogger(SimpleUrl.class);
  private final String host;
  private final String addr;
  private final int port;

  public SimpleUrl(URL url) {
    this.host = url.getHost();
    if (url.getPort() > 0) {
      this.port = url.getPort();
    } else {
      this.port = url.getDefaultPort();
    }
    String addr = host;
    try {
      addr = InetAddress.getByName(host).getHostAddress();
    } catch (UnknownHostException e) {
      logger.error(e.getMessage());
    } finally {
      this.addr = addr;
    }
  }

  public SimpleUrl(String host, String addr, int port) {
    this.host = host;
    this.addr = addr;
    this.port = port;
  }

  public String getAddr() {
    return addr;
  }

  public int getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SimpleUrl)) return false;
    SimpleUrl simpleUrl = (SimpleUrl) o;
    return port == simpleUrl.port &&
      Objects.equal(host, simpleUrl.host);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(addr, port);
  }

  @Override
  public String toString() {
    return "SimpleUrl{" +
      "host='" + host + '\'' +
      ", addr='" + addr + '\'' +
      ", port=" + port +
      '}';
  }
}

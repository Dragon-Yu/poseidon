package poseidon;

import io.netty.util.internal.ConcurrentSet;

import java.util.Set;

/**
 * Created by Johnson on 16/9/7.
 */
public class Context {

  final Client client;
  boolean httpsOnly = false;
  private boolean http2Unsupported = false;
  private Set<String> otherProtocols = new ConcurrentSet<>();

  public Context(Client client, boolean httpsOnly) {
    this.client = client;
    this.httpsOnly = httpsOnly;
  }

  public Context(Client client) {
    this.client = client;
  }

  public void setHttpsOnly(boolean httpsOnly) {
    this.httpsOnly = httpsOnly;
  }

  public void addOtherProtocol(String str) {
    otherProtocols.add(str);
  }

  public Set<String> getOtherProtocols() {
    return otherProtocols;
  }

  public boolean isHttp2Unsupported() {
    return http2Unsupported;
  }

  public void setHttp2Unsupported(boolean http2Unsupported) {
    this.http2Unsupported = http2Unsupported;
  }
}

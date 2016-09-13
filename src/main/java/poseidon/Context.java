package poseidon;

/**
 * Created by Johnson on 16/9/7.
 */
public class Context {

  final Client client;
  boolean httpsOnly = false;

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
}

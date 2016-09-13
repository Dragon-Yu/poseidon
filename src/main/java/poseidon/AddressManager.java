package poseidon;

import java.net.InetSocketAddress;

/**
 * Created by Johnson on 16/9/13.
 */
public class AddressManager {

  private static AddressManager ourInstance;
  private String localIp;
  private Context context = null;

  private AddressManager(Context context) {
    this.context = context;
  }

  public static synchronized AddressManager getInstance(Context context) {
    if (ourInstance == null) {
      ourInstance = new AddressManager(context);
    } else if (ourInstance.context != null && ourInstance.context != context) {
      ourInstance = new AddressManager(context);
    }
    return ourInstance;
  }

  public String getLocalIp() {
    return localIp;
  }

  public void setLocalIp(InetSocketAddress localAddress) {
    if (this.localIp == null) {
      this.localIp = localAddress.getAddress().getHostAddress();
    } else {
      assert this.localIp.equals(localAddress.getAddress().getHostAddress());
    }
  }
}

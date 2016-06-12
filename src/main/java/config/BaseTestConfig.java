package config;

/**
 * This test case just request a simple RESTful URI for many times,
 * Then we compare the difference between HTTPS and HTTP/2
 * Created by johnson on 16/1/16.
 */
public class BaseTestConfig {

  public static int REQUEST_TIMES = 20;
  public static boolean SSL = true;
  public static String URI = "https://www.google.com:443";
  public static final String ATTR_URI = "poseidon_url";

  static {
    if (System.getenv(ATTR_URI) != null && !System.getenv(ATTR_URI).isEmpty()) {
      URI = System.getenv(ATTR_URI);
    }
  }
}

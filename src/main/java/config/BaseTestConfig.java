package config;

/**
 * This test case just request a simple RESTful URI for many times,
 * Then we compare the difference between HTTPS and HTTP/2
 * Created by johnson on 16/1/16.
 */
public class BaseTestConfig {

  public static final int REQUEST_TIMES = 20;
  public static final boolean SSL = true;
  public static final String URI = "https://isports-1093.appspot.com:443/ping";
}

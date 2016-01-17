package config;

/**
 * This test case just request a simple RESTful URL for many times,
 * Then we compare the difference between HTTPS and HTTP/2
 * Created by johnson on 16/1/16.
 */
public class BaseTestConfig {

  public static final int REQUEST_TIMES = 100;
  public static final boolean SSL = true;
  public static final String HOST = "isports-1093.appspot.com";
  public static final int PORT = 443;
  public static final String URL = "/ping";

}

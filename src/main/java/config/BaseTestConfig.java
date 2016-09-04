package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * This test case just request a simple RESTful URI for many times,
 * Then we compare the difference between HTTPS and HTTP/2
 * Created by johnson on 16/1/16.
 */
public class BaseTestConfig {

  public static final int HTTPS_PORT = 443;
  private static final Logger logger = LoggerFactory.getLogger(BaseTestConfig.class);
  //variables that can be set by shell environment variable
  private static final String ATTR_URI = "target_url";
  private static final String ATTR_API_REQUEST_LOG_URL = "api_request_log_url";
  private static final String ATTR_REQUEST_TIMES = "request_times";
  private static final String ATTR_HOSTS_TO_CHECK = "hosts_to_check";
  private static final String ATTR_TCPDUMP_CMD = "tcpdump_cmd";
  private static final String ATTR_LOG_TCPDUMP_OUTPUT = "log_tcpdump_output";
  private static final String ATTR_IGNORE_OUTTER_LINK = "ignore_outter_link";
  public static int REQUEST_TIMES = 20;
  public static int CHANNEL_POOL_SIZE = 20;
  public static boolean LOG_TCPDUMP_OUTPUT = false;
  public static boolean IGNORE_OUTTER_LINK = true;
  public static String URI = "https://pay.sohu.com/payment/index.action";
  //  public static String URI = "https://www.google.com";
  public static String API_REQUEST_LOG_URL = "https://prometheus-1151.appspot.com/log/api_request";
  public static String HTTP2_CHECK_LOG_URL = "https://prometheus-1151.appspot.com/log/http2_check";
  public static String TRAFFIC_SIZE_LOG_URL = "https://prometheus-1151.appspot.com/log/traffic_size";
  public static String FULL_WEB_LOG_URL = "https://prometheus-1151.appspot.com/log/full_web";
  public static String MULTI_CONN_LOG_URL = "https://prometheus-1151.appspot.com/log/multi_conn";
  public static String HOSTS_TO_CHECK = "[\"https://baidu.com\", \"https://google.com\"]";
  public static String TCPDUMP_CMD = "sudo tcpdump -B 10240 -lnnv -i any tcp";

  static {
    for (Field field : BaseTestConfig.class.getDeclaredFields()) {
      if (field.getName().startsWith("ATTR_")) {
        String name = field.getName().substring(5);
        try {
          if (System.getenv(field.get(null).toString()) != null) {
            Field fieldToSet = BaseTestConfig.class.getDeclaredField(name);
            Class type = fieldToSet.getType();
            if (type == String.class) {
              fieldToSet.set(null, System.getenv(field.get(null).toString()));
            } else if (type == Integer.class || type == Integer.TYPE) {
              fieldToSet.set(null, Integer.parseInt(System.getenv(field.get(null).toString())));
            } else if (type == Long.class || type == Long.TYPE) {
              fieldToSet.set(null, Long.parseLong(System.getenv(field.get(null).toString())));
            } else if (type == Boolean.class || type == Boolean.TYPE) {
              fieldToSet.set(null, Boolean.parseBoolean(System.getenv(field.get(null).toString())));
            } else {
              throw new Exception("unsupported settable variable type: " + type.getName());
            }
          }
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }
    }
  }
}

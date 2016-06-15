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

  private static final Logger logger = LoggerFactory.getLogger(BaseTestConfig.class);

  public static int REQUEST_TIMES = 20;
  public static boolean SSL = true;
  public static String URI = "https://www.google.com:443";
  public static String LOG_URL = "https://prometheus-1151.appspot.com/log/api_request";

  //variables that can be set by shell environment variable
  private static final String ATTR_URI = "target_url";
  private static final String ATTR_LOG_URL = "log_url";
  private static final String ATTR_REQUEST_TIMES = "request_times";

  static {
    for (Field field: BaseTestConfig.class.getDeclaredFields()) {
      if (field.getName().startsWith("ATTR_")) {
        String name = field.getName().substring(5);
        try {
          if (System.getenv(field.get(null).toString()) != null) {
            Field fieldToSet = BaseTestConfig.class.getDeclaredField(name);
            Class type = fieldToSet.getType();
            if (type == String.class) {
              fieldToSet.set(null, System.getenv(field.get(null).toString()));
            }
            else if (type == Integer.class || type == Integer.TYPE) {
              fieldToSet.set(null, Integer.parseInt(System.getenv(field.get(null).toString())));
            } else if (type == Long.class || type == Long.TYPE) {
              fieldToSet.set(null, Long.parseLong(System.getenv(field.get(null).toString())));
            } else if (type == Boolean.class || type == Boolean.TYPE) {
              fieldToSet.set(null, Boolean.parseBoolean(System.getenv(field.get(null).toString())));
            }
            else {
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

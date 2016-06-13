package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Easy way to execute shell commands
 * Created by Johnson on 16/6/14.
 */
public class ShellUtil {
  private static Logger logger = LoggerFactory.getLogger(ShellUtil.class);

  public String exec(String command) {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      Process process = Runtime.getRuntime().exec(command);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return stringBuilder.toString().trim();
  }
}

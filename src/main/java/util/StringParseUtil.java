package util;

import entity.TrafficSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse string according to specific format
 * Created by Johnson on 16/6/19.
 */
public class StringParseUtil {
  private static Logger logger = LoggerFactory.getLogger(StringParseUtil.class);
  private static final Pattern lengthPattern = Pattern.compile(".*length (\\d+).*");

  /**
   * Get traffic size from tcpdump output
   *
   * @param str output of `tcpdump -nnr`
   * @return traffic size
   */
  public TrafficSize getTrafficSize(String str, String localIp, int localPort, String removeIp, int remotePort) {
    long inputSize = 0;
    long outputSize = 0;
    String local = String.format("%s.%d", localIp, localPort);
    String remote = String.format("%s.%d", removeIp, remotePort);
    Pattern inputPattern = Pattern.compile(String.format("%s > %s", remote, local));
    Pattern outputPattern = Pattern.compile(String.format("%s > %s", local, remote));
    for (String line : str.split("\n")) {
      String[] lines = line.split(";;;");
      if (lines.length < 2) {
        logger.warn("tcpdump output line num error: " + line);
      } else if (inputPattern.matcher(lines[1]).find()) {
        inputSize += getIpLength(lines[0]);
      } else if (outputPattern.matcher(lines[1]).find()) {
        outputSize += getIpLength(lines[0]);
      }
    }
    return new TrafficSize(inputSize, outputSize);
  }

  long getIpLength(String str) {
    Matcher matcher = lengthPattern.matcher(str);
    if (matcher.matches()) {
      return Long.valueOf(matcher.group(1));
    }
    throw new RuntimeException("Unrecognized tcpdump format: " + str);
  }
}

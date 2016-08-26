package util;

import entity.TrafficSize;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse string according to specific format
 * Created by Johnson on 16/6/19.
 */
public class StringParseUtil {
  private static final Pattern lengthPattern = Pattern.compile(".*length (\\d+).*");
  private static Logger logger = LoggerFactory.getLogger(StringParseUtil.class);

  /**
   * Get traffic size from tcpdump output
   *
   * @param str output of `tcpdump -nnr`
   * @return traffic size
   */
  public TrafficSize getTrafficSize(String str, String localIp, int localPort, String removeIp, int remotePort) {
    MutableLong inputSize = new MutableLong(0);
    MutableLong outputSize = new MutableLong(0);
    String local = String.format("%s.%d", localIp, localPort);
    String remote = String.format("%s.%d", removeIp, remotePort);
    Pattern inputPattern = Pattern.compile(String.format("%s > %s", remote, local));
    Pattern outputPattern = Pattern.compile(String.format("%s > %s", local, remote));
    logger.info("input pattern: " + inputPattern.toString());
    logger.info("output pattern: " + outputPattern.toString());
    List<String> lineBuffer = new ArrayList<>();
    for (String line : str.split("\n")) {
      if (line.startsWith(" ") || line.startsWith("\t")) {
        lineBuffer.add(line);
      } else {
        parseLineBuffer(lineBuffer, inputPattern, outputPattern, inputSize, outputSize);
        lineBuffer.clear();
        lineBuffer.add(line);
      }
    }
    if (!lineBuffer.isEmpty()) {
      parseLineBuffer(lineBuffer, inputPattern, outputPattern, inputSize, outputSize);
      lineBuffer.clear();
    }
    return new TrafficSize(inputSize.getValue(), outputSize.getValue());
  }

  private void parseLineBuffer(List<String> lines, Pattern inputPattern, Pattern outputPattern,
                               MutableLong inputSize, MutableLong outputSize) {
    if (lines.isEmpty()) {
      return;
    }
    String data = StringUtils.join(lines, " ");
    if (inputPattern.matcher(data).find()) {
      inputSize.add(getIpPackageSize(data));
    } else if (outputPattern.matcher(data).find()) {
      outputSize.add(getIpPackageSize(data));
    }
  }

  private long getIpPackageSize(String str) {
    Matcher matcher = lengthPattern.matcher(str);
    if (matcher.matches()) {
      return Long.valueOf(matcher.group(1));
    }
    throw new RuntimeException("Unrecognized tcpdump format: " + str);
  }
}

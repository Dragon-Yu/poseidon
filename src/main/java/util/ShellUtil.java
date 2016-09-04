package util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * Easy way to execute shell commands
 * Created by Johnson on 16/6/14.
 */
public class ShellUtil {
  private static Logger logger = LoggerFactory.getLogger(ShellUtil.class);
  private Thread processReadingThread;
  private Thread processWarningLoggingThread;
  private StringBuilder processOutput;

  public static long getPid(Process p) {
    long pid = -1;
    try {
      Field f = p.getClass().getDeclaredField("pid");
      f.setAccessible(true);
      pid = f.getLong(p);
      f.setAccessible(false);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return pid;
  }

  public String exec(String command) {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      Process process = Runtime.getRuntime().exec(command);
      readIntoStringBuilder(process, stringBuilder);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return stringBuilder.toString().trim();
  }

  public void startReadingFromProcess(final Process process) {
    processOutput = new StringBuilder();
    processReadingThread = new Thread() {
      @Override
      public void run() {
        try {
          readIntoStringBuilder(process, processOutput);
        } catch (InterruptedException e) {
          //Do nothing if it is interrupted
        } catch (IOException e) {
          if (!e.getMessage().equalsIgnoreCase("Stream Closed")) {
            logger.error(e.getMessage(), e);
          }
        }
      }
    };
    processReadingThread.setName("process_reading_thread");
    processWarningLoggingThread = new Thread() {
      @Override
      public void run() {
        try {
          logWarningMessage(process);
        } catch (InterruptedException e) {
          //Do nothing if it is interrupted
        } catch (IOException e) {
          if (!e.getMessage().equalsIgnoreCase("Stream Closed")) {
            logger.error(e.getMessage(), e);
          }
        }
      }
    };
    processWarningLoggingThread.setName("process_warning_logging_thread");
    processWarningLoggingThread.start();
    processReadingThread.start();
  }

  public String getProcessOutputThenInterrupt(int wait, Process process, String processName) {
    try {
      Thread.sleep(wait);
      logger.info("kill process");
      Runtime.getRuntime().exec(String.format("sudo pkill -SIGINT -P %d", getPid(process)));
      if (!StringUtils.isEmpty(processName)) {
        killProcess(processName);
      }
      Thread.sleep(3000);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    logger.info("interrupt thread");
    processReadingThread.interrupt();
    processWarningLoggingThread.interrupt();
    return processOutput.toString().trim();
  }

  private void killProcess(String process) throws IOException {
    Runtime.getRuntime().exec(String.format("sudo kill -9 `ps -ef|grep %s| awk '{print $2}'`", process));
  }

  private void logWarningMessage(Process process) throws IOException, InterruptedException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    String line;
    while ((line = reader.readLine()) != null) {
      logger.warn(line);
      if (Thread.currentThread().isInterrupted() && !reader.ready()) {
        throw new InterruptedException();
      }
    }
  }

  private void readIntoStringBuilder(Process process, StringBuilder stringBuilder)
    throws IOException, InterruptedException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = reader.readLine()) != null) {
      if (Thread.currentThread().isInterrupted() && !reader.ready()) {
        throw new InterruptedException();
      }
      stringBuilder.append(line + "\n");
    }
    logger.info("input stream read completed");
  }
}

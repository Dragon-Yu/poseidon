package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Easy way to execute shell commands
 * Created by Johnson on 16/6/14.
 */
public class ShellUtil {
  private static Logger logger = LoggerFactory.getLogger(ShellUtil.class);
  private Thread processReadingThread;
  private StringBuilder processOutput;

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

  public void startReadingFromProcess(Process process) {
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
    processReadingThread.start();
  }

  public String getProcessOutputThenInterrupt(int wait, Process process) {
    try {
      Thread.sleep(wait);
      logger.info("kill process");
      process.destroy();
      Thread.sleep(3000);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    logger.info("interrupt thread");
    processReadingThread.interrupt();
    return processOutput.toString().trim();
  }

  private void readIntoStringBuilder(Process process, StringBuilder stringBuilder)
    throws IOException, InterruptedException {
    InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
    char[] buff = new char[1000];
    int len;
    while ((len = inputStreamReader.read(buff)) > 0) {
      if (Thread.currentThread().isInterrupted()) {
        throw new InterruptedException();
      }
      stringBuilder.append(buff, 0, len);
    }
    logger.info("input stream read completed");
  }
}

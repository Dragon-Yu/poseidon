package util;

import com.google.gson.GsonBuilder;
import config.BaseTestConfig;
import entity.ApiRequestData;
import entity.Http2CheckResultData;
import entity.TrafficSize;
import entity.TrafficSizeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Uploader util
 * Created by Johnson on 16/6/12.
 */
public class Uploader {

  private static Logger logger = LoggerFactory.getLogger(Uploader.class);

  public void uploadHttp2VsHttp(ApiRequestData apiRequestData) throws IOException {
    String postData = new GsonBuilder().create().toJson(apiRequestData);
    URL url = new URL(BaseTestConfig.API_REQUEST_LOG_URL);
    String response = post(url, postData);
    logger.info(response);
  }

  public void uploadHttp2CheckResult(Http2CheckResultData http2CheckResultData) throws IOException {
    String postData = new GsonBuilder().create().toJson(http2CheckResultData);
    URL url = new URL(BaseTestConfig.HTTP2_CHECK_LOG_URL);
    String response = post(url, postData);
    logger.info(response);
  }

  public void uploadTrafficSizeComparison(String targetUrl, TrafficSize httpsTrafficSize, TrafficSize http2TrafficSize,
                                          TrafficSize httpsTrafficSizeTcp, TrafficSize http2TrafficSizeTcp)
    throws IOException {
    TrafficSizeData data = new TrafficSizeData(targetUrl, httpsTrafficSize.getInput(), httpsTrafficSize.getOutput(),
      http2TrafficSize.getInput(), http2TrafficSize.getOutput(), httpsTrafficSizeTcp.getInput(),
      httpsTrafficSizeTcp.getOutput(), http2TrafficSizeTcp.getInput(), http2TrafficSizeTcp.getOutput());
    String postData = new GsonBuilder().create().toJson(data);
    URL url = new URL(BaseTestConfig.TRAFFIC_SIZE_LOG_URL);
    String response = post(url, postData);
    logger.info(response);
  }

  private String post(URL url, String postData) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(30000);
    connection.setDoOutput(true);
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
      dataOutputStream.write(postData.getBytes());
    }
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      stringBuilder.append(line + "\n");
    }
    return stringBuilder.toString().trim();
  }
}

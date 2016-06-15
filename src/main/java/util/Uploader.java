package util;

import com.google.gson.GsonBuilder;
import config.BaseTestConfig;
import entity.ApiRequestData;
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

  public void upload_http2_vs_http(ApiRequestData apiRequestData) throws IOException {
    String postData = new GsonBuilder().create().toJson(apiRequestData);
    URL url = new URL(BaseTestConfig.LOG_URL);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
    logger.info(stringBuilder.toString().trim());
  }
}

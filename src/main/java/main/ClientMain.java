package main;

import config.BaseTestConfig;
import entity.ApiRequestData;
import http2.client.Http2Client;
import https.client.HttpsClient;
import util.Uploader;

/**
 * Main class for Https/Http2 client
 * Created by johnson on 16/3/29.
 */
public class ClientMain {

  static HttpsClient httpsClient = new HttpsClient();
  static Http2Client http2Client = new Http2Client();

  public static void main(String[] args) throws Exception {
    httpsClient.run();
    http2Client.run();

    long httpsTime = httpsClient.getTimeElapsed();
    long http2Time = http2Client.getTimeElapsed();

    Uploader uploader = new Uploader();
    uploader.upload_http2_vs_http(new ApiRequestData(BaseTestConfig.URI, http2Time, httpsTime));
  }
}

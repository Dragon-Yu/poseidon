package main;

import http2.client.Http2Client;
import https.client.HttpsClient;

/**
 * Created by johnson on 16/3/29.
 */
public class ClientMain {

  static HttpsClient httpsClient = new HttpsClient();
  static Http2Client http2Client = new Http2Client();

  public static void main(String[] args) throws Exception {
    httpsClient.run();
    http2Client.run();
  }
}

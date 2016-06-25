package main;

import config.BaseTestConfig;
import entity.ApiRequestData;
import http2.client.Http2Client;
import https.client.HttpsClient;
import network.RedirectionDetector;
import util.Uploader;

import java.net.URI;

/**
 * Main class for Https/Http2 client
 * Created by johnson on 16/3/29.
 */
public class ClientMain {

  static HttpsClient httpsClient = new HttpsClient();
  static Http2Client http2Client = new Http2Client();

  public static void main(String[] args) throws Exception {
    URI uri = new URI(BaseTestConfig.URI);
    uri = new RedirectionDetector(uri.toURL()).autoRedirect().toURI();
    httpsClient.run(uri);
    http2Client.run(uri);

    Uploader uploader = new Uploader();
    uploader.uploadHttp2VsHttp(new ApiRequestData(uri.toASCIIString(), http2Client.getTimeElapsed(),
      httpsClient.getTimeElapsed(), httpsClient.getRequestSize(), httpsClient.getResponseSize(),
      http2Client.getRequestSize(), http2Client.getResponseSize()));
  }
}

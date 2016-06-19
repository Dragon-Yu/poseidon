package entity;

import com.google.gson.annotations.SerializedName;
import config.BaseTestConfig;
import util.ShellUtil;

/**
 * Created by Johnson on 16/6/19.
 */
public class TrafficSizeData {
  @SerializedName("host_name")
  String hostName;
  @SerializedName("time_stamp")
  long timeStamp;
  @SerializedName("target_url")
  String targetUrl;
  @SerializedName("request_times")
  int requestTimes;
  @SerializedName("https_response_size")
  long httpsResponseSize;
  @SerializedName("https_request_size")
  long httpsRequestSize;
  @SerializedName("http2_response_size")
  long http2ResponseSize;
  @SerializedName("http2_request_size")
  long http2RequestSize;

  public TrafficSizeData(String targetUrl, long httpsResponseSize, long httpsRequestSize, long http2ResponseSize, long http2RequestSize) {
    this.targetUrl = targetUrl;
    this.httpsResponseSize = httpsResponseSize;
    this.httpsRequestSize = httpsRequestSize;
    this.http2ResponseSize = http2ResponseSize;
    this.http2RequestSize = http2RequestSize;

    timeStamp = System.currentTimeMillis();
    hostName = new ShellUtil().exec("hostname");
    requestTimes = BaseTestConfig.REQUEST_TIMES;
  }
}

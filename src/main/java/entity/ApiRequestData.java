package entity;

import com.google.gson.annotations.SerializedName;
import config.BaseTestConfig;
import util.ShellUtil;

/**
 * for request_log api
 * Created by Johnson on 16/6/14.
 */
public class ApiRequestData {

  @SerializedName("time_stamp")
  private long timeStamp;

  @SerializedName("host_name")
  private String hostName;

  @SerializedName("target_url")
  private String targetUrl;

  @SerializedName("http2_transfer_time")
  private long http2TransferTime;

  @SerializedName("https_transfer_time")
  private long httpsTransferTime;

  @SerializedName("request_times")
  private int requestTimes;

  @SerializedName("https_request_size")
  private long httpsRequestSize;

  @SerializedName("https_response_size")
  private long httpsResponseSize;

  @SerializedName("http2_request_size")
  private long http2RequestSize;

  @SerializedName("http2_response_size")
  private long http2ResponseSize;

  public ApiRequestData(String targetUrl, long http2TransferTime, long httpsTransferTime, long httpsRequestSize,
                        long httpsResponseSize, long http2RequestSize, long http2ResponseSize) {
    this.targetUrl = targetUrl;
    this.http2TransferTime = http2TransferTime;
    this.httpsTransferTime = httpsTransferTime;
    this.httpsRequestSize = httpsRequestSize;
    this.httpsResponseSize = httpsResponseSize;
    this.http2RequestSize = http2RequestSize;
    this.http2ResponseSize = http2ResponseSize;

    timeStamp = System.currentTimeMillis();
    hostName = new ShellUtil().exec("hostname");
    requestTimes = BaseTestConfig.REQUEST_TIMES;
  }
}

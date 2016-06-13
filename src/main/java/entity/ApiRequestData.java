package entity;

import com.google.gson.annotations.SerializedName;
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

  public ApiRequestData(String targetUrl, long http2TransferTime, long httpsTransferTime) {
    this.targetUrl = targetUrl;
    this.http2TransferTime = http2TransferTime;
    this.httpsTransferTime = httpsTransferTime;

    timeStamp = System.currentTimeMillis();
    hostName = new ShellUtil().exec("hostname");
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public String getHostName() {
    return hostName;
  }

  public String getTargetUrl() {
    return targetUrl;
  }

  public long getHttp2TransferTime() {
    return http2TransferTime;
  }

  public long getHttpsTransferTime() {
    return httpsTransferTime;
  }
}

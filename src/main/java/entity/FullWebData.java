package entity;

import com.google.gson.annotations.SerializedName;
import util.ShellUtil;

/**
 * Created by Johnson on 16/7/19.
 */
public class FullWebData {

  @SerializedName("time_stamp")
  private long timeStamp;

  @SerializedName("host_name")
  private String hostName;

  @SerializedName("target_url")
  private String targetUrl;

  @SerializedName("https_response_size")
  private long httpsResponseSize;

  @SerializedName("https_request_size")
  private long httpsRequestSize;

  @SerializedName("http2_response_size")
  private long http2ResponseSize;

  @SerializedName("http2_request_size")
  private long http2RequestSize;

  @SerializedName("https_response_size_tcp")
  private long httpsResponseSizeTcp;

  @SerializedName("https_request_size_tcp")
  private long httpsRequestSizeTcp;

  @SerializedName("http2_response_size_tcp")
  private long http2ResponseSizeTcp;

  @SerializedName("http2_request_size_tcp")
  private long http2RequestSizeTcp;

  private String[] links;

  public FullWebData(String targetUrl, long httpsResponseSize, long httpsRequestSize, long http2ResponseSize,
                     long http2RequestSize, long httpsResponseSizeTcp, long httpsRequestSizeTcp,
                     long http2ResponseSizeTcp, long http2RequestSizeTcp, String[] links) {
    this.targetUrl = targetUrl;
    this.httpsResponseSize = httpsResponseSize;
    this.httpsRequestSize = httpsRequestSize;
    this.http2ResponseSize = http2ResponseSize;
    this.http2RequestSize = http2RequestSize;
    this.httpsResponseSizeTcp = httpsResponseSizeTcp;
    this.httpsRequestSizeTcp = httpsRequestSizeTcp;
    this.http2ResponseSizeTcp = http2ResponseSizeTcp;
    this.http2RequestSizeTcp = http2RequestSizeTcp;
    this.links = links;

    timeStamp = System.currentTimeMillis();
    hostName = new ShellUtil().exec("hostname");
  }
}

package entity;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import util.ShellUtil;

import java.util.Map;

/**
 * Created by Johnson on 16/7/19.
 */
public class MultiConnData {

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

  @SerializedName("https_response_size_tcp_tcpdump")
  private long httpsResponseSizeTcpTcpdump;

  @SerializedName("https_request_size_tcp_tcpdump")
  private long httpsRequestSizeTcpTcpdump;

  @SerializedName("http2_response_size_tcp_tcpdump")
  private long http2ResponseSizeTcpTcpdump;

  @SerializedName("http2_request_size_tcp_tcpdump")
  private long http2RequestSizeTcpTcpdump;

  @SerializedName("https_traces")
  private JsonElement httpsTraces;

  @SerializedName("http2_traces")
  private JsonElement http2Traces;

  @SerializedName("https_channel_request_size")
  private Map<String, Long> httpsChannelRequestSize;

  @SerializedName("https_channel_response_size")
  private Map<String, Long> httpsChannelResponseSize;

  @SerializedName("http2_transfer_time")
  private long http2TransferTime;

  @SerializedName("https_transfer_time")
  private long httpsTransferTime;

  @SerializedName("https_tcpdump_packets_drop")
  private int httpsTcpdumpPacketsDrop;

  @SerializedName("http2_tcpdump_packets_drop")
  private int http2TcpdumpPacketsDrop;

  public MultiConnData(String targetUrl, long httpsResponseSize, long httpsRequestSize, long http2ResponseSize,
                       long http2RequestSize, long httpsResponseSizeTcp, long httpsRequestSizeTcp,
                       long http2ResponseSizeTcp, long http2RequestSizeTcp, JsonElement httpsTraces,
                       JsonElement http2Traces, long httpsTransferTime, long http2TransferTime,
                       Map<String, Long> httpsChannelRequestSize, Map<String, Long> httpsChannelResponseSize,
                       long httpsRequestSizeTcpTcpdump, long httpsResponseSizeTcpTcpdump,
                       long http2RequestSizeTcpTcpdump, long http2ResponseSizeTcpTcpdump,
                       int httpsTcpdumpPacketsDrop, int http2TcpdumpPacketsDrop) {
    this.targetUrl = targetUrl;
    this.httpsResponseSize = httpsResponseSize;
    this.httpsRequestSize = httpsRequestSize;
    this.http2ResponseSize = http2ResponseSize;
    this.http2RequestSize = http2RequestSize;
    this.httpsResponseSizeTcp = httpsResponseSizeTcp;
    this.httpsRequestSizeTcp = httpsRequestSizeTcp;
    this.http2ResponseSizeTcp = http2ResponseSizeTcp;
    this.http2RequestSizeTcp = http2RequestSizeTcp;
    this.httpsTraces = httpsTraces;
    this.http2Traces = http2Traces;
    this.httpsTransferTime = httpsTransferTime;
    this.http2TransferTime = http2TransferTime;
    this.httpsChannelRequestSize = httpsChannelRequestSize;
    this.httpsChannelResponseSize = httpsChannelResponseSize;
    this.httpsRequestSizeTcpTcpdump = httpsRequestSizeTcpTcpdump;
    this.httpsResponseSizeTcpTcpdump = httpsResponseSizeTcpTcpdump;
    this.http2RequestSizeTcpTcpdump = http2RequestSizeTcpTcpdump;
    this.http2ResponseSizeTcpTcpdump = http2ResponseSizeTcpTcpdump;
    this.httpsTcpdumpPacketsDrop = httpsTcpdumpPacketsDrop;
    this.http2TcpdumpPacketsDrop = http2TcpdumpPacketsDrop;

    timeStamp = System.currentTimeMillis();
    hostName = new ShellUtil().exec("hostname");
  }
}

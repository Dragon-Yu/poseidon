package fullweb;

import com.google.gson.annotations.SerializedName;

import java.net.URL;

/**
 * Created by Johnson on 16/7/25.
 */
public class TraceInfo {

  private transient long requestTimeStamp;
  private transient long responseTimeStamp;

  @SerializedName("channel_id")
  private String channelId;

  @SerializedName("time_elapsed")
  private long timeElapsed;

  @SerializedName("url")
  private URL url;

  public TraceInfo(URL url) {
    this.requestTimeStamp = System.nanoTime();
    this.url = url;
  }

  public TraceInfo(URL url, long requestTimeStamp) {
    this.requestTimeStamp = requestTimeStamp;
    this.url = url;
  }

  public TraceInfo(URL url, long requestTimeStamp, String channelId) {
    this.requestTimeStamp = requestTimeStamp;
    this.channelId = channelId;
    this.url = url;
  }

  public void setRequestTimeStamp(long requestTimeStamp) {
    this.requestTimeStamp = requestTimeStamp;
    timeElapsed = responseTimeStamp - requestTimeStamp;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public void setResponseTimeStamp(long responseTimeStamp) {
    this.responseTimeStamp = responseTimeStamp;
    timeElapsed = responseTimeStamp - requestTimeStamp;
  }

  public void finish(String channelId, long responseTimeStamp) {
    this.channelId = channelId;
    this.responseTimeStamp = responseTimeStamp;
    this.timeElapsed = responseTimeStamp - requestTimeStamp;
  }

  @Override
  public String toString() {
    return "TraceInfo{" +
      "channelId='" + channelId + '\'' +
      ", timeElapsed=" + timeElapsed +
      ", url=" + url +
      '}';
  }
}

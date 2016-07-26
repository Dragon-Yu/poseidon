package fullweb;

import com.google.gson.annotations.SerializedName;

import java.net.URL;

/**
 * Created by Johnson on 16/7/25.
 */
public class TraceInfo {

  private transient long requestTimeStamp;
  private transient long responseTimeStamp;

  @SerializedName("time_elapsed")
  private long timeElapsed;

  @SerializedName("url")
  private URL url;

  public TraceInfo(URL url) {
    this.url = url;
  }

  public void setRequestTimeStamp(long requestTimeStamp) {
    this.requestTimeStamp = requestTimeStamp;
    timeElapsed = responseTimeStamp - requestTimeStamp;
  }

  public void setResponseTimeStamp(long responseTimeStamp) {
    this.responseTimeStamp = responseTimeStamp;
    timeElapsed = responseTimeStamp - requestTimeStamp;
  }
}

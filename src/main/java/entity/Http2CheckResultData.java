package entity;

import com.google.gson.annotations.SerializedName;
import util.ShellUtil;

import java.util.Set;

/**
 * Store data of checking a host for http2 support
 * Created by Johnson on 16/6/18.
 */
public class Http2CheckResultData {

  String host;
  String source;
  boolean support;
  Set<String> errors;
  @SerializedName("time_stamp")
  long timeStamp;

  public Http2CheckResultData(String host, boolean support, Set<String> errors) {
    this.host = host;
    this.support = support;
    this.errors = errors;

    timeStamp = System.currentTimeMillis();
    source = new ShellUtil().exec("hostname");
  }
}

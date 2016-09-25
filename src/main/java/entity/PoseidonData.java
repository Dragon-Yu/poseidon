package entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Johnson on 16/9/13.
 */
public class PoseidonData {

  @SerializedName("http1_data")
  ExperimentData http1Data;
  @SerializedName("http2_data")
  ExperimentData http2Data;
  ConfigData config;
  @SerializedName("http2_unsupported")
  boolean http2Unsupported;
  String note = null;

  public PoseidonData(ExperimentData http1Data, ExperimentData http2Data, ConfigData config, boolean http2Unsupported) {
    this.http1Data = http1Data;
    this.http2Data = http2Data;
    this.config = config;
    this.http2Unsupported = http2Unsupported;
    this.note = "non_parallel";
  }
}

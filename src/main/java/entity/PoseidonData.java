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

  public PoseidonData(ExperimentData http1Data, ExperimentData http2Data, ConfigData config) {
    this.http1Data = http1Data;
    this.http2Data = http2Data;
    this.config = config;
  }
}

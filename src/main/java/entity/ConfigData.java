package entity;

import config.BaseTestConfig;
import util.ShellUtil;

/**
 * Created by Johnson on 16/9/13.
 */
public class ConfigData {

  private String targetUrl;
  private boolean ignoreOuterLink;
  private int channelPoolSize;
  private String hostName;

  public ConfigData() {
    this.ignoreOuterLink = BaseTestConfig.IGNORE_OUTER_LINK;
    this.targetUrl = BaseTestConfig.URI;
    this.channelPoolSize = BaseTestConfig.CHANNEL_POOL_SIZE;
    this.hostName = new ShellUtil().exec("hostname");
  }
}

package sonata.kernel.vimadaptor.wrapper.vlsp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppRequestData {

  @JsonProperty("aid")
  private int appID;
  @JsonProperty("id")
  private int reqId;
  @JsonProperty("name")
  private String appName;
  private int routerID;
  @JsonProperty("op_time")
  private long opTime;
  private boolean success;
  private String msg;

  public int getAppID() {
    return appID;
  }

  public int getReqId() {
    return reqId;
  }

  public String getAppName() {
    return appName;
  }

  public int getRouterID() {
    return routerID;
  }

  public long getOpTime() {
    return opTime;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setAppID(int appID) {
    this.appID = appID;
  }

  public void setReqId(int reqId) {
    this.reqId = reqId;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public void setRouterID(int routerID) {
    this.routerID = routerID;
  }

  public void setOpTime(long opTime) {
    this.opTime = opTime;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

}

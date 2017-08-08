package sonata.kernel.vimadaptor.wrapper.vlsp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinkData {

  
  private int linkID;
  private int router1;
  private int router2;
  private String linkName;
  private int weight;
  private long time;
  private String msg;
  private Boolean success;
  @JsonProperty("op_time")
  private long opTime;

  public int getLinkID() {
    return linkID;
  }

  public String getLinkName() {
    return linkName;
  }


  public int getWeight() {
    return weight;
  }

  public long getTime() {
    return time;
  }

  public void setLinkID(int linkID) {
    this.linkID = linkID;
  }

  public void setLinkName(String linkName) {
    this.linkName = linkName;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public String getMsg() {
    return msg;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  public long getOpTime() {
    return opTime;
  }

  public void setOpTime(long opTime) {
    this.opTime = opTime;
  }

  public int getRouter1() {
    return router1;
  }

  public int getRouter2() {
    return router2;
  }

  public void setRouter1(int router1) {
    this.router1 = router1;
  }

  public void setRouter2(int router2) {
    this.router2 = router2;
  }


}

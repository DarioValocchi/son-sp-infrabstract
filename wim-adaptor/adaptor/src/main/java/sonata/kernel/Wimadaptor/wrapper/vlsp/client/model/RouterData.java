package sonata.kernel.Wimadaptor.wrapper.vlsp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RouterData {

  

  private int[] linkIDs;
  private int[] links;
  private int routerID;
  private String address;
  private String name;
  private int mgmtPort;
  private String r2rPort;
  private long time;
  private String msg;
  private Boolean success;
  @JsonProperty("op_time")
  private long opTime;
  
  public int getRouterID() {
    return routerID;
  }

  public String getAddress() {
    return address;
  }

  public String getName() {
    return name;
  }

  public int getMgmtPort() {
    return mgmtPort;
  }

  public String getR2rPort() {
    return r2rPort;
  }

  public long getTime() {
    return time;
  }

  public void setRouterID(int routerID) {
    this.routerID = routerID;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMgmtPort(int mgmtPort) {
    this.mgmtPort = mgmtPort;
  }

  public void setR2rPort(String r2rPort) {
    this.r2rPort = r2rPort;
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

  public int[] getLinkIDs() {
    return linkIDs;
  }

  public int[] getLinks() {
    return links;
  }

  public void setLinkIDs(int[] linkIDs) {
    this.linkIDs = linkIDs;
  }

  public void setLinks(int[] links) {
    this.links = links;
  }



}

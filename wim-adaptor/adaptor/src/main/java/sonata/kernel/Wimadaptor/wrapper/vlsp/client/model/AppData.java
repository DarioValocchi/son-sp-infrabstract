package sonata.kernel.Wimadaptor.wrapper.vlsp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppData {
  
  @JsonProperty("aid")
  private int appID;
  private String appName;
  @JsonProperty("classname")
  private String nameOfClass;
  private String[] args;
  private int routerID;
  private long starttime;
  private long runtime;

  public int getAppID() {
    return appID;
  }

  public String getAppName() {
    return appName;
  }

  public String getNameOfClass() {
    return nameOfClass;
  }

  public String[] getArgs() {
    return args;
  }

  public int getRouterID() {
    return routerID;
  }

  public long getStarttime() {
    return starttime;
  }

  public long getRuntime() {
    return runtime;
  }

  public void setAppID(int appID) {
    this.appID = appID;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public void setNameOfClass(String nameOfClass) {
    this.nameOfClass = nameOfClass;
  }

  public void setArgs(String[] args) {
    this.args = args;
  }

  public void setRouterID(int routerID) {
    this.routerID = routerID;
  }

  public void setStarttime(long starttime) {
    this.starttime = starttime;
  }

  public void setRuntime(long runtime) {
    this.runtime = runtime;
  }
}

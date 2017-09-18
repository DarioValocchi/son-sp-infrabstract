package sonata.kernel.vimadaptor.wrapper.vlsp.client.model;

public class SliceCtrlResponse {

  private String message;
  private SlicePayload payload;
  private long timestamp;
  
  public String getMessage() {
    return message;
  }
  public SlicePayload getPayload() {
    return payload;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public void setPayload(SlicePayload payload) {
    this.payload = payload;
  }
  public long getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
  
}

package sonata.kernel.vimadaptor.wrapper.vlsp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SlicePayload {

  private long id;
  @JsonProperty("size")
  private int sliceSize;
  private String type;
  private VimInformation vim;

  public long getId() {
    return id;
  }

  public int getSliceSize() {
    return sliceSize;
  }

  public String getType() {
    return type;
  }

  public VimInformation getVim() {
    return vim;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setSliceSize(int sliceSize) {
    this.sliceSize = sliceSize;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setVim(VimInformation vim) {
    this.vim = vim;
  }


}

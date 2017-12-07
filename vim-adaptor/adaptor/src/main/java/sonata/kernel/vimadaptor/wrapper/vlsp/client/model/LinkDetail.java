package sonata.kernel.vimadaptor.wrapper.vlsp.client.model;

public class LinkDetail {

  private int[] ends;
  private int id;
  private String name;
  private int weight;
  private int port;

  public int[] getEnds() {
    return ends;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getWeight() {
    return weight;
  }

  public int getPort() {
    return port;
  }

  public void setEnds(int[] ends) {
    this.ends = ends;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public void setPort(int port) {
    this.port = port;
  }
}

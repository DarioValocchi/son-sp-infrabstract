package sonata.kernel.vimadaptor.wrapper.vlsp.client;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.RouterData;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.SliceCtrlResponse;

public class VlspSliceClient {

  private String host;
  private int port;

  public VlspSliceClient(String host, int port) {
    this.host = host;
    this.port = port;
  }


  public SliceCtrlResponse getSlice(String type, int size) throws JsonParseException, JsonMappingException, IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost post;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append(":");
    buildUrl.append(this.port);
    buildUrl.append("/slice");

    String body = "{\"type\":\"" + type + "\",\"size\":\"" + size + "\"}";

    post = new HttpPost(buildUrl.toString());
    post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
    // Logger.debug("[VlspGcCLient] " + post.toString());

    response = httpClient.execute(post);

    ObjectMapper mapper = new ObjectMapper();


    SliceCtrlResponse output =
        mapper.readValue(VlspClientUtils.convertHttpResponseToString(response), SliceCtrlResponse.class);
    return output;

  }


}

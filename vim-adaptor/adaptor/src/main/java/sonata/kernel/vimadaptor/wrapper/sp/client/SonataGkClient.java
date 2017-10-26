package sonata.kernel.vimadaptor.wrapper.sp.client;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import sonata.kernel.vimadaptor.commons.VimResources;
import sonata.kernel.vimadaptor.wrapper.ResourceUtilisation;
import sonata.kernel.vimadaptor.wrapper.sp.client.model.SonataAuthenticationResponse;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.VlspClientUtils;


public class SonataGkClient {

  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(SonataGkClient.class);

  private String host;
  private String username;
  private String password;
  private String token;

  public SonataGkClient(String host, String username, String password) {
    this.host = host;
    this.username = username;
    this.password = password;
  }

  public boolean authenticate(){

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost post;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append(":");
    buildUrl.append(32001);
    buildUrl.append("/api/v2/sessions");

    String body = "{\"username\":\"" + this.username + "\",\"password\":\"" + this.password + "\"}";

    post = new HttpPost(buildUrl.toString());

    post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
    try{
    response = httpClient.execute(post);

    String json = VlspClientUtils.convertHttpResponseToString(response);

    ObjectMapper mapper = new ObjectMapper();

    SonataAuthenticationResponse auth = mapper.readValue(json, SonataAuthenticationResponse.class);
    this.token = auth.getToken().getToken();

    if (response.getStatusLine().getStatusCode() == 200)
      return true;
    else
      return false;
    }catch (IOException e) {
      return false;
    }
  }

  public VimResources[] getPoP() throws ClientProtocolException, IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet get;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append("/api/v2/vims");

    get = new HttpGet(buildUrl.toString());

    get.addHeader("Authorization:Bearer", this.token);
    response = httpClient.execute(get);

    Logger.debug("[SONATA-GK-CLient] VIM endpoint response:");
    Logger.debug(response.toString());

    ObjectMapper mapper = new ObjectMapper();

    String stringResponse = VlspClientUtils.convertHttpResponseToString(response);

    VimResources[] list = mapper.readValue(stringResponse, VimResources[].class);

    return list;

  }
}

package sonata.kernel.WimAdaptor.wrapper.sp.client;

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

import sonata.kernel.WimAdaptor.commons.WimRecord;
import sonata.kernel.WimAdaptor.wrapper.sp.client.model.SonataAuthenticationResponse;
import sonata.kernel.WimAdaptor.wrapper.sp.client.model.VimRequestStatus;
import sonata.kernel.Wimadaptor.wrapper.vlsp.client.VlspClientUtils;


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

  public boolean authenticate() {

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
    Logger.debug("Authentication request:");

    Logger.debug(post.toString());

    try {
      response = httpClient.execute(post);

      String json = VlspClientUtils.convertHttpResponseToString(response);

      Logger.debug("Auth response: " + json);
      ObjectMapper mapper = new ObjectMapper();

      SonataAuthenticationResponse auth =
          mapper.readValue(json, SonataAuthenticationResponse.class);
      this.token = auth.getToken().getToken();

      if (response.getStatusLine().getStatusCode() == 200) {
        Logger.debug("Client authenticated");
        return true;
      } else {
        Logger.debug("Authentication failed");
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

    public WimRecord[] getWims() throws ClientProtocolException, IOException {
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet get;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append("/api/v2/wims");

    get = new HttpGet(buildUrl.toString());

    get.addHeader("Authorization:Bearer", this.token);
    response = httpClient.execute(get);

    Logger.debug("[SONATA-GK-CLient] /wim endpoint response (Request Object):");
    Logger.debug(response.toString());

    ObjectMapper mapper = new ObjectMapper();

    String stringResponse = VlspClientUtils.convertHttpResponseToString(response);
    Logger.debug(stringResponse);

    VimRequestStatus requestStatus = mapper.readValue(stringResponse, VimRequestStatus.class);

    if (requestStatus.getStatus() != 201) {
      throw new ClientProtocolException(
          "GK returned wrong status upon VIM request creation: " + requestStatus.getStatus());
    }
    String requestUuid = "";
    try {
      requestUuid = requestStatus.getItems().getRequestUuid();
    } catch (NullPointerException e) {
      throw new IOException(
          "The GK sent back an request status with empty values or values are not parsed correctly.");
    }
    WimRecord[] list;
    do {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      buildUrl = new StringBuilder();
      buildUrl.append("http://");
      buildUrl.append(this.host);
      buildUrl.append("/api/v2/wims");
      buildUrl.append("/" + requestUuid);

      get = new HttpGet(buildUrl.toString());

      get.addHeader("Authorization:Bearer", this.token);
      response = httpClient.execute(get);

      Logger.debug("[SONATA-GK-CLient] /wim endpoint response (WIM list):");
      Logger.debug(response.toString());
      stringResponse = VlspClientUtils.convertHttpResponseToString(response);
      Logger.debug(stringResponse);

      list = mapper.readValue(stringResponse, WimRecord[].class);
    } while (stringResponse.equals("{}"));

    return list;

  }
}

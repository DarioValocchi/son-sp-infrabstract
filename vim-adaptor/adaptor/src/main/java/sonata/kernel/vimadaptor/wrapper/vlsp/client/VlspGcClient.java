package sonata.kernel.vimadaptor.wrapper.vlsp.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.AppData;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.LinkData;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.RouterData;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.RouterList;

public class VlspGcClient {

  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(VlspGcClient.class);

  private String host;
  private int port;

  public VlspGcClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public RouterData getRouter(int id) throws ClientProtocolException, IOException{
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet get;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append(":");
    buildUrl.append(this.port);
    buildUrl.append("/router");
    buildUrl.append("/"+id);

    get = new HttpGet(buildUrl.toString());
    
    response = httpClient.execute(get);

    Logger.debug("[VlspGcCLient] Router list response:");
    Logger.debug(response.toString());

    ObjectMapper mapper = new ObjectMapper();


    RouterData router=
        mapper.readValue(VlspClientUtils.convertHttpResponseToString(response), RouterData.class);
    
    return router;

  }
  
  public int[] listRouters() throws ClientProtocolException, IOException{
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet get;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append(":");
    buildUrl.append(this.port);
    buildUrl.append("/router/");

    get = new HttpGet(buildUrl.toString());
    
    response = httpClient.execute(get);

    Logger.debug("[VlspGcCLient] Router list response:");
    Logger.debug(response.toString());

    ObjectMapper mapper = new ObjectMapper();


    RouterList routers=
        mapper.readValue(VlspClientUtils.convertHttpResponseToString(response), RouterList.class);
        
    return routers.getList();
    
  }
  
  public RouterData addRouter(String name, Integer address)
      throws ClientProtocolException, IOException {

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost post;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append(":");
    buildUrl.append(this.port);
    buildUrl.append("/router");
    buildUrl.append("/?name=" + name);
    buildUrl.append("&address=" + address);

    post = new HttpPost(buildUrl.toString());

    Logger.debug("[VlspGcCLient] Creating router...");
    Logger.debug("[VlspGcCLient] " + post.toString());

    response = httpClient.execute(post);

    Logger.debug("[VlspGcCLient] Router creation response:");
    Logger.debug(response.toString());

    ObjectMapper mapper = new ObjectMapper();


    RouterData output =
        mapper.readValue(VlspClientUtils.convertHttpResponseToString(response), RouterData.class);
    return output;

  }

  public LinkData addLink(int router1, int router2, Integer weight, String linkName)
      throws ClientProtocolException, IOException {

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost post;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append(":");
    buildUrl.append(this.port);
    buildUrl.append("/link");
    buildUrl.append("/?router1=" + router1);
    buildUrl.append("&router2=" + router2);
    if (weight != null) buildUrl.append("&weight=" + weight);
    if (linkName != null) buildUrl.append("&linkName=" + linkName);

    post = new HttpPost(buildUrl.toString());

    Logger.debug("[VlspGcCLient] Creating link...");
    Logger.debug("[VlspGcCLient] " + post.toString());

    response = httpClient.execute(post);

    Logger.debug("[VlspGcCLient] Link creation response:");
    Logger.debug(response.toString());

    ObjectMapper mapper = new ObjectMapper();

    LinkData output =
        mapper.readValue(VlspClientUtils.convertHttpResponseToString(response), LinkData.class);
    return output;

  }


  public AppData deployApp(Integer routerId, String appClassPath, String[] args)
      throws ClientProtocolException, IOException {

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost post;
    HttpResponse response = null;

    StringBuilder buildUrl = new StringBuilder();
    buildUrl.append("http://");
    buildUrl.append(this.host);
    buildUrl.append(":");
    buildUrl.append(this.port);
    buildUrl.append("/router");
    buildUrl.append("/" + routerId);
    buildUrl.append("/app");
    buildUrl.append("/?className=" + appClassPath);
    buildUrl.append("&args=");
    for (int i = 0; i < args.length - 1; i++)
      buildUrl.append(args[i] + "%20");
    buildUrl.append(args[args.length - 1]);

    post = new HttpPost(buildUrl.toString());

    Logger.debug("[VlspGcCLient] Creating link...");
    Logger.debug("[VlspGcCLient] " + post.toString());

    response = httpClient.execute(post);

    Logger.debug("[VlspGcCLient] Link creation response:");
    Logger.debug(response.toString());

    ObjectMapper mapper = new ObjectMapper();

    AppData output =
        mapper.readValue(VlspClientUtils.convertHttpResponseToString(response), AppData.class);
    return output;
  }



}

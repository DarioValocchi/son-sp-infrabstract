package sonata.kernel.WimAdaptor.wrapper.vlsp;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

import sonata.kernel.WimAdaptor.commons.vnfd.VnfDescriptor;
import sonata.kernel.WimAdaptor.wrapper.WimWrapper;
import sonata.kernel.WimAdaptor.wrapper.WrapperConfiguration;
import sonata.kernel.WimAdaptor.wrapper.WrapperStatusUpdate;
import sonata.kernel.Wimadaptor.wrapper.vlsp.client.VlspGcClient;
import sonata.kernel.Wimadaptor.wrapper.vlsp.client.model.RouterData;

public class VlspWimWrapper extends WimWrapper {

  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(VlspWimWrapper.class);

  public VlspWimWrapper(WrapperConfiguration config) {
    super(config);
  }

  @Override
  public boolean configureNetwork(String instanceId, String inputSegment, String outputSegment,
      String[] segmentList) {

    Logger.debug("Configuring SFC in VLSP...");
    String host = this.getConfig().getWimEndpoint();
    String config = this.getConfig().getConfiguration();
//    JSONTokener mapper = new JSONTokener(config);
//    JSONObject object = (JSONObject) mapper.nextValue();
//    if (!object.has("GC_port")) {
//      Logger.error("VLSP wrapper - Unable get GC port from VIM configuration");
//      this.setChanged();
//      WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(null, "ERROR",
//          "VLSP wrapper - Unable get GC port from VIM configuration");
//      this.notifyObservers(errorUpdate);
//      return false;
//    }
//    int port = object.getInt("GC_port");

    VlspGcClient client = new VlspGcClient(host, 8888);

    String routerInName = instanceId + "_ingress";
    String routerOutName = instanceId + "_egress";
    int routerInId = -1;
    int routerOutId = -1;
    try {
      int[] routerIds = client.listRouters();

      for (int id : routerIds) {
        RouterData router = client.getRouter(id);
        String name = router.getName();
        Logger.debug("router name:" + name);
        if (name.equals(routerInName)) {
          routerInId = id;
        }
        if (name.equals(routerOutName)) {
          routerOutId = id;
        }
      }

      if (routerInId < 0 || routerOutId < 0) {
        Logger.error("VLSP WIM wrapper - Cannot retrieve router ID of ingress/egress routers.");
        return false;
      }
      String[] inArgs = new String[5];
      inArgs[0] = "4000";
      inArgs[1] = inputSegment+":8856";
      inArgs[2] = "-v";
      inArgs[3] = "-b";
      inArgs[4] = "64";
      
      
      
      String[] outArgs = new String[3];
      outArgs[0] = "4000";
      outArgs[1] = outputSegment+":8856";
      outArgs[2] = "-v";

      client.deployApp(routerInId, "demo_usr.paths.Egress", inArgs);
      client.deployApp(routerOutId, "demo_usr.paths.Egress", outArgs);

    } catch (

    ClientProtocolException e) {
      e.printStackTrace();
      Logger.error(
          "VLSP WIM wrapper - Exception rised by REST client for protocol error while creating link.");
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      Logger.error(
          "VLSP WIM wrapper - Exception rised by REST client for I/O error while creating link.");
      return false;
    }


    return true;
  }

  @Override
  public boolean removeNetConfiguration(String instanceId) {
    // TODO Auto-generated method stub
    return false;
  }

}

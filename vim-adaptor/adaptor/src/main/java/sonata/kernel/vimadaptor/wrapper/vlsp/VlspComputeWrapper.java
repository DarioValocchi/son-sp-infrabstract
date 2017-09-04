package sonata.kernel.vimadaptor.wrapper.vlsp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

import sonata.kernel.vimadaptor.commons.FunctionDeployPayload;
import sonata.kernel.vimadaptor.commons.FunctionScalePayload;
import sonata.kernel.vimadaptor.commons.ServiceDeployPayload;
import sonata.kernel.vimadaptor.commons.VnfImage;
import sonata.kernel.vimadaptor.commons.nsd.ConnectionPoint;
import sonata.kernel.vimadaptor.commons.nsd.VirtualLink.ConnectivityType;
import sonata.kernel.vimadaptor.commons.vnfd.VirtualDeploymentUnit;
import sonata.kernel.vimadaptor.commons.vnfd.VnfDescriptor;
import sonata.kernel.vimadaptor.commons.vnfd.VnfVirtualLink;
import sonata.kernel.vimadaptor.wrapper.ComputeWrapper;
import sonata.kernel.vimadaptor.wrapper.ResourceUtilisation;
import sonata.kernel.vimadaptor.wrapper.WrapperConfiguration;
import sonata.kernel.vimadaptor.wrapper.WrapperStatusUpdate;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.VlspGcClient;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.LinkData;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.RouterData;

public class VlspComputeWrapper extends ComputeWrapper {

  
  public VlspComputeWrapper(WrapperConfiguration config){
    super(config);
  }
  
  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(VlspComputeWrapper.class);

  @Override
  public void deployFunction(FunctionDeployPayload data, String sid) {
    Logger.debug("Deploying a VNF on VLSP...");
    HashMap<String, RouterData> vduToRouterDataMap = new HashMap<String, RouterData>();
    String host = this.getConfig().getVimEndpoint();
    String config = this.getConfig().getConfiguration();
    JSONTokener mapper = new JSONTokener(config);
    JSONObject object = (JSONObject) mapper.nextValue();
    VnfDescriptor vnfd = data.getVnfd();
    if (!object.has("GC_port")) {
      Logger.error("VLSP wrapper - Unable get GC port from VIM configuration");
      this.setChanged();
      WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR",
          "VLSP wrapper - Unable get GC port from VIM configuration");
      this.notifyObservers(errorUpdate);
      return;
    }
    int port = object.getInt("GC_port");
    VlspGcClient client = new VlspGcClient(host, port);
    // Deploy the VDUs
    Logger.debug("Creating virtual nodes to host VNFCs...");
    for (VirtualDeploymentUnit vdu : data.getVnfd().getVirtualDeploymentUnits()) {
      // For each VDU create a router
      Logger.debug("Creating virtual node for VDU_ID " + vdu.getId());
      String name = "SonataService" + data.getServiceInstanceId() + "_" + vnfd.getVendor() + "_"
          + vnfd.getName() + "_" + vnfd.getVersion() + "_" + vdu.getId();
      Logger.debug("Virtual node name: " + name);
      int address = new Random().nextInt();
      RouterData routerData;
      try {
        routerData = client.addRouter(name, address);
        vduToRouterDataMap.put(vdu.getId(), routerData);
      } catch (ClientProtocolException e) {
        e.printStackTrace();
        Logger.error(
            "VLSP wrapper - Exception rised by REST client for protocol error while creating router.");
        this.setChanged();
        WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR", e.getMessage());
        this.notifyObservers(errorUpdate);
        return;
      } catch (IOException e) {
        e.printStackTrace();
        Logger.error(
            "VLSP wrapper - Exception rised by REST client for I/O error while creating router.");
        this.setChanged();
        WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR", e.getMessage());
        this.notifyObservers(errorUpdate);
        return;
      }
    }
    Logger.debug("DONE");
    Logger.debug("Creating virtual links...");
    ArrayList<String> vnfCpNames = new ArrayList<String>();
    for (ConnectionPoint vnfCp : vnfd.getConnectionPoints()) {
      vnfCpNames.add(vnfCp.getId());
    }
    for (VnfVirtualLink link : vnfd.getVirtualLinks()) {
      Logger.debug("Checking if the link is an external one...");
      boolean isExtLink = false;
      for (String cpRef : link.getConnectionPointsReference()) {
        if (vnfCpNames.contains(cpRef)) {
          isExtLink = true;
          break;
        }
      }
      Logger.debug("is it? "+isExtLink);
      if (isExtLink) continue;
      int numberOfVdus = link.getConnectionPointsReference().size();
      if (link.getConnectivityType().equals(ConnectivityType.E_LINE)) {
        // E-LINE implies a single link between two VNFCs
        assert numberOfVdus == 2;
        String cp1 = link.getConnectionPointsReference().get(0);
        String cp2 = link.getConnectionPointsReference().get(1);

        String vdu1 = cp1.split(":")[0];
        String vdu2 = cp2.split(":")[0];
        Logger.debug("Deploying an E_LINE link between "+vdu1+" and "+vdu2);

        RouterData router1 = vduToRouterDataMap.get(vdu1);
        RouterData router2 = vduToRouterDataMap.get(vdu2);
        String linkName = "SonataService" + data.getServiceInstanceId() + "_" + vnfd.getVendor()
            + "_" + vnfd.getName() + "_" + vnfd.getVersion() + "_" + link.getId();
        try {
          LinkData vlspLink =
              client.addLink(router1.getRouterID(), router2.getRouterID(), null, linkName);
        } catch (ClientProtocolException e) {
          e.printStackTrace();
          Logger.error(
              "VLSP wrapper - Exception rised by REST client for protocol error while creating link.");
          this.setChanged();
          WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR", e.getMessage());
          this.notifyObservers(errorUpdate);
          return;
        } catch (IOException e) {
          e.printStackTrace();
          Logger.error(
              "VLSP wrapper - Exception rised by REST client for I/O error while creating link.");
          this.setChanged();
          WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR", e.getMessage());
          this.notifyObservers(errorUpdate);
          return;
        }

      } else if (link.getConnectivityType().equals(ConnectivityType.E_LAN)) {
        // E_LAN imply a full mesh between all connected VNFCs
        Logger.debug("Deploying an E_LAN link. Full mesh between VNFCs.");
        String[] vdus = new String[numberOfVdus];
        for (int i = 0; i < numberOfVdus; i++) {
          vdus[i] = link.getConnectionPointsReference().get(i).split(":")[0];
        }
        for (int i = 0; i < numberOfVdus; i++) {
          for (int j = i + 1; j < numberOfVdus; j++) {
            if (!(vduToRouterDataMap.containsKey(vdus[i])
                && vduToRouterDataMap.containsKey(vdus[j]))) {
              Logger.error("VLSP wrapper - mapping error. can't find VNFC connected by link: "
                  + link.getId());
              this.setChanged();
              WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR",
                  "VLSP wrapper - mapping error. can't find VNFC connected by link: "
                      + link.getId());
              this.notifyObservers(errorUpdate);
              return;
            }
            int router1 = vduToRouterDataMap.get(vdus[i]).getRouterID();
            int router2 = vduToRouterDataMap.get(vdus[j]).getRouterID();
            String linkName = "SonataService" + data.getServiceInstanceId() + "_" + vnfd.getVendor()
                + "_" + vnfd.getName() + "_" + vnfd.getVersion() + "_" + link.getId();

            try {
              client.addLink(router1, router2, null, linkName);
            } catch (ClientProtocolException e) {
              e.printStackTrace();
              Logger.error(
                  "VLSP wrapper - Exception rised by REST client for protocol error while creating link.");
              this.setChanged();
              WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR", e.getMessage());
              this.notifyObservers(errorUpdate);
              return;
            } catch (IOException e) {
              e.printStackTrace();
              Logger.error(
                  "VLSP wrapper - Exception rised by REST client for I/O error while creating link.");
              this.setChanged();
              WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(sid, "ERROR", e.getMessage());
              this.notifyObservers(errorUpdate);
              return;
            }
          }
        }


      }
    }

  }

  @Override
  public boolean deployService(ServiceDeployPayload data, String callSid) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ResourceUtilisation getResourceUtilisation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isImageStored(VnfImage image, String callSid) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean prepareService(String instanceId) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean removeService(String instanceUuid, String callSid) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void scaleFunction(FunctionScalePayload data, String sid) {
    // TODO Auto-generated method stub

  }

  @Override
  public void uploadImage(VnfImage image) throws IOException {
    // TODO Auto-generated method stub

  }

}

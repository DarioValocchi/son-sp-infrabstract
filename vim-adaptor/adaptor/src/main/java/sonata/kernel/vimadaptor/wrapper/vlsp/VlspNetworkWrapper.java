package sonata.kernel.vimadaptor.wrapper.vlsp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

import sonata.kernel.vimadaptor.commons.NetworkConfigurePayload;
import sonata.kernel.vimadaptor.commons.nsd.ForwardingGraph;
import sonata.kernel.vimadaptor.commons.nsd.NetworkForwardingPath;
import sonata.kernel.vimadaptor.commons.nsd.NetworkFunction;
import sonata.kernel.vimadaptor.commons.nsd.ServiceDescriptor;
import sonata.kernel.vimadaptor.commons.nsd.VirtualLink;
import sonata.kernel.vimadaptor.commons.nsd.VirtualLink.ConnectivityType;
import sonata.kernel.vimadaptor.commons.vnfd.ConnectionPointReference;
import sonata.kernel.vimadaptor.commons.vnfd.VnfDescriptor;
import sonata.kernel.vimadaptor.commons.vnfd.VnfVirtualLink;
import sonata.kernel.vimadaptor.wrapper.NetworkWrapper;
import sonata.kernel.vimadaptor.wrapper.WrapperConfiguration;
import sonata.kernel.vimadaptor.wrapper.WrapperStatusUpdate;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.VlspGcClient;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.LinkData;
import sonata.kernel.vimadaptor.wrapper.vlsp.client.model.RouterData;

public class VlspNetworkWrapper extends NetworkWrapper {

  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(VlspNetworkWrapper.class);


  public VlspNetworkWrapper(WrapperConfiguration config) {
    super(config);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void configureNetworking(NetworkConfigurePayload data) throws Exception {

    Logger.debug("Configuring SFC in VLSP...");
    String host = this.getConfig().getVimEndpoint();
    String config = this.getConfig().getConfiguration();
    JSONTokener mapper = new JSONTokener(config);
    JSONObject object = (JSONObject) mapper.nextValue();
    if (!object.has("GC_port")) {
      Logger.error("VLSP wrapper - Unable get GC port from VIM configuration");
      this.setChanged();
      WrapperStatusUpdate errorUpdate = new WrapperStatusUpdate(null, "ERROR",
          "VLSP wrapper - Unable get GC port from VIM configuration");
      this.notifyObservers(errorUpdate);
      return;
    }
    int port = object.getInt("GC_port");

    VlspGcClient client = new VlspGcClient(host, port);

    ArrayList<VnfDescriptor> vnfds = data.getVnfds();

    int[] routerIds = client.listRouters();
    HashMap<String, RouterData> name2RouterMap = new HashMap<String, RouterData>();
    for (int id : routerIds) {
      RouterData router = client.getRouter(id);
      String name = router.getName();
      for (VnfDescriptor vnfd : vnfds) {
        String vnfPrefix = vnfd.getName();
        Logger.debug("VNF prefix:" + vnfPrefix);
        Logger.debug("router name:" + name);
        if (name.startsWith(vnfPrefix)) {
          name2RouterMap.put(name, router);
        }
      }
    }

    Logger.debug("Retrieved routers:");
    for (RouterData router : name2RouterMap.values()) {
      Logger.debug(router.getName());
    }

    ServiceDescriptor nsd = data.getNsd();

    // Finish graph with NS virtual links
    HashMap<String, String> vnfId2vnfNameMap = new HashMap<String, String>();
    for (NetworkFunction vnf : nsd.getNetworkFunctions()) {
      vnfId2vnfNameMap.put(vnf.getVnfId(),
          vnf.getVnfVendor() + ":" + vnf.getVnfName() + ":" + vnf.getVnfVersion());
    }

    HashMap<String, VnfDescriptor> vnfTrio2VnfdMap = new HashMap<String, VnfDescriptor>();
    for (VnfDescriptor vnf : vnfds) {
      vnfTrio2VnfdMap.put(vnf.getVendor() + ":" + vnf.getName() + ":" + vnf.getVersion(), vnf);
    }
    
    for (VirtualLink vl : nsd.getVirtualLinks()) {
      if (vl.getConnectivityType().equals(ConnectivityType.E_LAN)) {
        // TODO
      } else if (vl.getConnectivityType().equals(ConnectivityType.E_LINE)) {

        String[] linkEnds = new String[2];
        
        if (vl.getConnectionPointsReference().contains("in")) {
          linkEnds[0] = data.getServiceInstanceId() + "_ingress";
        } else if (vl.getConnectionPointsReference().contains("out")) {
          linkEnds[1] = data.getServiceInstanceId() + "_egress";
        } else {
          int i = 0;
          for (String cpRef : vl.getConnectionPointsReference()) {
            // Process the cp reference
            Logger.debug("Processing vnf CP" + cpRef);
            String[] split = cpRef.split(":");
            String vnfId = split[0];
            String cpName = split[1];

            // Resolve VNF triple and vnf descriptor.
            String vnfTrio = vnfId2vnfNameMap.get(vnfId);
            if (vnfTrio == null) {
              Logger.error("Unable to map the vnfId in the cpRef to a proper VNF: " + cpRef);
              return;
            }
            VnfDescriptor vnf = vnfTrio2VnfdMap.get(vnfTrio);
            // find VL connecting VNF_CP
            for (VnfVirtualLink vnfVl : vnf.getVirtualLinks()) {
              if (vnfVl.getConnectionPointsReference().contains(cpName)) {
                // This must be an E_LINE with just two CPs.
                Logger.debug("Virtual link found:" + vnfVl.getId());
                int indexOfVnfCp = vnfVl.getConnectionPointsReference().indexOf(cpName);
                int indexOfVduCp = (indexOfVnfCp + 1) % 2;
                String vlEnd = vl.getConnectionPointsReference().get(indexOfVduCp);
                Logger.debug("Vl end found: " + vlEnd);
                split = vlEnd.split(":");
                String vduId = split[0];
                linkEnds[i] = vnf.getName() + "_" + vduId;
                i++;
                break;
              }
            }
          }
        }
        //Create the link between the router which represents the link ends.
        if(!(name2RouterMap.containsKey(linkEnds[0])&&name2RouterMap.containsKey(linkEnds[1]))){
          Logger.error("Cannot find the router connected to this link in the router map.");
          Logger.error("link ends were "+linkEnds[0]+" and "+linkEnds[1]);
          Logger.error("name2RouterMap: "+name2RouterMap.toString());
          return;
        }
        RouterData router1 = name2RouterMap.get(linkEnds[0]);
        RouterData router2 = name2RouterMap.get(linkEnds[1]);
        try {
          LinkData vlspLink =
              client.addLink(router1.getRouterID(), router2.getRouterID(), null, vl.getId());
        } catch (ClientProtocolException e) {
          e.printStackTrace();
          Logger.error(
              "VLSP wrapper - Exception rised by REST client for protocol error while creating link.");
          return;
        } catch (IOException e) {
          e.printStackTrace();
          Logger.error(
              "VLSP wrapper - Exception rised by REST client for I/O error while creating link.");
          return;
        }
      }
    }
    
    // TODO Enforce SFC path
    ArrayList<ForwardingGraph> graphs = nsd.getForwardingGraphs();
    for (ForwardingGraph graph : graphs) {
      ArrayList<NetworkForwardingPath> paths = graph.getNetworkForwardingPaths();
      for (NetworkForwardingPath path : paths) {
        ArrayList<String> router = this.resolvePath(path, vnfds, nsd);
      }
    }

  }

  private ArrayList<String> resolvePath(NetworkForwardingPath path, ArrayList<VnfDescriptor> vnfds,
      ServiceDescriptor nsd) {

    HashMap<String, String> vnfId2vnfNameMap = new HashMap<String, String>();
    for (NetworkFunction vnf : nsd.getNetworkFunctions()) {
      vnfId2vnfNameMap.put(vnf.getVnfId(),
          vnf.getVnfVendor() + ":" + vnf.getVnfName() + ":" + vnf.getVnfVersion());
    }

    HashMap<String, VnfDescriptor> vnfTrio2VnfdMap = new HashMap<String, VnfDescriptor>();
    for (VnfDescriptor vnf : vnfds) {
      vnfTrio2VnfdMap.put(vnf.getVendor() + ":" + vnf.getName() + ":" + vnf.getVersion(), vnf);
    }


    for (ConnectionPointReference cpRef : path.getConnectionPoints()) {
      if (!cpRef.getConnectionPointRef().contains(":")) continue;
      // Process the cp reference
      String[] split = cpRef.getConnectionPointRef().split(":");
      String vnfId = split[0];
      String cpName = split[1];

      // Resolve VNF triple and vnf descriptor.
      String vnfTrio = vnfId2vnfNameMap.get(vnfId);
      if (vnfTrio == null) {
        Logger.error("Unable to map the vnfId in the cpRef to a proper VNF: " + cpRef);
        return null;
      }
      VnfDescriptor vnf = vnfTrio2VnfdMap.get(vnfTrio);
      // find VL connecting VNF_CP

      for (VnfVirtualLink vl : vnf.getVirtualLinks()) {
        if (vl.getConnectionPointsReference().contains(cpName)) {
          // This must be an E_LINE with just two CPs.
          int indexOfVnfCp = vl.getConnectionPointsReference().indexOf(cpName);
          int indexOfVlEnd = (indexOfVnfCp + 1) % 2;
          String vlEnd = vl.getConnectionPointsReference().get(indexOfVlEnd);
          split = vlEnd.split(":");
          String vduId = split[0];
          String vduCpName = split[1];
        }
      }
      // TODO incomplete method, to be finished when SFC is implemented/sorted out in VLSP
    }

    return null;
  }

  @Override
  public void deconfigureNetworking(String instanceId) throws Exception {
    // TODO Auto-generated method stub

  }

}

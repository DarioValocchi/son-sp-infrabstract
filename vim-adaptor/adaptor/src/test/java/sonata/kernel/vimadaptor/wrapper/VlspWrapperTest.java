package sonata.kernel.vimadaptor.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import sonata.kernel.vimadaptor.commons.FunctionDeployPayload;
import sonata.kernel.vimadaptor.commons.ServiceDeployPayload;
import sonata.kernel.vimadaptor.commons.SonataManifestMapper;
import sonata.kernel.vimadaptor.commons.nsd.ServiceDescriptor;
import sonata.kernel.vimadaptor.commons.vnfd.VnfDescriptor;
import sonata.kernel.vimadaptor.wrapper.vlsp.VlspComputeWrapper;

public class VlspWrapperTest {

  VlspComputeWrapper computeWrapper;
  private ServiceDeployPayload data;
  private ObjectMapper mapper;


  @Before
  public void setUp() throws Exception {

    ServiceDescriptor sd;
    StringBuilder bodyBuilder = new StringBuilder();
    BufferedReader in = new BufferedReader(new InputStreamReader(
        new FileInputStream(new File("./YAML/vlsp/long-chain-demo.nsd")), Charset.forName("UTF-8")));
    String line;
    while ((line = in.readLine()) != null)
      bodyBuilder.append(line + "\n\r");
    this.mapper = SonataManifestMapper.getSonataMapper();

    sd = mapper.readValue(bodyBuilder.toString(), ServiceDescriptor.class);

    VnfDescriptor vnfd1;
    bodyBuilder = new StringBuilder();
    in = new BufferedReader(new InputStreamReader(
        new FileInputStream(new File("./YAML/vlsp/1-vnf.vnfd")), Charset.forName("UTF-8")));
    line = null;
    while ((line = in.readLine()) != null)
      bodyBuilder.append(line + "\n\r");
    vnfd1 = mapper.readValue(bodyBuilder.toString(), VnfDescriptor.class);

    VnfDescriptor vnfd2;
    bodyBuilder = new StringBuilder();
    in = new BufferedReader(new InputStreamReader(
        new FileInputStream(new File("./YAML/vlsp/2-vnf.vnfd")), Charset.forName("UTF-8")));
    line = null;
    while ((line = in.readLine()) != null)
      bodyBuilder.append(line + "\n\r");
    vnfd2 = mapper.readValue(bodyBuilder.toString(), VnfDescriptor.class);

    VnfDescriptor vnfd3;
    bodyBuilder = new StringBuilder();
    in = new BufferedReader(new InputStreamReader(
        new FileInputStream(new File("./YAML/vlsp/3-vnf.vnfd")), Charset.forName("UTF-8")));
    line = null;
    while ((line = in.readLine()) != null)
      bodyBuilder.append(line + "\n\r");
    vnfd3 = mapper.readValue(bodyBuilder.toString(), VnfDescriptor.class);

    VnfDescriptor vnfd4;
    bodyBuilder = new StringBuilder();
    in = new BufferedReader(new InputStreamReader(
        new FileInputStream(new File("./YAML/vlsp/4-vnf.vnfd")), Charset.forName("UTF-8")));
    line = null;
    while ((line = in.readLine()) != null)
      bodyBuilder.append(line + "\n\r");
    vnfd4 = mapper.readValue(bodyBuilder.toString(), VnfDescriptor.class);


    this.data = new ServiceDeployPayload();
    sd.setInstanceUuid(sd.getInstanceUuid() + "IASFCTEST");
    data.setServiceDescriptor(sd);
    data.addVnfDescriptor(vnfd1);
    data.addVnfDescriptor(vnfd2);
    data.addVnfDescriptor(vnfd3);
    data.addVnfDescriptor(vnfd4);

    WrapperConfiguration config = new WrapperConfiguration();
    config.setName("VlspTest");
    config.setVimVendor(ComputeVimVendor.VLSP);
    config.setWrapperType(WrapperType.COMPUTE);
    config.setCountry("Italy");
    config.setCity("Cropani");
    config.setUuid(UUID.randomUUID().toString());
    config.setVimEndpoint("localhost");
    config.setConfiguration("{\"GC_port\":8888}");
    computeWrapper = (VlspComputeWrapper) WrapperFactory.createWrapper(config);
    
  }

  @Test
  public void testDeployFunctions() {
    
    for (VnfDescriptor vnfd : data.getVnfdList()) {
      FunctionDeployPayload payload = new FunctionDeployPayload();
      payload.setServiceInstanceId(UUID.randomUUID().toString());
      payload.setVimUuid(computeWrapper.getConfig().getUuid());
      payload.setVnfd(vnfd);
      System.out.println("Deploying function: "+vnfd.getName());
      computeWrapper.deployFunction(payload, UUID.randomUUID().toString());
    }
  }



}

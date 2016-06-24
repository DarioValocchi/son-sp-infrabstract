/**
 * @author Dario Valocchi (Ph.D.)
 * @mail d.valocchi@ucl.ac.uk
 * 
 *       Copyright 2016 [Dario Valocchi]
 * 
 *       Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 *       except in compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *       Unless required by applicable law or agreed to in writing, software distributed under the
 *       License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *       either express or implied. See the License for the specific language governing permissions
 *       and limitations under the License.
 * 
 */

package sonata.kernel.adaptor.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import sonata.kernel.adaptor.DeployServiceCallProcessor;
import sonata.kernel.adaptor.commons.DeployServiceData;
import sonata.kernel.adaptor.commons.DeployServiceResponse;
import sonata.kernel.adaptor.commons.ServiceRecord;
import sonata.kernel.adaptor.commons.Status;
import sonata.kernel.adaptor.commons.VduRecord;
import sonata.kernel.adaptor.commons.VnfRecord;
import sonata.kernel.adaptor.commons.vnfd.VirtualDeploymentUnit;
import sonata.kernel.adaptor.commons.vnfd.VnfDescriptor;

import java.util.UUID;

public class MockWrapper extends ComputeWrapper implements Runnable {

  /*
   * Utility fields to implement the mock response creation. A real wrapper should instantiate a
   * suitable object with these fields, able to handle the API call asynchronously, generate a
   * response and update the observer
   */
  private DeployServiceData data;
  private String sid;

  public MockWrapper(WrapperConfiguration config) {
    super();
  }

  @Override
  public String toString() {
    return "MockWrapper";
  }

  @Override
  public boolean deployService(DeployServiceData data,
      final DeployServiceCallProcessor callProcessor) {
    this.addObserver(callProcessor);
    this.data = data;
    this.sid = callProcessor.getSid();
    // This is a mock compute wrapper.

    /*
     * Just use the SD to forge the response message for the SLM with a success. In general Wrappers
     * would need a complex set of actions to deploy the service, so this function should just check
     * if the request is acceptable, and if so start a new thread to deal with the perform the
     * needed actions.
     */
    Thread thread = new Thread(this);
    thread.start();
    return true;
  }

  @Override
  public void run() {
    System.out.println("[MockWrapperFSM] - Deploying Service...");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("[MockWrapperFSM] - Service Deployed. Creating response");
    DeployServiceResponse response = new DeployServiceResponse();
    response.setStatus(Status.normal_operation);
    ServiceRecord sr = new ServiceRecord();
    sr.setStatus(Status.normal_operation);
    sr.setId(data.getNsd().getInstanceUuid());
    for (VnfDescriptor vnf : data.getVnfdList()) {
      VnfRecord vnfr = new VnfRecord();
      vnfr.setDescriptorVersion("vnfr-schema-01");
      vnfr.setStatus(Status.normal_operation);
      vnfr.setDescriptorReferenceName(vnf.getName());
      vnfr.setDescriptorReferenceVendor(vnf.getVendor());
      vnfr.setDescriptorReferenceVersion(vnf.getVersion());
      
      vnfr.setId(vnf.getInstanceUuid());
      for (VirtualDeploymentUnit vdu : vnf.getVirtualDeploymentUnits()) {
        VduRecord vdur = new VduRecord();
        vdur.setId(vdu.getId());
        vdur.setNumberOfInstances(1);
        vdur.setVduReference(vnf.getName() + ":" + vdu.getId());
        vdur.setVmImage(vdu.getVmImage());
        vnfr.addVdu(vdur);
      }
      response.addVnfRecord(vnfr);
    }
    response.setNsr(sr);

    System.out.println("[MockWrapperFSM] - Response created. Serializing...");

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
    mapper.setSerializationInclusion(Include.NON_NULL);
    String body;
    try {
      body = mapper.writeValueAsString(response);
      this.setChanged();
      System.out.println("[MockWrapperFSM] - Serialized. notifying call processor");
      WrapperStatusUpdate update = new WrapperStatusUpdate(this.sid, "SUCCESS", body);
      this.notifyObservers(update);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean removeService(String instanceUuid) {
    boolean out = true;

    this.setChanged();
    String body = "{\"status\":\"SUCCESS\"}";
    WrapperStatusUpdate update = new WrapperStatusUpdate(this.sid, "SUCCESS", body);
    this.notifyObservers(update);

    return out;
  }

  @Override
  public ResourceUtilisation getResourceUtilisation() {

    ResourceUtilisation resources = new ResourceUtilisation();
    resources.setTotCores(10);
    resources.setUsedCores(0);
    resources.setTotMemory(10000);
    resources.setUsedMemory(0);

    return resources;
  }

}

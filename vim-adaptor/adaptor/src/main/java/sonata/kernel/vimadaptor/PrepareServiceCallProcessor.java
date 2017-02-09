/*
 * Copyright (c) 2015 SONATA-NFV, UCL, NOKIA, NCSR Demokritos ALL RIGHTS RESERVED.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Neither the name of the SONATA-NFV, UCL, NOKIA, NCSR Demokritos nor the names of its contributors
 * may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * This work has been performed in the framework of the SONATA project, funded by the European
 * Commission under Grant number 671517 through the Horizon 2020 and 5G-PPP programmes. The authors
 * would like to acknowledge the contributions of their colleagues of the SONATA partner consortium
 * (www.sonata-nfv.eu).
 *
 * @author Dario Valocchi (Ph.D.), UCL
 * 
 */
package sonata.kernel.vimadaptor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.slf4j.LoggerFactory;

import sonata.kernel.vimadaptor.commons.ServicePreparePayload;
import sonata.kernel.vimadaptor.commons.VimPreDeploymentList;
import sonata.kernel.vimadaptor.commons.VnfImage;
import sonata.kernel.vimadaptor.commons.vnfd.Unit;
import sonata.kernel.vimadaptor.commons.vnfd.UnitDeserializer;
import sonata.kernel.vimadaptor.messaging.ServicePlatformMessage;
import sonata.kernel.vimadaptor.wrapper.ComputeWrapper;
import sonata.kernel.vimadaptor.wrapper.WrapperBay;

import java.util.Observable;

public class PrepareServiceCallProcessor extends AbstractCallProcessor {
  private static final org.slf4j.Logger Logger =
      LoggerFactory.getLogger(DeployServiceCallProcessor.class);

  /**
   * @param message
   * @param sid
   * @param mux
   */
  public PrepareServiceCallProcessor(ServicePlatformMessage message, String sid, AdaptorMux mux) {
    super(message, sid, mux);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  @Override
  public void update(Observable arg0, Object arg1) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see sonata.kernel.vimadaptor.AbstractCallProcessor#process(sonata.kernel.vimadaptor.messaging.
   * ServicePlatformMessage)
   */
  @Override
  public boolean process(ServicePlatformMessage message) {

    boolean out = true;
    Logger.info("Call received...");
    // parse the payload to get Wrapper UUID and NSD/VNFD from the request body
    Logger.info("Parsing payload...");
    ServicePreparePayload payload = null;
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Unit.class, new UnitDeserializer());
    mapper.registerModule(module);
    mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    try {
      payload = mapper.readValue(message.getBody(), ServicePreparePayload.class);
      Logger.info("payload parsed. Configuring VIMs");

      for (VimPreDeploymentList vim : payload.getVimList()) {
        ComputeWrapper wr = WrapperBay.getInstance().getComputeWrapper(vim.getUuid());
        if (wr == null) {
          Logger.warn("Error retrieving the wrapper");

          this.sendToMux(new ServicePlatformMessage(
              "{\"request_status\":\"fail\",\"message\":\"VIM not found\"}", "application/json",
              message.getReplyTo(), message.getSid(), null));
          return false;
        }
        Logger.info("Wrapper retrieved");

        for (VnfImage vnfImage : vim.getImages()) {
          if (!wr.isImageStored(vnfImage)) {
            wr.uploadImage(vnfImage);
          }
        }

        boolean success = wr.prepareService(payload.getInstanceId());
        if (!success) {
          throw new Exception("Unable to prepare the environment for instance: "
              + payload.getInstanceId() + " on VIM " + vim.getUuid());
        }

      }
      String responseJson = "{\"status\":\"COMPLETED\",\"message\":\"\"}";
      ServicePlatformMessage responseMessage = new ServicePlatformMessage(responseJson,
          "application/json", message.getReplyTo(), message.getSid(), null);
      this.sendToMux(responseMessage);

    } catch (Exception e) {
      Logger.error("Error deploying the system: " + e.getMessage(), e);
      this.sendToMux(
          new ServicePlatformMessage("{\"status\":\"fail\",\"message\":\"" + e.getMessage() + "\"}",
              "application/json", message.getReplyTo(), message.getSid(), null));
      out = false;
    }
    return out;
  }

}
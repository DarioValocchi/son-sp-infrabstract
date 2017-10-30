/*
 * Copyright (c) 2015 SONATA-NFV, UCL, NOKIA, THALES, NCSR Demokritos ALL RIGHTS RESERVED.
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

package sonata.kernel.WimAdaptor.wrapper.sp;

import sonata.kernel.WimAdaptor.commons.WimRecord;
import sonata.kernel.WimAdaptor.wrapper.WimWrapper;
import sonata.kernel.WimAdaptor.wrapper.WrapperConfiguration;
import sonata.kernel.WimAdaptor.wrapper.sp.client.SonataGkClient;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.LoggerFactory;


public class SPWimWrapper extends WimWrapper{

  private static final org.slf4j.Logger Logger =
      LoggerFactory.getLogger(SPWimWrapper.class);

  public SPWimWrapper(WrapperConfiguration config) {
    super(config);
  }

  @Override
  public boolean configureNetwork(String instanceId, String inputSegment, String outputSegment,
      String[] segmentList) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean removeNetConfiguration(String instanceId) {
    // TODO Auto-generated method stub
    return false;
  }

  
  public WimRecord[] getWims() throws ClientProtocolException, IOException{
    WimRecord[] list;
    Logger.info("[SpWrapper] Creating SONATA Rest Client");
    SonataGkClient client = new SonataGkClient(this.getConfig().getWimEndpoint(),
        this.getConfig().getAuthUserName(), this.getConfig().getAuthPass());

    Logger.info("[SpWrapper] Authenticating SONATA Rest Client");
    if (!client.authenticate()) throw new NotAuthorizedException("Client cannot login to the SP");

    Logger.info("[SpWrapper] Retrieving VIMs connected to slave SONATA SP");
    list = client.getWims();    
    return list;
  }
}

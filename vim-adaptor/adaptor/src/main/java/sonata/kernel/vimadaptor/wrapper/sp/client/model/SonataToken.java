package sonata.kernel.vimadaptor.wrapper.sp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SonataToken {

  @JsonProperty("access_token")
  private String token;
  @JsonProperty("token_type")
  private String type;
  @JsonProperty("not-before-policy")
  private String policy;
  @JsonProperty("session_state")
  private String sessionState;

  public String getToken() {
    return token;
  }

  public String getType() {
    return type;
  }

  public String getPolicy() {
    return policy;
  }

  public String getSessionState() {
    return sessionState;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public void setSessionState(String sessionState) {
    this.sessionState = sessionState;
  }

}

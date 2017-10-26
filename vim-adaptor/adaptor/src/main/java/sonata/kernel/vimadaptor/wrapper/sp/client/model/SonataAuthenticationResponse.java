package sonata.kernel.vimadaptor.wrapper.sp.client.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SonataAuthenticationResponse {

  private String username;
  @JsonProperty("session_began_at")
  private Date sessionStart;

  private SonataToken token;

  public String getUsername() {
    return username;
  }

  public Date getSessionStart() {
    return sessionStart;
  }

  public SonataToken getToken() {
    return token;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setSessionStart(Date sessionStart) {
    this.sessionStart = sessionStart;
  }

  public void setToken(SonataToken token) {
    this.token = token;
  }



}

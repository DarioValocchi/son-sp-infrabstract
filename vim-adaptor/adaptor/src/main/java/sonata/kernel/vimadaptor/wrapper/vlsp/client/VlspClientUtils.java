package sonata.kernel.vimadaptor.wrapper.vlsp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.NotFoundException;

import org.apache.http.HttpResponse;
import org.slf4j.LoggerFactory;

public class VlspClientUtils {

  private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(VlspClientUtils.class);

  
  public static String convertHttpResponseToString(HttpResponse response) throws IOException {

    int status = response.getStatusLine().getStatusCode();
    String statusCode = Integer.toString(status);
    String reasonPhrase = response.getStatusLine().getReasonPhrase();

    if (statusCode.startsWith("2") || statusCode.startsWith("3")) {
      Logger.debug("Response Received with Status: " + response.getStatusLine().getStatusCode());

      StringBuilder sb = new StringBuilder();
      if (response.getEntity() != null) {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line);
        }
        //Logger.debug("Response: " + sb.toString());
        return sb.toString();
      } else {
        return null;
      }
    } else if (status == 404) {
      throw new NotFoundException("Resource doesn't exists");
    } else if (status == 403) {
      throw new IOException(
          "Access forbidden, make sure you are using the correct credentials: " + reasonPhrase);
    } else if (status == 409) {
      throw new IOException("conflict detected: " + reasonPhrase);
    } else {
      throw new IOException("Failed Request: " + reasonPhrase);
    }
  }
  
}

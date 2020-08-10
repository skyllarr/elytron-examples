package qq;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.net.ssl.SSLContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;

/**
 * This class uses RESTEasy client that is configured with default SSL Context.
 * Server in this example is configured with dynamic ssl context, so it will be dynamically switched according to the peer's hostname and port.
 */

@Path("/")
public class Endpoint {

    private ResteasyClientBuilder builder = (ResteasyClientBuilder) ClientBuilder.newBuilder();
    private ResteasyClient client = builder.hostnameVerifier((s, sslSession) -> true).sslContext(SSLContext.getDefault()).build(); // resteasy client must set default ssl context otherwise it uses null

    public Endpoint() throws NoSuchAlgorithmException {
    }

    @GET
    @Path("/port9443request")
    @Produces("text/html")
    public String pingFirstServer() {
        Response response = client.target("https://127.0.0.1:9443").request().get();
        String output = response.readEntity(String.class);
        return "HTTP status of result is " +  String.valueOf(response.getStatus()) + " and body is: " + output;
    }

    @GET
    @Path("/port10443request")
    @Produces("text/html")
    public String pingSecondServer() {
        Response response = client.target("https://127.0.0.1:10443").request().get();
        String output = response.readEntity(String.class);
        return "HTTP status of result is " +  String.valueOf(response.getStatus()) + " and body is: " + output;
    }

}

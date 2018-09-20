package org.blondin.mpg.stats;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.blondin.mpg.stats.model.Championship;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgStatsClient {

    private static final String URL = "https://www.mpgstats.fr/json/customteam.json";

    private static Championship cache;

    private MpgStatsClient() {
        super();
    }

    public static synchronized Championship getStats() {
        if (cache == null) {
            // TODO : Stats are updated every week (analyse JS for detail) => could be cached in file
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(URL).path("Ligue-1");
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();
            cache = response.readEntity(Championship.class);
        }
        return cache;
    }
}

package org.blondin.mpg.root;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.UserSignIn;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgClient extends AbstractClient {

    private MultivaluedMap<String, Object> headersToken = new MultivaluedHashMap<>();

    private MpgClient() {
        super();
    }

    public static MpgClient build(String login, String password) {
        MpgClient client = new MpgClient();
        client.signIn(login, password);
        return client;
    }

    @Override
    protected String getUrl() {
        return "https://api.monpetitgazon.com";
    }

    public Coach getCoach(String league) {
        return get("league/" + league + "/coach", headersToken, Coach.class, true);
    }

    public Dashboard getDashboard() {
        return get("user/dashboard", headersToken, Dashboard.class, true);
    }

    private void signIn(String login, String password) {
        Map<String, String> entity = new HashMap<>();
        entity.put("email", login);
        entity.put("password", password);
        entity.put("language", "fr-FR");
        String token = post("user/signIn", entity, UserSignIn.class).getToken();
        headersToken.add("authorization", token);
    }

}

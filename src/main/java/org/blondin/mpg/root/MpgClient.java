package org.blondin.mpg.root;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.exception.NoMoreGamesException;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.CoachRequest;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.UserSignIn;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgClient extends AbstractClient {

    private MultivaluedMap<String, Object> headersToken = new MultivaluedHashMap<>();

    private MpgClient() {
        super();
    }

    public static MpgClient build(Config config) {
        return build(config, null);
    }

    public static MpgClient build(Config config, String urlOverride) {
        MpgClient client = new MpgClient();
        client.setUrl(StringUtils.defaultString(urlOverride, "https://api.monpetitgazon.com"));
        client.setProxy(config.getProxy());
        client.signIn(config.getLogin(), config.getPassword());
        return client;
    }

    public Coach getCoach(String league) {
        final String path = "league/" + league + "/coach";
        try {
            return get(path, headersToken, Coach.class, true);
        } catch (ProcessingException e) {
            if (e.getCause() instanceof JsonMappingException
                    && e.getCause().getMessage().contains("Root name 'success' does not match expected ('data')")) {
                String response = get(path, headersToken, String.class, true);
                if (response.contains("noMoreGames")) {
                    throw new NoMoreGamesException();
                }
                throw new UnsupportedOperationException(String.format("Coach response not supported: %s", response));
            }
            throw e;
        }
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

    public void updateCoach(League league, CoachRequest coachRequest) {
        String result = post("league/" + league.getId() + "/coach", headersToken, coachRequest, String.class);
        if (!"{\"success\":\"teamSaved\"}".equals(result)) {
            throw new UnsupportedOperationException(String.format("The team has been updated, result message: %s", result));
        }
    }

}

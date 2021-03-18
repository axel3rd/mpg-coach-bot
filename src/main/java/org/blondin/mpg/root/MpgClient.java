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
import org.blondin.mpg.root.model.ChampionshipType;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.CoachRequest;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.Mercato;
import org.blondin.mpg.root.model.MercatoChampionship;
import org.blondin.mpg.root.model.MercatoLeague;
import org.blondin.mpg.root.model.TransferBuy;
import org.blondin.mpg.root.model.UserSignIn;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgClient extends AbstractClient {

    public static final String MPG_CLIENT_VERSION = "6.9.1";

    private static final String PATH_LEAGUE = "league/";

    private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    private MpgClient(Config config) {
        super(config);
    }

    public static MpgClient build(Config config) {
        return build(config, null);
    }

    public static MpgClient build(Config config, String urlOverride) {
        MpgClient client = new MpgClient(config);
        client.setUrl(StringUtils.defaultString(urlOverride, "https://api.monpetitgazon.com"));
        client.signIn(config.getLogin(), config.getPassword());
        return client;
    }

    public Coach getCoach(String league) {
        final String path = PATH_LEAGUE + league + "/coach";
        try {
            return get(path, headers, Coach.class, true);
        } catch (ProcessingException e) {
            if (e.getCause() instanceof JsonMappingException
                    && e.getCause().getMessage().contains("Root name 'success' does not match expected ('data')")) {
                String response = get(path, headers, String.class, true);
                if (response.contains("noMoreGames")) {
                    throw new NoMoreGamesException();
                }
                throw new UnsupportedOperationException(String.format("Coach response not supported: %s", response));
            }
            throw e;
        }
    }

    public Dashboard getDashboard() {
        return get("user/dashboard", headers, Dashboard.class, true);
    }

    public Mercato getMercato(ChampionshipType championship) {
        return get("mercato/" + championship.value(), headers, MercatoChampionship.class);
    }

    public Mercato getMercato(String league) {
        return get(PATH_LEAGUE + league + "/mercato", headers, MercatoLeague.class);
    }

    public TransferBuy getTransferBuy(String league) {
        return get(PATH_LEAGUE + league + "/transfer/buy", headers, TransferBuy.class);
    }

    private void signIn(String login, String password) {
        Map<String, String> entity = new HashMap<>();
        entity.put("email", login);
        entity.put("password", password);
        entity.put("language", "fr-FR");
        String token = post("user/signIn", entity, UserSignIn.class).getToken();
        headers.add("authorization", token);
        headers.add("client-version", MPG_CLIENT_VERSION);
    }

    public void updateCoach(League league, CoachRequest coachRequest) {
        String result = post(PATH_LEAGUE + league.getId() + "/coach", headers, coachRequest, String.class);
        if (!"{\"success\":\"teamSaved\"}".equals(result)) {
            throw new UnsupportedOperationException(String.format("The team has been updated, result message: %s", result));
        }
    }

}

package org.blondin.mpg.root;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.ChampionshipType;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.CoachRequest;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.Division;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.Mercato;
import org.blondin.mpg.root.model.MercatoChampionship;
import org.blondin.mpg.root.model.MercatoLeague;
import org.blondin.mpg.root.model.PoolPlayers;
import org.blondin.mpg.root.model.Team;
import org.blondin.mpg.root.model.TransferBuy;
import org.blondin.mpg.root.model.UserSignIn;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgClient extends AbstractClient {

    public static final String MPG_CLIENT_VERSION = "8.2.0";

    private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    private EnumMap<ChampionshipType, PoolPlayers> cache = new EnumMap<>(ChampionshipType.class);

    private String userId;

    private MpgClient(Config config) {
        super(config);
    }

    public static MpgClient build(Config config) {
        return build(config, null);
    }

    public static MpgClient build(Config config, String urlOverride) {
        MpgClient client = new MpgClient(config);
        client.setUrl(StringUtils.defaultString(urlOverride, "https://api.mpg.football"));
        client.signIn(config.getLogin(), config.getPassword());
        return client;
    }

    public String getUserId() {
        if (StringUtils.isBlank(userId)) {
            throw new UnsupportedOperationException("Please sigin first");
        }
        return userId;
    }

    public Dashboard getDashboard() {
        return get("dashboard/leagues", headers, Dashboard.class);
    }

    public Division getDivision(String leagueDivisionId) {
        if (!StringUtils.startsWith(leagueDivisionId, "mpg_division_")) {
            throw new UnsupportedOperationException(String.format("League id '%s' should start with 'mpg_division_'", leagueDivisionId));
        }
        return get("division/" + leagueDivisionId + "", headers, Division.class);
    }

    public Team getTeam(String leagueTeamId) {
        if (!StringUtils.startsWith(leagueTeamId, "mpg_team_")) {
            throw new UnsupportedOperationException(String.format("League id '%s' should start with 'mpg_team_'", leagueTeamId));
        }
        return get("/team/" + leagueTeamId, headers, Team.class);
    }

    public Coach getCoach(String leagueDivisionId) {
        if (!StringUtils.startsWith(leagueDivisionId, "mpg_division_")) {
            throw new UnsupportedOperationException(String.format("League id '%s' should start with 'mpg_division_'", leagueDivisionId));
        }
        return get("division/" + leagueDivisionId + "/coach", headers, Coach.class);
    }

    public Mercato getMercato(ChampionshipType championship) {
        return get("mercato/" + championship.value(), headers, MercatoChampionship.class);
    }

    public Mercato getMercato(String league) {
        return get("todo-currently-not-found/" + league + "/mercato", headers, MercatoLeague.class);
    }

    public TransferBuy getTransferBuy(String leagueDivisionId) {
        if (!StringUtils.startsWith(leagueDivisionId, "mpg_division_")) {
            throw new UnsupportedOperationException(String.format("League id '%s' should start with 'mpg_division_'", leagueDivisionId));
        }
        return get("division/" + leagueDivisionId + "/available-players", headers, TransferBuy.class);
    }

    public PoolPlayers getPoolPlayers(ChampionshipType championship) {
        if (!cache.containsKey(championship)) {
            PoolPlayers pool = get("championship-players-pool/" + championship.value(), headers, PoolPlayers.class);
            cache.put(championship, pool);
        }
        return cache.get(championship);
    }

    private void signIn(String login, String password) {
        Map<String, String> entity = new HashMap<>();
        entity.put("login", login);
        entity.put("password", password);
        entity.put("language", "fr-FR");
        UserSignIn usi = post("user/sign-in", entity, UserSignIn.class);
        this.userId = usi.getUserId();
        headers.add("authorization", usi.getToken());
        // headers.add("client-version", MPG_CLIENT_VERSION);
    }

    public void updateCoach(League league, CoachRequest coachRequest) {
        String result = put("/match-team-formation/mpg_match_team_formation_LEAGUE_ID_X_Y_Z", headers, coachRequest, String.class);
        if (!"{\"success\":\"teamSaved\"}".equals(result)) {
            throw new UnsupportedOperationException(String.format("The team has been updated, result message: %s", result));
        }
    }

}

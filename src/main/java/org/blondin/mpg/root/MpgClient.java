package org.blondin.mpg.root;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.exception.UrlForbiddenException;
import org.blondin.mpg.root.model.AvailablePlayers;
import org.blondin.mpg.root.model.ChampionshipType;
import org.blondin.mpg.root.model.Clubs;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.CoachRequest;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.Division;
import org.blondin.mpg.root.model.PoolPlayers;
import org.blondin.mpg.root.model.Team;
import org.blondin.mpg.root.model.UserSignIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgClient extends AbstractClient {

    private static final Logger LOG = LoggerFactory.getLogger(MpgClient.class);

    private static final String HEADER_AUTHORIZATION = "authorization";

    private static final String PREFIX_PATH_DIVISION = "division/";

    private static final String PREFIX_ID_DIVISION = "mpg_division_";

    private static final String ERROR_MESSAGE_LEAGUE = "League id '%s' should start with '%s'";

    private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    private EnumMap<ChampionshipType, PoolPlayers> cachePlayers = new EnumMap<>(ChampionshipType.class);
    private Clubs clubs;
    private String userId;

    private MpgClient(Config config) {
        super(config);
    }

    public static MpgClient build(Config config) {
        return build(config, null);
    }

    public static MpgClient build(Config config, String urlOverride) {
        MpgClient client = new MpgClient(config);
        client.setUrl(Objects.toString(urlOverride, "https://api.mpg.football"));

        AuthentMpgWebClient mpgWebClient = AuthentMpgWebClient.build(config, urlOverride);
        client.signIn(config.getLogin(), config.getPassword(), config.getAuthentications(), mpgWebClient);
        return client;
    }

    void signIn(String login, String password, String authentications, AuthentMpgWebClient mpgWebClient) {
        String[] auths = authentications.split(",");
        if (auths.length == 0) {
            throw new UnsupportedOperationException("Authentications types should be defined");
        }
        UserSignIn usi = null;
        for (String authentication : auths) {
            if (headers.containsKey(HEADER_AUTHORIZATION)) {
                // Authorization set by a previous authentication
                return;
            }
            switch (authentication) {
            case "simple":
                try {
                    LOG.debug("Authenticate with 'simple' type");
                    usi = signInSimple(login, password);
                } catch (UrlForbiddenException e) {
                    // Fallback to next (is exist) when authentication problem
                    LOG.debug("Authenticate with 'simple' type if forbidden, will continue with another is exist");
                    if (auths.length == 1) {
                        throw e;
                    }
                }
                break;
            case "oidc":
                LOG.debug("Authenticate with 'oidc' type");
                usi = mpgWebClient.authenticate(login, password, UUID.randomUUID().toString());
                // TODO userId is null here => find a way to fill it
                break;
            default:
                throw new UnsupportedOperationException(String.format("Authentication not supported: '%s'", authentication));
            }
        }
        if (usi == null) {
            throw new UnsupportedOperationException(String.format("Authentication cannot be succeed with one of type:: '%s'", authentications));
        }
        headers.add(HEADER_AUTHORIZATION, usi.getToken());
        this.userId = usi.getUserId();
    }

    private UserSignIn signInSimple(String login, String password) {
        Map<String, String> entity = new HashMap<>();
        entity.put("login", login);
        entity.put("password", password);
        entity.put("language", "fr-FR");
        return post("user/sign-in", entity, UserSignIn.class);
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
        if (!StringUtils.startsWith(leagueDivisionId, PREFIX_ID_DIVISION)) {
            throw new UnsupportedOperationException(String.format(ERROR_MESSAGE_LEAGUE, leagueDivisionId, PREFIX_ID_DIVISION));
        }
        return get(PREFIX_PATH_DIVISION + leagueDivisionId + "", headers, Division.class);
    }

    public Team getTeam(String leagueTeamId) {
        if (!StringUtils.startsWith(leagueTeamId, "mpg_team_")) {
            throw new UnsupportedOperationException(String.format(ERROR_MESSAGE_LEAGUE, leagueTeamId, "mpg_team_"));
        }
        return get("team/" + leagueTeamId, headers, Team.class);
    }

    public Coach getCoach(String leagueDivisionId) {
        if (!StringUtils.startsWith(leagueDivisionId, PREFIX_ID_DIVISION)) {
            throw new UnsupportedOperationException(String.format(ERROR_MESSAGE_LEAGUE, leagueDivisionId, PREFIX_ID_DIVISION));
        }
        return get(PREFIX_PATH_DIVISION + leagueDivisionId + "/coach", headers, Coach.class);
    }

    public AvailablePlayers getAvailablePlayers(String leagueDivisionId) {
        if (!StringUtils.startsWith(leagueDivisionId, PREFIX_ID_DIVISION)) {
            throw new UnsupportedOperationException(String.format(ERROR_MESSAGE_LEAGUE, leagueDivisionId, PREFIX_ID_DIVISION));
        }
        return get(PREFIX_PATH_DIVISION + leagueDivisionId + "/available-players", headers, AvailablePlayers.class);
    }

    public PoolPlayers getPoolPlayers(ChampionshipType championship) {
        if (!cachePlayers.containsKey(championship)) {
            PoolPlayers pool = get("championship-players-pool/" + championship.value(), headers, PoolPlayers.class);
            cachePlayers.put(championship, pool);
        }
        return cachePlayers.get(championship);
    }

    public synchronized Clubs getClubs() {
        if (clubs == null) {
            clubs = get("championship-clubs", headers, Clubs.class);
        }
        return clubs;
    }

    public void updateCoach(String matchId, CoachRequest coachRequest) {
        if (!StringUtils.startsWith(matchId, "mpg_match_team_formation_")) {
            throw new UnsupportedOperationException(String.format("Coach match id '%s' should start with 'mpg_match_team_formation_'", matchId));
        }
        String result = put("match-team-formation/" + matchId, headers, coachRequest, String.class);
        if (StringUtils.isBlank(result) || !result.contains(matchId)) {
            throw new UnsupportedOperationException(String.format("The team has not been updated, result message: %s", result));
        }
    }

}

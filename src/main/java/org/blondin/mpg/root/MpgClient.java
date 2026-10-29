package org.blondin.mpg.root;

import java.util.EnumMap;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
import org.blondin.mpg.root.model.Tokens;
import org.blondin.mpg.root.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Client for https://www.mpgstats.fr/
 */
public class MpgClient extends AbstractClient {

    private static final Logger LOG = LoggerFactory.getLogger(MpgClient.class);

    protected static final String HEADER_AUTHORIZATION_NAME = "authorization";
    protected static final String HEADER_AUTHORIZATION_BEARER = "Bearer ";

    private static final String PREFIX_PATH_DIVISION = "division/";

    private static final String PREFIX_ID_DIVISION = "mpg_division_";

    private static final String ERROR_MESSAGE_LEAGUE = "League id '%s' should start with '%s'";

    private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    private final EnumMap<ChampionshipType, PoolPlayers> cachePlayers = new EnumMap<>(ChampionshipType.class);
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
        client.signIn(config, mpgWebClient);
        return client;
    }

    void signIn(Config config, AuthentMpgWebClient mpgWebClient) {
        try {
            Tokens tokens = new Tokens(config.getTokenAccess(), config.getTokenRefresh());
            if (!tokens.isValid() || tokens.isExpiredInDays(0)) {
                tokens = mpgWebClient.authenticate(config.getLogin(), config.getPassword());
                config.storeTokens(tokens.getAccessToken(), tokens.getRefreshToken());
            } else if (tokens.isExpiredInDays(config.getTokenRenew())) {
                tokens = mpgWebClient.refresh(tokens.getRefreshToken());
                config.storeTokens(tokens.getAccessToken(), tokens.getRefreshToken());
            }
            headers.add(HEADER_AUTHORIZATION_NAME, HEADER_AUTHORIZATION_BEARER + tokens.getAccessToken());
            User user = get("user", headers, User.class);
            this.userId = user.getId();
        } catch (UnsupportedOperationException e) {
            if (e.getMessage().contains("401") || e instanceof UrlForbiddenException) {
                LOG.error("There is a problem with Bearer token. It has been removed from configuration file. Please retry to run program to enforce renew authentication process with login/password.",
                        e);
                config.removeTokens();
            }
            throw e;
        }
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
        if (!Strings.CS.startsWith(leagueDivisionId, PREFIX_ID_DIVISION)) {
            throw new UnsupportedOperationException(String.format(ERROR_MESSAGE_LEAGUE, leagueDivisionId, PREFIX_ID_DIVISION));
        }
        return get(PREFIX_PATH_DIVISION + leagueDivisionId + "", headers, Division.class);
    }

    public Team getTeam(String leagueTeamId) {
        if (!Strings.CS.startsWith(leagueTeamId, "mpg_team_")) {
            throw new UnsupportedOperationException(String.format(ERROR_MESSAGE_LEAGUE, leagueTeamId, "mpg_team_"));
        }
        return get("team/" + leagueTeamId, headers, Team.class);
    }

    public Coach getCoach(String leagueDivisionId) {
        if (!Strings.CS.startsWith(leagueDivisionId, PREFIX_ID_DIVISION)) {
            throw new UnsupportedOperationException(String.format(ERROR_MESSAGE_LEAGUE, leagueDivisionId, PREFIX_ID_DIVISION));
        }
        return get(PREFIX_PATH_DIVISION + leagueDivisionId + "/coach", headers, Coach.class);
    }

    public AvailablePlayers getAvailablePlayers(String leagueDivisionId) {
        if (!Strings.CS.startsWith(leagueDivisionId, PREFIX_ID_DIVISION)) {
            throw new UnsupportedOperationException(String.format(ERROR_MESSAGE_LEAGUE, leagueDivisionId, PREFIX_ID_DIVISION));
        }
        return get(PREFIX_PATH_DIVISION + leagueDivisionId + "/available-players", headers, AvailablePlayers.class);
    }

    public PoolPlayers getPoolPlayers(ChampionshipType championship) {
        cachePlayers.computeIfAbsent(championship, c -> get("championship-players-pool/" + championship.value(), headers, PoolPlayers.class));
        return cachePlayers.get(championship);
    }

    public synchronized Clubs getClubs() {
        if (clubs == null) {
            clubs = get("championship-clubs", headers, Clubs.class);
        }
        return clubs;
    }

    public void updateCoach(String matchId, CoachRequest coachRequest) {
        if (!Strings.CS.startsWith(matchId, "mpg_match_team_formation_")) {
            throw new UnsupportedOperationException(String.format("Coach match id '%s' should start with 'mpg_match_team_formation_'", matchId));
        }
        String result = put("match-team-formation/" + matchId, headers, coachRequest, String.class);
        if (StringUtils.isBlank(result) || !result.contains(matchId)) {
            throw new UnsupportedOperationException(String.format("The team has not been updated, result message: %s", result));
        }
    }

}

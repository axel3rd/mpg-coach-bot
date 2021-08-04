package org.blondin.mpg.out;

import javax.ws.rs.ServiceUnavailableException;

import org.apache.commons.lang3.ObjectUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.blondin.mpg.out.model.Position;
import org.blondin.mpg.root.exception.TeamsNotFoundException;
import org.blondin.mpg.root.exception.UrlForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for:<br/>
 * - https://www.sportsgambler.com/injuries/football<br/>
 * - (Before February 2021: https://www.sportsgambler.com/football/injuries-suspensions)<br/>
 * - https://maligue2.fr/2020/08/20/joueurs-blesses-et-suspendus/<br/>
 * - https://www.equipeactu.fr/blessures-et-suspensions/fodbold/<br/>
 */
public class InjuredSuspendedWrapperClient {

    private static final Logger LOG = LoggerFactory.getLogger(InjuredSuspendedWrapperClient.class);
    private static InjuredSuspendedSportsGamblerClient sportsGamblerClient = null;
    private static InjuredSuspendedEquipeActuClient equipeActuClient = null;
    private static InjuredSuspendedMaLigue2Client maLigue2Client = null;
    private boolean sportsGamblerReachable = true;
    private boolean maLigue2Reachable = true;

    public static InjuredSuspendedWrapperClient build(Config config) {
        return build(config, null, null, null);
    }

    public static InjuredSuspendedWrapperClient build(Config config, String urlOverrideSportsGambler, String urlOverrideEquipeActu,
            String urlOverrideMaLigue2) {
        InjuredSuspendedWrapperClient client = new InjuredSuspendedWrapperClient();
        sportsGamblerClient = InjuredSuspendedSportsGamblerClient.build(config, urlOverrideSportsGambler);
        equipeActuClient = InjuredSuspendedEquipeActuClient.build(config, urlOverrideEquipeActu);
        maLigue2Client = InjuredSuspendedMaLigue2Client.build(config, urlOverrideMaLigue2);
        return client;
    }

    /**
     * Return injured or suspended player
     * 
     * @param championship Championship of player
     * @param name Player Name
     * @param position Position (used to improve "out player" matching if not null)
     * @param teamName Team Name
     * @param excludes {@link OutType} to exclude
     * @return Player or null if not found
     */
    public Player getPlayer(ChampionshipOutType championship, String playerName, Position position, String teamName, OutType... excludes) {
        if (!ObjectUtils.allNotNull(championship, playerName, position, teamName)) {
            throw new UnsupportedOperationException("Main parameters (championship, playerName, position, teamName) can not be null");
        }
        if (ChampionshipOutType.LIGUE_2.equals(championship)) {
            if (!maLigue2Reachable) {
                return null;
            }
            try {
                return maLigue2Client.getPlayer(playerName, teamName);
            } catch (ServiceUnavailableException e) {
                LOG.error("WARN: Maligue2.fr is unavailable, L2 injured/suspended players not taken into account :-(");
                maLigue2Reachable = false;
                return null;
            }
        }
        try {
            if (sportsGamblerReachable) {
                return useDirectlyOnlyForTestGetSportsGamblerClient().getPlayer(championship, playerName, teamName);
            }
        } catch (UrlForbiddenException | ServiceUnavailableException e) {
            LOG.error("WARN: SportsGambler is not reacheable, fallback to EquipeActu...");
            sportsGamblerReachable = false;
        } catch (TeamsNotFoundException e) {
            LOG.error("WARN: No teams found on SportsGambler, fallback to EquipeActu...");
            LOG.error("(Your IP is perhaps temporary ban, try to increase 'request.wait.time' parameter)");
        }
        // Fallback on EquipeActu if SportsGambler not reachable
        return useDirectlyOnlyForTestGetEquipeActuClient().getPlayer(championship, playerName, position, teamName, excludes);
    }

    public InjuredSuspendedEquipeActuClient useDirectlyOnlyForTestGetEquipeActuClient() {
        return equipeActuClient;
    }

    public InjuredSuspendedSportsGamblerClient useDirectlyOnlyForTestGetSportsGamblerClient() {
        return sportsGamblerClient;
    }

}

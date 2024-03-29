package org.blondin.mpg.out;

import org.apache.commons.lang3.ObjectUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.blondin.mpg.out.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ServiceUnavailableException;

/**
 * Wrapper for:<br/>
 * - https://www.sportsgambler.com/injuries/football<br/>
 * - (Before February 2021: https://www.sportsgambler.com/football/injuries-suspensions)<br/>
 * - https://maligue2.fr/2020/08/20/joueurs-blesses-et-suspendus/<br/>
 */
public class InjuredSuspendedWrapperClient {

    private static final Logger LOG = LoggerFactory.getLogger(InjuredSuspendedWrapperClient.class);
    private static InjuredSuspendedSportsGamblerClient sportsGamblerClient = null;
    private static InjuredSuspendedMaLigue2Client maLigue2Client = null;
    private boolean sportsGamblerReachable = true;
    private boolean maLigue2Reachable = true;

    public static InjuredSuspendedWrapperClient build(Config config) {
        return build(config, null, null);
    }

    public static InjuredSuspendedWrapperClient build(Config config, String urlOverrideSportsGambler, String urlOverrideMaLigue2) {
        InjuredSuspendedWrapperClient client = new InjuredSuspendedWrapperClient();
        sportsGamblerClient = InjuredSuspendedSportsGamblerClient.build(config, urlOverrideSportsGambler);
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
            try {
                if (maLigue2Reachable) {
                    return maLigue2Client.getPlayer(playerName, teamName);
                }
            } catch (UnsupportedOperationException | ServiceUnavailableException e) {
                LOG.warn("WARN: Maligue2.fr is unavailable, L2 injured/suspended players not taken into account :-(");
                maLigue2Reachable = false;

            }
        } else {
            try {
                if (sportsGamblerReachable) {
                    return sportsGamblerClient.getPlayer(championship, playerName, teamName);
                }
            } catch (UnsupportedOperationException | ServiceUnavailableException e) {
                LOG.warn("WARN: SportsGambler is unavailable, injured/suspended players not taken into account :-(");
                LOG.warn("(Your IP is perhaps temporary ban, try to increase 'request.wait.time' parameter)");
                sportsGamblerReachable = false;
            }
        }
        return null;
    }

}

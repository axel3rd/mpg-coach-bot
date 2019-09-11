package org.blondin.mpg.out;

import org.apache.commons.lang3.ObjectUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.blondin.mpg.out.model.Position;

/**
 * Wrapper for:<br/>
 * - http://www.equipeactu.fr/blessures-et-suspensions/fodbold/<br/>
 * - https://maligue2.fr/2019/08/05/joueurs-blesses-et-suspendus/
 */
public class InjuredSuspendedWrapperClient {

    private static InjuredSuspendedEquipeActuClient equipeActuClient = null;
    private static InjuredSuspendedMaLigue2Client maLigue2Client = null;

    public static InjuredSuspendedWrapperClient build(Config config) {
        return build(config, null, null);
    }

    public static InjuredSuspendedWrapperClient build(Config config, String urlOverrideEquipeActu, String urlOverrideMaLigue2) {
        InjuredSuspendedWrapperClient client = new InjuredSuspendedWrapperClient();
        equipeActuClient = InjuredSuspendedEquipeActuClient.build(config, urlOverrideEquipeActu);
        maLigue2Client = InjuredSuspendedMaLigue2Client.build(config, urlOverrideMaLigue2);
        return client;
    }

    /**
     * Return injured or suspended player
     * 
     * @param championship Championship of player
     * @param name         Player Name
     * @param position     Position (used to improve "out player" matching if not null)
     * @param teamName     Team Name
     * @param excludes     {@link OutType} to exclude
     * @return Player or null if not found
     */
    public Player getPlayer(ChampionshipOutType championship, String playerName, Position position, String teamName, OutType... excludes) {
        if (!ObjectUtils.allNotNull(championship, playerName, position, teamName)) {
            throw new UnsupportedOperationException("Main parameters (championship, playerName, position, teamName) can not be null");
        }
        if (ChampionshipOutType.LIGUE_2.equals(championship)) {
            return useOnlyForTestGetMaLigue2Client().getPlayer(playerName, teamName);
        }
        return useOnlyForTestGetEquipeActuClient().getPlayer(championship, playerName, position, teamName, excludes);
    }

    public InjuredSuspendedEquipeActuClient useOnlyForTestGetEquipeActuClient() {
        return equipeActuClient;
    }

    private InjuredSuspendedMaLigue2Client useOnlyForTestGetMaLigue2Client() {
        return maLigue2Client;
    }
}

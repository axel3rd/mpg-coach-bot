package org.blondin.mpg.out;

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
     * @param name         Name
     * @param position     Position
     * @param excludes     {@link OutType} to exclude
     * @return Player or null if not found
     */
    public Player getPlayer(ChampionshipOutType championship, String name, Position position, OutType... excludes) {
        if (ChampionshipOutType.LIGUE_2.equals(championship)) {
            return useOnlyForTestGetMaLigue2Client().getPlayer(name);
        }
        return useOnlyForTestGetEquipeActuClient().getPlayer(championship, name, position, excludes);
    }

    public InjuredSuspendedEquipeActuClient useOnlyForTestGetEquipeActuClient() {
        return equipeActuClient;
    }

    private InjuredSuspendedMaLigue2Client useOnlyForTestGetMaLigue2Client() {
        return maLigue2Client;
    }
}

package org.blondin.mpg;

import org.blondin.mpg.out.InjuredSuspendedWrapperClient;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.stats.MpgStatsClient;

public class ApiClients {

    private MpgClient mpgClient = null;
    private MpgStatsClient mpgStatsClient = null;
    private InjuredSuspendedWrapperClient outPlayersClient = null;

    private ApiClients(MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedWrapperClient outPlayersClient) {
        super();
        this.mpgClient = mpgClient;
        this.mpgStatsClient = mpgStatsClient;
        this.outPlayersClient = outPlayersClient;
    }

    public static ApiClients build(MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedWrapperClient outPlayersClient) {
        return new ApiClients(mpgClient, mpgStatsClient, outPlayersClient);
    }

    public MpgClient getMpg() {
        return mpgClient;
    }

    public MpgStatsClient getStats() {
        return mpgStatsClient;
    }

    public InjuredSuspendedWrapperClient getOutPlayers() {
        return outPlayersClient;
    }
}

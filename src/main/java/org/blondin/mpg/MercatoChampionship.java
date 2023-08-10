package org.blondin.mpg;

import java.util.List;

import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MercatoChampionship extends AbstractMercatoMpgProcess {

    private static final Logger LOG = LoggerFactory.getLogger(MercatoChampionship.class);

    public void process(League league, ApiClients apiClients, Config config) {
        LOG.info("\nProposal for your coming soon mercato:\n");
        List<Player> players = apiClients.getMpg().getPoolPlayers(league.getChampionship()).getPlayers();
        completePlayersClub(players, apiClients.getMpg().getClubs());
        completeAuctionAndcalculateEfficiency(players, apiClients.getStats(), ChampionshipTypeWrapper.toStats(league.getChampionship()), config,
                false, true);
        processMercato(players, apiClients.getOutPlayers(), ChampionshipTypeWrapper.toOut(league.getChampionship()));
    }
}

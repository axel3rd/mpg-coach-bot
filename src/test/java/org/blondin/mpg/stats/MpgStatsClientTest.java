package org.blondin.mpg.stats;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import java.io.File;
import java.util.Arrays;

import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.stats.model.Championship;
import org.blondin.mpg.stats.model.LeaguesRefresh;
import org.blondin.mpg.stats.model.Player;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MpgStatsClientTest extends AbstractMockTestClient {

    @Test
    public void testEfficiencyRecentFocus() {
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20190406.json")));
        stubFor(get("/leagues/Ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.ligue-1.20190406.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        Player playerNeymar = mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getPlayer("Neymar");
        Assert.assertNotNull(playerNeymar);
        Assert.assertEquals(6.77, playerNeymar.getStats().getAverage(0), 0);
        Assert.assertEquals(0, playerNeymar.getStats().getAverage(8), 0);
        Assert.assertEquals(13, playerNeymar.getStats().getGoals(0));
        Assert.assertEquals(13, playerNeymar.getStats().getGoals(-1));
        Assert.assertEquals(0, playerNeymar.getStats().getGoals(1));
        Assert.assertEquals(0, playerNeymar.getStats().getGoals(8));
        Assert.assertEquals(13, playerNeymar.getStats().getMatchs(-1));
        Assert.assertEquals(13, playerNeymar.getStats().getMatchs(0));
        Assert.assertEquals(0, playerNeymar.getStats().getMatchs(1));
        Assert.assertEquals(0, playerNeymar.getStats().getMatchs(8));

        Player playerMBappe = mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getPlayer("Mbappé");
        Assert.assertNotNull(playerMBappe);
        Assert.assertEquals(6.79, playerMBappe.getStats().getAverage(0), 0);
        Assert.assertEquals(6, playerMBappe.getStats().getAverage(1), 0);
        Assert.assertEquals(6.5, playerMBappe.getStats().getAverage(2), 0);
        Assert.assertEquals(6.67, playerMBappe.getStats().getAverage(4), 0);
        Assert.assertEquals(6.33, playerMBappe.getStats().getAverage(7), 0);
        Assert.assertEquals(27, playerMBappe.getStats().getGoals(0));
        Assert.assertEquals(1, playerMBappe.getStats().getGoals(1));
        Assert.assertEquals(2, playerMBappe.getStats().getGoals(2));
        Assert.assertEquals(7, playerMBappe.getStats().getGoals(8));
        Assert.assertEquals(24, playerMBappe.getStats().getMatchs(0));
        Assert.assertEquals(7, playerMBappe.getStats().getMatchs(8));
        Assert.assertEquals(2, playerMBappe.getStats().getMatchs(2));
        Assert.assertEquals(2, playerMBappe.getStats().getMatchs(3));

        Player playerMTrap = mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getPlayer("Trapp");
        Assert.assertNotNull(playerMTrap);
        Assert.assertEquals(0, playerMTrap.getStats().getAverage(1), 0);
        Assert.assertEquals(0, playerMTrap.getStats().getGoals(1));
        Assert.assertEquals(0, playerMTrap.getStats().getMatchs(1));
    }

    @Test
    public void testMockAllLeagues() {
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20181017.json")));
        stubFor(get("/leagues/Ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.ligue-1.20181017.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20181017.json")));
        stubFor(get("/leagues/Liga")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.liga.20181017.json")));

        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        for (ChampionshipStatsType type : Arrays.asList(ChampionshipStatsType.LIGUE_1, ChampionshipStatsType.PREMIER_LEAGUE,
                ChampionshipStatsType.LIGA)) {
            Championship championship = mpgStatsClient.getStats(type);
            subChampionshipTest(championship, type.getValue().toLowerCase());
        }
    }

    @Test
    public void testLocalMapping() throws Exception {
        for (String subFile : Arrays.asList("ligue-1", "premier-league", "liga")) {
            Championship championship = new ObjectMapper().readValue(new File("src/test/resources/__files", "mpgstats." + subFile + ".20181017.json"),
                    Championship.class);
            subChampionshipTest(championship, subFile);
        }
    }

    @Test
    public void testLocalMappingRefresh() throws Exception {
        LeaguesRefresh refresh = new ObjectMapper().readValue(new File("src/test/resources/__files", "mpgstats.leagues.20181017.json"),
                LeaguesRefresh.class);
        Assert.assertNotNull(refresh);
        Assert.assertNotNull(refresh.getDate(1));
        Assert.assertNotNull(refresh.getDate(2));
        Assert.assertNotNull(refresh.getDate(3));
    }

    private void subChampionshipTest(Championship championship, String championshipName) {
        Assert.assertNotNull(championship);
        Assert.assertNotNull(championship.getInfos());
        Assert.assertEquals(championshipName, championship.getInfos().getName().toLowerCase());
        Assert.assertNotNull(championship.getInfos().getId());
        Assert.assertNotNull(championship.getPlayers());
        Assert.assertTrue(championshipName + ":" + championship.getPlayers().size(), championship.getPlayers().size() > 550);
        boolean atLeastOne = false;
        for (Player player : championship.getPlayers()) {
            Assert.assertNotNull(player);
            Assert.assertNotNull(player.getName(), player.getFirstName());
            Assert.assertNotNull(player.getName(), player.getLastName());
            Assert.assertNotNull(player.getName());
            Assert.assertFalse(player.getName(), player.getName().contains("null"));
            Assert.assertTrue(player.getName(), player.getPrice() > 0);
            if (player.getStats().getGoals(-1) > 0) {
                atLeastOne = true;
                Assert.assertTrue(player.getName(), player.getStats().getAverage(0) > 0);
            }
        }
        Assert.assertTrue(atLeastOne);
    }
}

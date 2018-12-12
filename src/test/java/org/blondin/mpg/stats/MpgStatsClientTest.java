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
    public void testMockAllLeagues() {
        stubFor(get("/leagues.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20181017.json")));
        stubFor(get("/customteam.json/Ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.ligue-1.20181017.json")));
        stubFor(get("/customteam.json/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20181017.json")));
        stubFor(get("/customteam.json/Liga")
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
        boolean atLeatOne = false;
        for (Player player : championship.getPlayers()) {
            Assert.assertNotNull(player);
            Assert.assertNotNull(player.getName(), player.getFirstName());
            Assert.assertNotNull(player.getName(), player.getLastName());
            Assert.assertNotNull(player.getName());
            Assert.assertFalse(player.getName(), player.getName().contains("null"));
            Assert.assertTrue(player.getName(), player.getPrice() > 0);
            if (player.getStats().getGoals() > 0) {
                atLeatOne = true;
                Assert.assertTrue(player.getName(), player.getStats().getAverage() > 0);
            }
        }
        Assert.assertTrue(atLeatOne);
    }
}

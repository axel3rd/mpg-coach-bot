package org.blondin.mpg.stats;

import java.io.File;
import java.util.Arrays;

import org.blondin.mpg.config.Config;
import org.blondin.mpg.stats.model.Championship;
import org.blondin.mpg.stats.model.LeaguesRefresh;
import org.blondin.mpg.stats.model.Player;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MpgStatsClientTest {

    @Test
    public void testReal() {
        // Real test => use real config if exist (for potential proxy usage)
        Config config = Config.build("src/test/resources/mpg.properties.here");
        if (new File("src/test/resources", "mpg.properties").exists()) {
            config = Config.build("src/test/resources/mpg.properties");
        }

        // Remove cache file to be sure it works
        File cacheFile = MpgStatsClient.getCacheFile("https://www.mpgstats.fr/json/customteam.json", "Ligue-1");
        cacheFile.delete();
        Assert.assertFalse(cacheFile.exists());

        for (ChampionshipStatsType type : Arrays.asList(ChampionshipStatsType.LIGUE_1, ChampionshipStatsType.PREMIER_LEAGUE,
                ChampionshipStatsType.LIGA)) {
            Championship championship = MpgStatsClient.build(config).getStats(type);
            Assert.assertNotNull(championship);
            Assert.assertNotNull(championship.getPlayers());
            Assert.assertTrue(championship.getPlayers().size() > 550);
        }
    }

    @Test
    public void testLocalMapping() throws Exception {
        for (String subFile : Arrays.asList("ligue-1", "premier-league", "liga")) {
            Championship championship = new ObjectMapper().readValue(new File("src/test/resources/datas", "mpgstats." + subFile + ".json"),
                    Championship.class);
            Assert.assertNotNull(championship);
            Assert.assertNotNull(championship.getInfos());
            Assert.assertEquals(subFile, championship.getInfos().getName().toLowerCase());
            Assert.assertNotNull(championship.getInfos().getId());
            Assert.assertNotNull(championship.getPlayers());
            Assert.assertTrue(subFile + ":" + championship.getPlayers().size(), championship.getPlayers().size() > 550);
            boolean atLeatOne = false;
            for (Player player : championship.getPlayers()) {
                Assert.assertNotNull(player);
                Assert.assertNotNull(player.getName(), player.getFirstName());
                Assert.assertNotNull(player.getName(), player.getLastName());
                Assert.assertNotNull(player.getName());
                Assert.assertEquals(player.getName(), (player.getLastName() + " " + player.getFirstName()).trim());
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

    @Test
    public void testLocalMappingRefresh() throws Exception {
        LeaguesRefresh refresh = new ObjectMapper().readValue(new File("src/test/resources/datas", "mpgstats.leagues.json"), LeaguesRefresh.class);
        Assert.assertNotNull(refresh);
        Assert.assertNotNull(refresh.getDate(1));
        Assert.assertNotNull(refresh.getDate(2));
        Assert.assertNotNull(refresh.getDate(3));
    }
}

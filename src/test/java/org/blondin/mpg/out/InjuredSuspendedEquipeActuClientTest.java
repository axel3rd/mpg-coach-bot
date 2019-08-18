package org.blondin.mpg.out;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.blondin.mpg.out.model.Position;
import org.junit.Assert;
import org.junit.Test;

public class InjuredSuspendedEquipeActuClientTest extends AbstractMockTestClient {

    @Test
    public void testLocalMapping() throws Exception {
        for (String subFile : Arrays.asList("ligue-1", "premier-league", "liga")) {
            List<Player> players = InjuredSuspendedEquipeActuClient.build(Config.build("src/test/resources/mpg.properties.here")).getPlayers(FileUtils
                    .readFileToString(new File("src/test/resources/__files", "equipeactu." + subFile + ".20181017.html"), Charset.defaultCharset()));
            Assert.assertNotNull(players);
            Assert.assertTrue(players.size() > 10);
            for (Player player : players) {
                Assert.assertNotNull(player);
                Assert.assertNotNull(player.getFullNameWithPosition());
                Assert.assertNotNull(player.getFullNameWithPosition(), player.getDescription());
                Assert.assertNotNull(player.getFullNameWithPosition(), player.getLength());
            }
        }
    }

    @Test
    public void testSomeInjuries() throws Exception {
        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.ligue-1.20190131.html"), Charset.defaultCharset()))
                .when(client).getHtmlContent(ChampionshipOutType.LIGUE_1);

        Assert.assertNotNull("Fares Bahlouli is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Fares Bahlouli", Position.UNDEFINED));
        Assert.assertNotNull("Neymar is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Neymar", Position.UNDEFINED));
        Assert.assertNotNull("Neymar is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Neymar", Position.A));
        Assert.assertNotNull("Pablo Chavarria is not injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Pablo Chavarria", Position.A));
        Assert.assertNull("Pablo is not injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Pablo", Position.D));
    }

    @Test
    public void testFeaturesLigue1() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.LIGUE_1;

        // Mock
        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.ligue-1.20181017.html"), Charset.defaultCharset()))
                .when(client).getHtmlContent(ChampionshipOutType.LIGUE_1);

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Presnel Kimpembe", Position.UNDEFINED));
        Assert.assertNotNull(client.getPlayer(c, "preSnel kimpeMbe", Position.UNDEFINED));
        Assert.assertNotNull(client.getPlayer(c, "Kimpembe Presnel", Position.UNDEFINED));

        Player p = client.getPlayer(c, "Jesé", Position.UNDEFINED);
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure à la hanche (depuis 29/09)", p.getDescription());
        Assert.assertEquals("Inconnu", p.getLength());

        Assert.assertNull(client.getPlayer(c, "Presnel Kimpembe", Position.UNDEFINED, OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesPremierLeague() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.PREMIER_LEAGUE;

        // Mock
        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.premier-league.20181017.html"),
                Charset.defaultCharset())).when(client).getHtmlContent(ChampionshipOutType.PREMIER_LEAGUE);

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Yoshinori Muto", Position.UNDEFINED));
        Assert.assertNotNull(client.getPlayer(c, "yoshinori muto", Position.UNDEFINED));
        Assert.assertNotNull(client.getPlayer(c, "Kenedy", Position.UNDEFINED));

        Player p = client.getPlayer(c, "Danilo", Position.UNDEFINED);
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure à la cheville (depuis 16/10)", p.getDescription());
        Assert.assertEquals("Inconnu", p.getLength());

        Assert.assertNotNull(client.getPlayer(c, "José Holebas", Position.UNDEFINED));
        Assert.assertNull(client.getPlayer(c, "José Holebas", Position.UNDEFINED, OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesLiga() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.LIGA;

        // Mock
        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.liga.20181017.html"), Charset.defaultCharset()))
                .when(client).getHtmlContent(ChampionshipOutType.LIGA);

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Unai Bustinza", Position.UNDEFINED));
        Assert.assertNotNull(client.getPlayer(c, "unai bustinza", Position.UNDEFINED));
        Assert.assertNotNull(client.getPlayer(c, "CheMa", Position.UNDEFINED));

        Player p = client.getPlayer(c, "Antonio Luna Rodriguez", Position.UNDEFINED);
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure musculaire (depuis 03/10)", p.getDescription());
        Assert.assertEquals("Au jour le jour", p.getLength());

        Assert.assertNotNull(client.getPlayer(c, "Álvaro Medrán", Position.UNDEFINED));
        Assert.assertNull(client.getPlayer(c, "Álvaro Medrán", Position.UNDEFINED, OutType.SUSPENDED, OutType.INJURY_ORANGE));
    }

    @Test
    public void testMock() throws Exception {
        stubFor(get("/blessures-et-suspensions/fodbold/france/ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.ligue-1.20181017.html")));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/championship")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20181017.html")));
        stubFor(get("/blessures-et-suspensions/fodbold/espagne/primera-division")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.liga.20181017.html")));

        InjuredSuspendedEquipeActuClient injuredSuspendedClient = InjuredSuspendedEquipeActuClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");

        // Remove cache
        File tmpFile = InjuredSuspendedEquipeActuClient.getCacheFile("http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/",
                "france/ligue-1");
        tmpFile.delete();
        Assert.assertFalse(tmpFile.exists());

        for (ChampionshipOutType type : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.PREMIER_LEAGUE, ChampionshipOutType.LIGA)) {
            List<Player> players = injuredSuspendedClient.getPlayers(type);
            Assert.assertNotNull(players);
            Assert.assertTrue(players.size() > 10);
        }

        // Verify cache file has been created, recall and verify date file doesn't change
        Assert.assertTrue(tmpFile.exists());
        long cacheDate = tmpFile.lastModified();
        injuredSuspendedClient = InjuredSuspendedEquipeActuClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        injuredSuspendedClient.getPlayers(ChampionshipOutType.LIGUE_1);
        Assert.assertEquals(cacheDate, tmpFile.lastModified());
    }
}

package org.blondin.mpg.equipeactu;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.equipeactu.model.OutType;
import org.blondin.mpg.equipeactu.model.Player;
import org.junit.Assert;
import org.junit.Test;

public class InjuredSuspendedClientTest {

    @Test
    public void testLocalMapping() throws Exception {
        for (String subFile : Arrays.asList("ligue-1", "premier-league", "liga")) {
            List<Player> players = InjuredSuspendedClient.build()
                    .getPlayers(FileUtils.readFileToString(new File("src/test/resources/datas", "equipeactu." + subFile + "-1.html")));
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
    public void testFeaturesLigue1() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.LIGUE_1;

        // Mock
        InjuredSuspendedClient client = spy(InjuredSuspendedClient.class);
        when(client.getHtmlContent(c))
                .thenReturn(FileUtils.readFileToString(new File("src/test/resources/datas", "equipeactu.ligue-1-1.html"), "UTF-8"));

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Presnel Kimpembe"));
        Assert.assertNotNull(client.getPlayer(c, "preSnel kimpeMbe"));
        Assert.assertNotNull(client.getPlayer(c, "Kimpembe Presnel"));

        Player p = client.getPlayer(c, "Jesé");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure à la hanche (depuis 29/09)", p.getDescription());
        Assert.assertEquals("Inconnu", p.getLength());

        Assert.assertNull(client.getPlayer(c, "Presnel Kimpembe", OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesPremierLeague() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.PREMIER_LEAGUE;

        // Mock
        InjuredSuspendedClient client = spy(InjuredSuspendedClient.class);
        when(client.getHtmlContent(c))
                .thenReturn(FileUtils.readFileToString(new File("src/test/resources/datas", "equipeactu.premier-league-1.html"), "UTF-8"));

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Yoshinori Muto"));
        Assert.assertNotNull(client.getPlayer(c, "yoshinori muto"));
        Assert.assertNotNull(client.getPlayer(c, "Kenedy"));

        Player p = client.getPlayer(c, "Danilo");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure à la cheville (depuis 16/10)", p.getDescription());
        Assert.assertEquals("Inconnu", p.getLength());

        Assert.assertNotNull(client.getPlayer(c, "José Holebas"));
        Assert.assertNull(client.getPlayer(c, "José Holebas", OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesLiga() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.LIGA;

        // Mock
        InjuredSuspendedClient client = spy(InjuredSuspendedClient.class);
        when(client.getHtmlContent(c))
                .thenReturn(FileUtils.readFileToString(new File("src/test/resources/datas", "equipeactu.liga-1.html"), "UTF-8"));

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Unai Bustinza"));
        Assert.assertNotNull(client.getPlayer(c, "unai bustinza"));
        Assert.assertNotNull(client.getPlayer(c, "CheMa"));

        Player p = client.getPlayer(c, "Antonio Luna Rodriguez");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure musculaire (depuis 03/10)", p.getDescription());
        Assert.assertEquals("Au jour le jour", p.getLength());

        Assert.assertNotNull(client.getPlayer(c, "Álvaro Medrán"));
        Assert.assertNull(client.getPlayer(c, "Álvaro Medrán", OutType.SUSPENDED, OutType.INJURY_ORANGE));
    }

    @Test
    public void testReal() throws Exception {
        for (ChampionshipOutType type : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.PREMIER_LEAGUE, ChampionshipOutType.LIGA)) {
            List<Player> players = InjuredSuspendedClient.build().getPlayers(type);
            Assert.assertNotNull(players);
            Assert.assertTrue(players.size() > 10);
        }
    }
}

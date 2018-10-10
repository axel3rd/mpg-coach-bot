package org.blondin.mpg.equipeactu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.equipeactu.model.OutType;
import org.blondin.mpg.equipeactu.model.Player;
import org.junit.Assert;
import org.junit.Test;

public class InjuredSuspendedClientTest {

    @Test
    public void testLocalMapping() throws Exception {
        List<Player> players = InjuredSuspendedClient.build()
                .getPlayers(FileUtils.readFileToString(new File("src/test/resources/datas", "equipeactu.ligue-1-1.html")));
        Assert.assertNotNull(players);
        Assert.assertTrue(players.size() > 10);
        for (Player player : players) {
            Assert.assertNotNull(player);
            Assert.assertNotNull(player.getFullNameWithPosition());
            Assert.assertNotNull(player.getFullNameWithPosition(), player.getDescription());
            Assert.assertNotNull(player.getFullNameWithPosition(), player.getLength());
        }
    }

    @Test
    public void testFeatures() throws Exception {
        // Mock
        List<Player> players = InjuredSuspendedClient.build()
                .getPlayers(FileUtils.readFileToString(new File("src/test/resources/datas", "equipeactu.ligue-1-1.html")));

        InjuredSuspendedClient client = mock(InjuredSuspendedClient.class);
        when(client.getPlayers()).thenReturn(players);
        // TODO : With real client OK (except ignoreCase) => problem with mock
        // InjuredSuspendedClient client = InjuredSuspendedClient.build();

        // Test
        Assert.assertNotNull(client.getPlayer("Presnel Kimpembe"));
        Assert.assertNotNull(client.getPlayer("preSnel kimpeMbe"));
        Assert.assertNotNull(client.getPlayer("Kimpembe Presnel"));
        Player p = client.getPlayer("Jesé");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure à la hanche", p.getDescription());
        Assert.assertEquals("Inconnu", p.getLength());

    }

    @Test
    public void testReal() throws Exception {
        List<Player> players = InjuredSuspendedClient.build().getPlayers();
        Assert.assertNotNull(players);
        Assert.assertTrue(players.size() > 10);
    }
}

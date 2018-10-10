package org.blondin.mpg.equipeactu;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
            Assert.assertNotNull(player.getName());
            Assert.assertNotNull(player.getName(), player.getDescription());
            Assert.assertNotNull(player.getName(), player.getLength());
        }
    }
}

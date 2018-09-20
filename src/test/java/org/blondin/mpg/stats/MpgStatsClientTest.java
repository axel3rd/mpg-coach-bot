package org.blondin.mpg.stats;

import org.blondin.mpg.stats.model.Championship;
import org.junit.Assert;
import org.junit.Test;

public class MpgStatsClientTest {

    @Test
    public void testClient() {
        Championship championship = MpgStatsClient.getStats();
        Assert.assertNotNull(championship);
        Assert.assertNotNull(championship.getPlayers());
        Assert.assertTrue(championship.getPlayers().size() > 600);

        // TODO: Some test should be done from src/test/resources/datas/mpgstats.ligue-1.json
    }
}

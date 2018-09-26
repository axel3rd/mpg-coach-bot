package org.blondin.mpg.stats;

import java.io.File;

import org.blondin.mpg.stats.model.Championship;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MpgStatsClientTest {

    @Test
    public void testReal() {
        Championship championship = MpgStatsClient.build().getStats();
        Assert.assertNotNull(championship);
        Assert.assertNotNull(championship.getPlayers());
        Assert.assertTrue(championship.getPlayers().size() > 600);
    }

    @Test
    public void testLocalMapping() throws Exception {
        Championship championship = new ObjectMapper().readValue(new File("src/test/resources/datas", "mpgstats.ligue-1.json"), Championship.class);
        Assert.assertNotNull(championship);
        Assert.assertNotNull(championship.getPlayers());
        Assert.assertTrue(championship.getPlayers().size() > 600);
    }
}

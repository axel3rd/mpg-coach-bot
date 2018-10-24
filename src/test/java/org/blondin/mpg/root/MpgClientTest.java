package org.blondin.mpg.root;

import java.io.File;

import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.Player;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MpgClientTest {

    private static final Config config = Config.build("src/test/resources/mpg.properties");

    @Test
    public void testCoachReal() {
        Coach coach = MpgClient.build(config).getCoach(config.getLeagueTest());
        Assert.assertNotNull(coach);
        Assert.assertNotNull(coach.getPlayers());
        Assert.assertTrue(coach.getPlayers().size() > 10);
    }

    @Test
    public void testCoachLocalMapping() throws Exception {
        Coach coach = new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(new File("src/test/resources/datas", "mpg.coach-1.json"), Coach.class);
        Assert.assertNotNull(coach);
        Assert.assertNotNull(coach.getPlayers());
        Assert.assertTrue(coach.getPlayers().size() > 10);
        for (Player player : coach.getPlayers()) {
            Assert.assertNotNull(player);
            Assert.assertNotNull(player.getId());
            Assert.assertNotNull(player.getPosition());
            Assert.assertNotNull(player.getName(), player.getFirstName());
            Assert.assertNotNull(player.getName(), player.getLastName());
            Assert.assertNotNull(player.getName());
            Assert.assertEquals(player.getName(), (player.getLastName() + " " + player.getFirstName()).trim());
            Assert.assertFalse(player.getName(), player.getName().contains("null"));
        }
    }

    @Test
    public void testDashboardReal() {
        Dashboard dashboard = MpgClient.build(config).getDashboard();
        Assert.assertNotNull(dashboard);
        Assert.assertNotNull(dashboard.getLeagues());
        Assert.assertEquals(config.getLeagueTest(), dashboard.getLeagues().get(0).getId());
        Assert.assertNotNull(dashboard.getLeagues().get(0).getChampionship());

    }

    @Test
    public void testDashboardLocalMapping() throws Exception {
        Dashboard dashboard = new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(new File("src/test/resources/datas", "mpg.dashboard-1.json"), Dashboard.class);
        Assert.assertNotNull(dashboard);
        Assert.assertNotNull(dashboard.getLeagues());
        Assert.assertEquals(config.getLeagueTest(), dashboard.getLeagues().get(0).getId());
        Assert.assertEquals("Rock on the grass", dashboard.getLeagues().get(0).getName());
    }
}

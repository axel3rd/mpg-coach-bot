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
    public void testApiV2L1() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20220327.json")));
        stubFor(get("/leagues/Ligue-1_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.ligue-1.20220327.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        Player p = mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getPlayer("Mbappé");
        Assert.assertEquals(47, p.getPrice());
        Assert.assertEquals(44, p.getAuction().getMin());
        Assert.assertEquals(103, p.getAuction().getAverage());
        Assert.assertEquals(281, p.getAuction().getMax());
        Assert.assertEquals(365, p.getAuction().getNumber());

        Assert.assertEquals(6.02, p.getStats().getAverage(0), 0);
        Assert.assertEquals(3.5, p.getStats().getAverage(1), 0);
        Assert.assertEquals(1, p.getStats().getGoals(2));
        Assert.assertEquals(4, p.getStats().getGoals(6));
        Assert.assertEquals(3, p.getStats().getMatchs(4));
    }

    @Test
    public void testApiV2L2() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20220327.json")));
        stubFor(get("/leagues/Ligue-2_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.ligue-2.20220327.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        Player p = mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_2).getPlayer("van den Boomen");
        Assert.assertEquals(39, p.getPrice());
        Assert.assertEquals(34, p.getAuction().getMin());
        Assert.assertEquals(72, p.getAuction().getAverage());
        Assert.assertEquals(100, p.getAuction().getMax());
        Assert.assertEquals(71, p.getAuction().getNumber());

        Assert.assertEquals(6.61, p.getStats().getAverage(0), 0);
        Assert.assertEquals(7.5, p.getStats().getAverage(2), 0);
        Assert.assertEquals(3, p.getStats().getGoals(2));
        Assert.assertEquals(4, p.getStats().getGoals(4));
        Assert.assertEquals(4, p.getStats().getMatchs(5));
    }

    @Test
    public void testApiV2Liga() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20220327.json")));
        stubFor(get("/leagues/Liga_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.liga.20220327.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        Player p = mpgStatsClient.getStats(ChampionshipStatsType.LIGA).getPlayer("Benzema");
        Assert.assertEquals(50, p.getPrice());
        Assert.assertEquals(49, p.getAuction().getMin());
        Assert.assertEquals(107, p.getAuction().getAverage());
        Assert.assertEquals(205, p.getAuction().getMax());
        Assert.assertEquals(175, p.getAuction().getNumber());

        Assert.assertEquals(6.42, p.getStats().getAverage(0), 0);
        Assert.assertEquals(0, p.getStats().getAverage(1), 0);
        Assert.assertEquals(3, p.getStats().getGoals(3));
        Assert.assertEquals(4, p.getStats().getGoals(4));
        Assert.assertEquals(4, p.getStats().getMatchs(7));
    }

    @Test
    public void testApiV2Pl() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20220327.json")));
        stubFor(get("/leagues/Premier-League_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20220327.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        Player p = mpgStatsClient.getStats(ChampionshipStatsType.PREMIER_LEAGUE).getPlayer("Mohamed Salah");
        Assert.assertEquals(49, p.getPrice());
        Assert.assertEquals(48, p.getAuction().getMin());
        Assert.assertEquals(97, p.getAuction().getAverage());
        Assert.assertEquals(190, p.getAuction().getMax());
        Assert.assertEquals(180, p.getAuction().getNumber());

        Assert.assertEquals(6.18, p.getStats().getAverage(0), 0);
        Assert.assertEquals(6.5, p.getStats().getAverage(1), 0);
        Assert.assertEquals(1, p.getStats().getGoals(3));
        Assert.assertEquals(1, p.getStats().getGoals(6));
        Assert.assertEquals(4, p.getStats().getMatchs(5));
    }

    @Test
    public void testApiV2SerieA() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20220327.json")));
        stubFor(get("/leagues/Serie-A_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.serie-a.20220327.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        Player p = mpgStatsClient.getStats(ChampionshipStatsType.SERIE_A).getPlayer("Vlahovic");
        Assert.assertEquals(41, p.getPrice());
        Assert.assertEquals(41, p.getAuction().getMin());
        Assert.assertEquals(100, p.getAuction().getAverage());
        Assert.assertEquals(150, p.getAuction().getMax());
        Assert.assertEquals(166, p.getAuction().getNumber());

        Assert.assertEquals(5.68, p.getStats().getAverage(0), 0);
        Assert.assertEquals(7, p.getStats().getAverage(1), 0);
        Assert.assertEquals(1, p.getStats().getGoals(3));
        Assert.assertEquals(3, p.getStats().getGoals(6));
        Assert.assertEquals(5, p.getStats().getMatchs(5));
    }

    @Test
    public void testAuction() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20211122.json")));
        stubFor(get("/leagues/Ligue-1_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.ligue-1.20211122.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        Player p = mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getPlayer("Mbappé");
        Assert.assertEquals(43, p.getAuction().getMin());
        Assert.assertEquals(103, p.getAuction().getAverage());
        Assert.assertEquals(199, p.getAuction().getMax());
        Assert.assertEquals(106, p.getAuction().getNumber());
    }

    @Test
    public void testPlayersWithSameName() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20210804.json")));
        stubFor(get("/leagues/Ligue-2_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.ligue-2.20210804.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        Assert.assertEquals(25, mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_2).getPlayer("Ba Pape Ibnou").getPrice());
        Assert.assertEquals(1, mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_2).getInfos().getAnnualStats().getCurrentDay().getDayReached());
        Assert.assertFalse(mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_2).getInfos().getAnnualStats().getCurrentDay().isStatsDayReached());
    }

    @Test
    public void testEfficiencyRecentFocus() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20190406.json")));
        stubFor(get("/leagues/Ligue-1_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.ligue-1.20190406.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        Assert.assertEquals(30, mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getInfos().getAnnualStats().getCurrentDay().getDayReached());
        Assert.assertTrue(mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getInfos().getAnnualStats().getCurrentDay().isStatsDayReached());
        testEfficiencyRecentFocusNeymar(mpgStatsClient);
        testEfficiencyRecentFocusMBappe(mpgStatsClient);
        testEfficiencyRecentFocusTrapp(mpgStatsClient);

    }

    private void testEfficiencyRecentFocusNeymar(MpgStatsClient mpgStatsClient) {
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
    }

    private void testEfficiencyRecentFocusMBappe(MpgStatsClient mpgStatsClient) {
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
    }

    private void testEfficiencyRecentFocusTrapp(MpgStatsClient mpgStatsClient) {
        Player playerTrap = mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getPlayer("Trapp");
        Assert.assertNotNull(playerTrap);
        Assert.assertEquals(0, playerTrap.getStats().getAverage(1), 0);
        Assert.assertEquals(0, playerTrap.getStats().getGoals(1));
        Assert.assertEquals(0, playerTrap.getStats().getMatchs(1));
    }

    @Test
    public void testMockAllLeagues() {
        stubFor(get("/builds.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20181017.json")));
        stubFor(get("/leagues/Ligue-1_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.ligue-1.20181017.json")));
        stubFor(get("/leagues/Premier-League_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20181017.json")));
        stubFor(get("/leagues/Liga_v2.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.liga.20181017.json")));

        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        Assert.assertEquals(8, mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getInfos().getAnnualStats().getCurrentDay().getDayReached());
        Assert.assertTrue(mpgStatsClient.getStats(ChampionshipStatsType.LIGUE_1).getInfos().getAnnualStats().getCurrentDay().isStatsDayReached());

        Assert.assertEquals(8,
                mpgStatsClient.getStats(ChampionshipStatsType.PREMIER_LEAGUE).getInfos().getAnnualStats().getCurrentDay().getDayReached());
        Assert.assertTrue(
                mpgStatsClient.getStats(ChampionshipStatsType.PREMIER_LEAGUE).getInfos().getAnnualStats().getCurrentDay().isStatsDayReached());

        Assert.assertEquals(8, mpgStatsClient.getStats(ChampionshipStatsType.LIGA).getInfos().getAnnualStats().getCurrentDay().getDayReached());
        Assert.assertTrue(mpgStatsClient.getStats(ChampionshipStatsType.LIGA).getInfos().getAnnualStats().getCurrentDay().isStatsDayReached());

        for (ChampionshipStatsType type : Arrays.asList(ChampionshipStatsType.LIGUE_1, ChampionshipStatsType.PREMIER_LEAGUE,
                ChampionshipStatsType.LIGA)) {
            Championship championship = mpgStatsClient.getStats(type);
            subChampionshipTest(championship, type.getValue().toLowerCase());
        }
    }

    @Test
    public void testLocalMapping() throws Exception {
        for (String subFile : Arrays.asList("ligue-1", "premier-league", "liga")) {
            Championship championship = new ObjectMapper().readValue(new File("src/test/resources/__files", "mlnstats." + subFile + ".20181017.json"),
                    Championship.class);
            subChampionshipTest(championship, subFile);
        }
    }

    @Test
    public void testLocalMappingRefresh() throws Exception {
        LeaguesRefresh refresh = new ObjectMapper().readValue(new File("src/test/resources/__files", "mlnstats.builds.20181017.json"),
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
        Assert.assertTrue(String.valueOf(championship.getInfos().getId()), championship.getInfos().getId() > 0);
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

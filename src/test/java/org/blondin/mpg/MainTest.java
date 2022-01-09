package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.InjuredSuspendedWrapperClient;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.stats.MpgStatsClient;
import org.junit.Assert;
import org.junit.Test;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

public class MainTest extends AbstractMockTestClient {

    @Test
    public void testRealIfCredentials() throws Exception {
        try {
            final String config = "src/test/resources/mpg.properties";
            if (new File(config).exists()
                    || (StringUtils.isNoneBlank(System.getenv("MPG_EMAIL")) && StringUtils.isNoneBlank(System.getenv("MPG_PASSWORD")))) {
                Main.main(new String[] { config });
            }
        } catch (ProcessingException e) {
            Assert.assertEquals("No network", "java.net.UnknownHostException: api.mpg.football", e.getMessage());
        }
    }

    @Test
    public void testRealWithBadCredentials() throws Exception {
        try {
            Main.main(new String[] { "src/test/resources/mpg.properties.here" });
            Assert.fail("Credentials are invalid");
        } catch (UnsupportedOperationException e) {
            // Credentials in sample file are fake
            Assert.assertTrue("Bad credentials", e.getMessage().contains("Forbidden URL"));
        } catch (ProcessingException e) {
            // Proxy not configured or real URL not accessible
            Assert.assertEquals("No network", "java.net.UnknownHostException: api.mpg.football", e.getMessage());
        }
    }

    @Test
    public void testLastLiveDay() throws Exception {
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/dashboard/leagues")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.NL931FU9-noNextweek.json")));
        Config config = spy(getConfig());
        executeMainProcess(config);

        Assert.assertTrue(getLogOut(), getLogOut().contains("This is the last live day, no next week"));
    }

    @Test
    public void testCaptainAndBoostPlayerBonus3() throws Exception {
        prepareMainFrenchLigue1Mocks("MLAX7HMK-20211122", "2021", "20211122", "20211122");
        stubFor(get("/division/mpg_division_MLAX7HMK_3_1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.MLAX7HMK.20211122.json")));
        stubFor(get("/team/mpg_team_MLAX7HMK_3_1_6")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLAX7HMK.20211122.json")));
        stubFor(get("/division/mpg_division_MLAX7HMK_3_1/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLAX7HMK.20211122.json")));
        stubFor(put("/match-team-formation/mpg_match_team_formation_MLAX7HMK_3_1_14_5_6")
                .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode()).withHeader("Content-Type", "application/json")
                        .withBody("Fake: mpg_match_team_formation_MLAX7HMK_3_1_14_5_6")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(true).when(config).isEfficiencyRecentFocus();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);

        Assert.assertTrue(getLogOut(), getLogOut().contains("  Captain: Faivre Romain"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("  Bonus  : boostOnePlayer (Blas Ludovic)"));
    }

    @Test
    public void testCaptainAndBoostPlayerBonus2() throws Exception {
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/dashboard/leagues")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.MLMHBPCB-20211122.json")));
        stubFor(get("/division/mpg_division_MLMHBPCB_3_1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.MLMHBPCB.20211122.json")));
        stubFor(get("/team/mpg_team_MLMHBPCB_3_1_4")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLMHBPCB.20211122.json")));
        stubFor(get("/division/mpg_division_MLMHBPCB_3_1/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLMHBPCB.20211122.json")));
        stubFor(get("/championship-players-pool/2")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.poolPlayers.2.2021.json")));
        stubFor(get("/championship-clubs")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.clubs.2021.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/available-players").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.available.players.MLMHBPCB.20211122.json")));
        stubFor(put("/match-team-formation/mpg_match_team_formation_MLMHBPCB_3_1_12_5_4")
                .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode()).withHeader("Content-Type", "application/json")
                        .withBody("Fake: mpg_match_team_formation_MLMHBPCB_3_1_12_5_4")));

        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20211122.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20211122.json")));
        stubFor(get("/injuries/football/england-premier-league/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.premier-league.20211122.html")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(true).when(config).isEfficiencyRecentFocus();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);

        Assert.assertTrue(getLogOut(), getLogOut().contains("  Captain: Rodri"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("  Bonus  : boostOnePlayer (Trossard Leandro)"));
    }

    @Test
    public void testCaptainAndBoostPlayerBonus() throws Exception {
        prepareMainFrenchLigue2Mocks("MLEFEX6G-20211019", "2021", "20211019", "20211019");
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.MLEFEX6G.20211019.json")));
        stubFor(get("/team/mpg_team_MLEFEX6G_3_1_2")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLEFEX6G.20211019.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLEFEX6G.20211019.json")));
        stubFor(put("/match-team-formation/mpg_match_team_formation_MLEFEX6G_3_1_12_5_2")
                .withRequestBody(equalToJson(getTestFileToString("mpg.coach.MLEFEX6G.20211019-Request.json")))
                .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode()).withHeader("Content-Type", "application/json")
                        .withBody("Fake: mpg_match_team_formation_MLEFEX6G_3_1_12_5_2")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);

        Assert.assertTrue(getLogOut(), getLogOut().contains("  Captain: Boissier Remy"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("  Bonus  : boostOnePlayer (Picouleau Mathis)"));
    }

    @Test
    public void testCaptainNotOnMainPitch() throws Exception {
        prepareMainFrenchLigue1Mocks("MLAX7HMK-status-4", "2021", "20210812", "20210812");
        stubFor(get("/division/mpg_division_MLAX7HMK_3_1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.MLAX7HMK.20210812.json")));
        stubFor(get("/team/mpg_team_MLAX7HMK_3_1_6")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLAX7HMK.20210812.manyBonus.json")));
        stubFor(get("/division/mpg_division_MLAX7HMK_3_1/coach").willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withBodyFile("mpg.coach.MLAX7HMK.20210812.withNotOnMainPitchCaptain.json")));
        stubFor(put("/match-team-formation/mpg_match_team_formation_MLAX7HMK_3_1_1_5_6")
                .withRequestBody(equalToJson(getTestFileToString("mpg.coach.MLAX7HMK.20210812.withNotOnMainPitchCaptain-Request.json")))
                .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode()).withHeader("Content-Type", "application/json")
                        .withBody("Fake: mpg_match_team_formation_MLAX7HMK_3_1_1_5_6")));
        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);
        // Day 1 => efficiency with "Recent Focus" or global is the same
        Assert.assertTrue(getLogOut(), getLogOut().contains("| G | Rajkovic Predrag    |  6.00 |"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("| A | Laborde Gaëtan      | 15.40 |"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("  Captain: Faivre Romain"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("  Bonus  : boostOnePlayer (Bamba Jonathan)"));
    }

    @Test
    public void testCaptainAlreadySelected() throws Exception {
        prepareMainFrenchLigue1Mocks("MLAX7HMK-status-4", "2021", "20210812", "20210812");
        stubFor(get("/division/mpg_division_MLAX7HMK_3_1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.MLAX7HMK.20210812.json")));
        stubFor(get("/team/mpg_team_MLAX7HMK_3_1_6")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLAX7HMK.20210812.json")));
        stubFor(get("/division/mpg_division_MLAX7HMK_3_1/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLAX7HMK.20210812.withCaptain.json")));
        stubFor(put("/match-team-formation/mpg_match_team_formation_MLAX7HMK_3_1_1_5_6")
                .withRequestBody(equalToJson(getTestFileToString("mpg.coach.MLAX7HMK.20210812.withCaptain-Request.json")))
                .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode()).withHeader("Content-Type", "application/json")
                        .withBody("Fake: mpg_match_team_formation_MLAX7HMK_3_1_1_5_6")));
        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);
        // Day 1 => efficiency with "Recent Focus" or global is the same
        Assert.assertTrue(getLogOut(), getLogOut().contains("| G | Rajkovic Predrag    |  6.00 |"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("| A | Laborde Gaëtan      | 15.40 |"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Updating team ..."));
        Assert.assertTrue(getLogOut(), getLogOut().contains("  Captain: Botman Sven"));
    }

    @Test
    public void testCaptainAdd() throws Exception {
        prepareMainFrenchLigue1Mocks("MLAX7HMK-status-4", "2021", "20210812", "20210812");
        stubFor(get("/division/mpg_division_MLAX7HMK_3_1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.MLAX7HMK.20210812.json")));
        stubFor(get("/team/mpg_team_MLAX7HMK_3_1_6")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLAX7HMK.20210812.json")));
        stubFor(get("/division/mpg_division_MLAX7HMK_3_1/coach").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLAX7HMK.20210812.withNoCaptain.json")));
        stubFor(put("/match-team-formation/mpg_match_team_formation_MLAX7HMK_3_1_1_5_6")
                .withRequestBody(equalToJson(getTestFileToString("mpg.coach.MLAX7HMK.20210812.withNoCaptain-Request.json")))
                .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode()).withHeader("Content-Type", "application/json")
                        .withBody("Fake: mpg_match_team_formation_MLAX7HMK_3_1_1_5_6")));
        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);
        // Day 1 => efficiency with "Recent Focus" or global is the same
        Assert.assertTrue(getLogOut(), getLogOut().contains("| G | Rajkovic Predrag    |  6.00 |"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("| A | Laborde Gaëtan      | 15.40 |"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Updating team ..."));
        Assert.assertTrue(getLogOut(), getLogOut().contains("  Captain: Bamba Jonathan"));

        // UT player remove from pool
        Assert.assertTrue(getLogOut(), getLogOut()
                .contains("Some player in your team removed because doesn't exist in league pool players: mpg_championship_player_482549"));
    }

    @Test
    public void testMercatoEnd() throws Exception {
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/dashboard/leagues").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.MLAX7HMK-status-3-mercatoEnd.json")));
        Config config = spy(getConfig());
        executeMainProcess(config);
        doReturn(false).when(config).isEfficiencyRecentFocus();
        Assert.assertTrue(getLogOut(), getLogOut().contains("Mercato will be ending, ready for your first match"));
    }

    @Test
    public void testWaitMercatoNextTurnWithConnectionReset() throws Exception {
        // CONNECTION_RESET_BY_PEER doesn't work on Windows (block response), EMPTY_RESPONSE throws a SocketException too
        final String scenario = "Retry Scenario Connection Reset";
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/dashboard/leagues").inScenario(scenario).whenScenarioStateIs(Scenario.STARTED).willSetStateTo("SocketException")
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
        // Don't understand why too fault request necessary to have only one :/
        stubFor(get("/dashboard/leagues").inScenario(scenario).whenScenarioStateIs("SocketException").willSetStateTo("ValidResponse")
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
        stubFor(get("/dashboard/leagues").inScenario(scenario).whenScenarioStateIs("ValidResponse").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.MLAX7HMK-status-3-waitMercatoNextTurn.json")));
        Config config = spy(getConfig());
        doReturn(Arrays.asList("MLAX7HMK")).when(config).getLeaguesInclude();
        doReturn(false).when(config).isEfficiencyRecentFocus();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Mercato round is closed, come back soon for the next !"));
    }

    @Test
    public void testPrepareMercatoTurn0Day0EnglishSerieARecentFocusEnabled() throws Exception {
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/dashboard/leagues").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.NKCDJTKS.MLMHBPCB-status-3.json")));
        stubFor(get("/championship-players-pool/2")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.poolPlayers.2.2021.json")));
        stubFor(get("/championship-players-pool/5")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.poolPlayers.5.2021.json")));
        stubFor(get("/championship-clubs")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.clubs.2021.json")));
        stubFor(get("/division/mpg_division_MLMHBPCB_3_1/available-players").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.available.players.MLMHBPCB.20210813.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20210813.json")));
        stubFor(get("/leagues/Serie-A")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.serie-a.20210813.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20210813.json")));
        stubFor(get("/injuries/football/italy-serie-a/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.serie-a.20210224.html")));
        stubFor(get("/injuries/football/england-premier-league/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.premier-league.20210224.html")));
        Config config = spy(getConfig());
        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("| A | Cristiano Ronaldo       | 313.12 | 44 |"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("| A | Kane Harry             | 214.87 | 47 |"));
    }

    @Test
    public void testPrepareMercatoTurn0Day0Ligue1() throws Exception {
        prepareMainFrenchLigue1Mocks("MLAX7HMK-status-1", "2021", "20210805", "20210805");
        Config config = spy(getConfig());
        doReturn(Arrays.asList("MLAX7HMK")).when(config).getLeaguesInclude();
        doReturn(Arrays.asList("MLEFEX6G")).when(config).getLeaguesExclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Proposal for your coming soon mercato"));

        // When championship not started (incoming day 1 in statistics), the previous year should be taken
        Assert.assertTrue(getLogOut(), getLogOut().contains("| A | Mbappé Kylian        | 166.21 | 40 |   0 |                                    |"));

        // Test some injuries
        Assert.assertTrue(getLogOut(), getLogOut().contains("| D | Maripán Guillermo    |  24.19 | 18 |   0 | INJURY_RED - Leg injury - Mid July |"));
    }

    @Test
    public void testProcessFromEmptyCoach() throws Exception {
        prepareMainFrenchLigue2Mocks("MLEFEX6G-status-4", "2021", "20210804", "20210804");
        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isUseBonus();
        doReturn(true).when(config).isTransactionsProposal();
        doReturn(false).when(config).isEfficiencyRecentFocus();
        doReturn(true).when(config).isDebug();
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.MLEFEX6G.20210804.json")));
        stubFor(get("/team/mpg_team_MLEFEX6G_3_1_2")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLEFEX6G.20210804.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLEFEX6G.20210804.empty.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/available-players").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.available.players.MLEFEX6G.20210804.json")));
        stubFor(put("/match-team-formation/mpg_match_team_formation_MLEFEX6G_3_1_2_2_2")
                .withRequestBody(equalToJson(getTestFileToString("mpg.coach.MLEFEX6G.20210804-Request.json")))
                .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode()).withHeader("Content-Type", "application/json")
                        .withBody("Fake: mpg_match_team_formation_MLEFEX6G_3_1_2_2_2")));
        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Ligue 2 Fous"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Ba"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Updating team"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Transactions proposal"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("| G | Prevot Maxence (mpg_championship_player_220359)   |  4.00 |"));
    }

    private void executeMainProcess(Config config) {
        executeMainProcess(null, null, null, config);
    }

    private void executeMainProcess(MpgClient mpgClient, MpgStatsClient mpgStatsClient, InjuredSuspendedWrapperClient injuredSuspendedClient,
            Config config) {
        Config c = ObjectUtils.defaultIfNull(config, getConfig());
        // ObjectUtils.defaultIfNull could not be used for other, the builders of client should not be called
        MpgClient mpgClientLocal = mpgClient;
        if (mpgClientLocal == null) {
            mpgClientLocal = MpgClient.build(c, "http://localhost:" + server.port());
        }
        MpgStatsClient mpgStatsClientLocal = mpgStatsClient;
        if (mpgStatsClientLocal == null) {
            mpgStatsClientLocal = MpgStatsClient.build(c, "http://localhost:" + getServer().port());
        }
        InjuredSuspendedWrapperClient injuredSuspendedClientLocal = injuredSuspendedClient;
        if (injuredSuspendedClientLocal == null) {
            injuredSuspendedClientLocal = InjuredSuspendedWrapperClient.build(c, "http://localhost:" + getServer().port() + "/injuries/football/",
                    "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/",
                    "http://localhost:" + getServer().port() + "/2020/08/20/joueurs-blesses-et-suspendus/");
        }
        Main.process(ApiClients.build(mpgClientLocal, mpgStatsClientLocal, injuredSuspendedClientLocal), c);
    }

    private static void prepareMainFrenchLigue1Mocks(String dashboard, String poolPlayerYear, String statsLeaguesDate,
            String sportsGamblerDateOrEquipeActu) {
        try {
            if (StringUtils.isBlank(sportsGamblerDateOrEquipeActu)) {
                prepareMainFrenchLigueMocks(dashboard, 1, null, statsLeaguesDate, null, null, null);
                return;
            }
            final SimpleDateFormat dateParser = new SimpleDateFormat("yyyyMMdddd");
            Date sportsGamblerSwitch = dateParser.parse("20201010");
            Date dataFileDate = dateParser.parse(sportsGamblerDateOrEquipeActu);
            if (dataFileDate.after(sportsGamblerSwitch)) {
                prepareMainFrenchLigueMocks(dashboard, 1, poolPlayerYear, statsLeaguesDate, sportsGamblerDateOrEquipeActu, null, null);
            } else {
                prepareMainFrenchLigueMocks(dashboard, 1, poolPlayerYear, statsLeaguesDate, null, sportsGamblerDateOrEquipeActu, null);
            }
        } catch (ParseException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static void prepareMainFrenchLigue2Mocks(String dashboard, String poolPlayerYear, String statsLeaguesDate, String maLigue2Date) {
        prepareMainFrenchLigueMocks(dashboard, 2, poolPlayerYear, statsLeaguesDate, null, null, maLigue2Date);
    }

    private static void prepareMainFrenchLigueMocks(String dashboard, int frenchLigue, String poolPlayerYear, String statsLeaguesDate,
            String sportsGamblerDate, String equipeActuDate, String maLigue2Date) {
        try {
            SimpleDateFormat dateDayFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy");
            stubFor(post("/user/sign-in")
                    .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
            if (StringUtils.isNotBlank(dashboard)) {
                stubFor(get("/dashboard/leagues")
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard." + dashboard + ".json")));
            }
            if (StringUtils.isNotBlank(poolPlayerYear)) {
                dateYearFormat.parse(poolPlayerYear);
                stubFor(get("/championship-players-pool/" + (frenchLigue == 1 ? 1 : 4))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withBodyFile("mpg.poolPlayers." + (frenchLigue == 1 ? 1 : 4) + "." + poolPlayerYear + ".json")));
                stubFor(get("/championship-clubs").willReturn(
                        aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.clubs." + poolPlayerYear + ".json")));
            }
            if (StringUtils.isNotBlank(statsLeaguesDate)) {
                dateDayFormat.parse(statsLeaguesDate);
                stubFor(get("/builds").willReturn(
                        aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds." + statsLeaguesDate + ".json")));
                stubFor(get("/leagues/Ligue-" + frenchLigue).willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("mlnstats.ligue-" + frenchLigue + "." + statsLeaguesDate + ".json")));
            }
            if (StringUtils.isNotBlank(sportsGamblerDate)) {
                dateDayFormat.parse(sportsGamblerDate);
                stubFor(get("/injuries/football/france-ligue-" + frenchLigue + "/")
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withBodyFile("sportsgambler.ligue-" + frenchLigue + "." + sportsGamblerDate + ".html")));
            }
            if (StringUtils.isNotBlank(equipeActuDate)) {
                dateDayFormat.parse(equipeActuDate);
                stubFor(get("/blessures-et-suspensions/fodbold/france/ligue-" + frenchLigue)
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withBodyFile("equipeactu.ligue-" + frenchLigue + "." + equipeActuDate + ".html")));

                // 403 on SportsGambler, to force FallBack on EquipeActu
                stubFor(get("/injuries/football/france-ligue-" + frenchLigue + "/")
                        .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
            }
            if (StringUtils.isNotBlank(maLigue2Date)) {
                stubFor(get("/2020/08/20/joueurs-blesses-et-suspendus/").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("maligue2.joueurs-blesses-et-suspendus." + maLigue2Date + ".html")));
            }
        } catch (ParseException e) {
            throw new UnsupportedOperationException("Input is not in correct date format: " + e.getMessage(), e);
        }
    }

    private String getTestFileToString(String fileName) throws IOException {
        return FileUtils.readFileToString(new File(TESTFILES_BASE, fileName), StandardCharsets.UTF_8);
    }

}

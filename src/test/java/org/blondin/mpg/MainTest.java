package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.ChampionshipOutType;
import org.blondin.mpg.out.InjuredSuspendedEquipeActuClient;
import org.blondin.mpg.out.InjuredSuspendedWrapperClient;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.root.exception.UrlForbiddenException;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.stats.ChampionshipStatsType;
import org.blondin.mpg.stats.MpgStatsClient;
import org.blondin.mpg.stats.model.Championship;
import org.blondin.mpg.test.io.ConsoleTestAppender;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            Assert.assertEquals("No network", "java.net.UnknownHostException: api.monpetitgazon.com", e.getMessage());
        }
    }

    @Test
    public void testRealWithBadCredentials() throws Exception {
        try {
            Main.main(new String[] { "src/test/resources/mpg.properties.here" });
            Assert.fail("Credentials are invalid");
        } catch (UnsupportedOperationException e) {
            // Credentials in sample file are fake
            Assert.assertEquals("Bad credentials",
                    "Unsupported status code: 401 Unauthorized / Content: {\"success\":false,\"error\":\"incorrectPasswordUser\",\"code\":819}",
                    e.getMessage());
        } catch (ProcessingException e) {
            // Proxy not configured or real URL not accessible
            Assert.assertEquals("No network", "java.net.UnknownHostException: api.monpetitgazon.com", e.getMessage());
        }
    }

    @Test
    public void testInjuredSuspendedSportsGamblerFallBackEquipeActu() throws Exception {
        prepareMainFrenchLigueMocks("MLAX7HMK-MLEFEX6G-MN7VSYBM-MLMHBPCB", "20201021", 1, "20201021", null, "20201006", null);
        stubFor(get("/league/MLAX7HMK/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLAX7HMK.20201021.json")));
        // 403 for sportgambler
        stubFor(get("/football/injuries-suspensions/france-ligue-1/").willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));

        // Only L1
        Config config = spy(getConfig());
        doReturn(Arrays.asList("MLAX7HMK")).when(config).getLeaguesInclude();

        executeMainProcess(config);

        // Asserts
        Assert.assertTrue(getLogOut().contains("========== Des Cartons =========="));
        verify(1, getRequestedFor(urlMatching("/football/injuries-suspensions/france-ligue-1/")));
        verify(1, getRequestedFor(urlMatching("/blessures-et-suspensions/fodbold/france/ligue-1")));
    }

    @Test
    public void testInjuredSuspendedSportsGambler() throws Exception {

        // L1 (MLAX7HMK)
        prepareMainFrenchLigue1Mocks("MLAX7HMK-MLEFEX6G-MN7VSYBM-MLMHBPCB", "20201021", "20201021", "20201020");
        stubFor(get("/league/MLAX7HMK/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLAX7HMK.20201021.json")));

        // PL (MLMHBPCB)
        stubFor(get("/league/MLMHBPCB/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLMHBPCB.20201021.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20201021.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.premier-league.20201020.html")));

        // Serie A (MN7VSYBM)
        stubFor(get("/league/MN7VSYBM/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MN7VSYBM.20201021.json")));
        stubFor(get("/leagues/Serie-A")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.serie-a.20201021.json")));
        stubFor(get("/football/injuries-suspensions/italy-serie-a/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.serie-a.20201020.html")));

        // Exclude L2 (in dashboard)
        Config config = spy(getConfig());
        doReturn(Arrays.asList("MLEFEX6G")).when(config).getLeaguesExclude();
        executeMainProcess(config);

        // Ligue 1
        Assert.assertTrue("Verratti Marco injured", getLogOut().contains("Out: Verratti Marco (M - 4.07) - INJURY_RED - Unknown - Early November"));
        Assert.assertTrue("Caqueret Maxence injured", getLogOut().contains("Out: Caqueret Maxence (M - 3.71) - INJURY_RED - COVID-19 - A few weeks"));

        // PL
        Assert.assertTrue("Vardy Jamie injured", getLogOut().contains("Out: Vardy Jamie (A - 32.93) - INJURY_ORANGE - Calf Injury - Doubtful"));

        // Serie A
        Assert.assertTrue("Alex Sandro injured", getLogOut().contains("Out: Alex Sandro (D - 0.00) - INJURY_ORANGE - Thigh injury - Doubtful"));

    }

    @Test
    public void testInjuredLigue1FrenchAccent() throws Exception {
        prepareMainFrenchLigue1Mocks("MLAX7HMK-MLEFEX6G", "20201006", "20201006", "20201006");
        stubFor(get("/league/MLAX7HMK/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLAX7HMK.20201006.json")));

        Config config = spy(getConfig());
        doReturn(Arrays.asList("MLAX7HMK")).when(config).getLeaguesInclude();
        executeMainProcess(config);

        Assert.assertTrue("Michelin Clement injured", getLogOut().contains("Out: Michelin Clement"));
    }

    @Test
    public void testInjuredLigue2FrenchAccent() throws Exception {
        prepareMainFrenchLigue2Mocks("MLAX7HMK-MLEFEX6G", "20201006", "20201006", "20201006");
        stubFor(get("/league/MLEFEX6G/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLEFEX6G.20201006.json")));

        Config config = spy(getConfig());
        doReturn(Arrays.asList("MLEFEX6G")).when(config).getLeaguesInclude();
        executeMainProcess(config);

        Assert.assertTrue("Barthelme Maxime injured", getLogOut().contains("Out: Barthelme Maxime"));
    }

    @Test
    public void testConnectionReset() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));

        // CONNECTION_RESET_BY_PEER doesn't work on Windows (block response), EMPTY_RESPONSE throws a SocketException too
        final String scenario = "Retry Scenario Connection Reset";
        stubFor(get("/user/dashboard").inScenario(scenario).whenScenarioStateIs(Scenario.STARTED).willSetStateTo("SocketException")
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
        // Don't understand why too fault request necessary to have only one :/
        stubFor(get("/user/dashboard").inScenario(scenario).whenScenarioStateIs("SocketException").willSetStateTo("ValidResponse")
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
        stubFor(get("/user/dashboard").inScenario(scenario).whenScenarioStateIs("ValidResponse").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LH9HKBTD-LJV92C9Y-LJT3FXDF.json")));

        Config config = spy(getConfig());
        doReturn(Arrays.asList("LJV92C9Y", "LJT3FXDF", "LH9HKBTD")).when(config).getLeaguesExclude();
        executeMainProcess(config);
        // Log in debug, not testable, so just check process is OK
        Assert.assertTrue(true);
    }

    @Test
    public void testTransactionProposalWithoutStatsFullyReached() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LH9HKBTD-LJV92C9Y-LJT3FXDF.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20191212.json")));
        stubFor(get("/league/LJT3FXDF/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.LJT3FXDF.20200217.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20200106.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20200217.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20191212.html")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(true).when(config).isEfficiencyRecentFocus();
        doReturn(true).when(config).isTransactionsProposal();
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesInclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("========== Peter Ouch  =========="));
        Assert.assertTrue(getLogOut().contains("WARNING: Last day stats have not fully reached! Please retry tomorrow"));
    }

    @Test
    public void testLeagueStartPlayerToKept() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LJV92C9Y-LH9HKBTD-status-6.json")));

        Config config = spy(getConfig());
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesExclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("Some users should select players to kept before Mercato can start, come back soon"));
    }

    @Test
    public void testPlayersStatsLeagueRefreshDateInvalid() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LH9HKBTD-LJV92C9Y-LJT3FXDF.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20191212.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20200106.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20191218.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20191212.html")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(true).when(config).isEfficiencyRecentFocus();
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesInclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("========== Peter Ouch  =========="));
    }

    @Test
    public void testMlnstatsEfficiencyRecentFocus() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LH9HKBTD-LJV92C9Y-LJT3FXDF.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20191212.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20191218.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20191218.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20191212.html")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(true).when(config).isEfficiencyRecentFocus();
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesInclude();
        executeMainProcess(config);

        // Main team
        Assert.assertTrue(getLogOut().contains("| G | Schmeichel Kasper |  5.75 | 24 |"));
        Assert.assertTrue(getLogOut().contains("| G | Pope Nick         |  5.00 | 13 |"));
        Assert.assertTrue(getLogOut().contains("| D | Willems Jetro     | 11.40 | 16 |"));
        Assert.assertTrue(getLogOut().contains("| D | Doherty Matt      | 10.13 | 14 |"));
        Assert.assertTrue(getLogOut().contains("| D | Cresswell Aaron   |  9.38 | 12 |"));
        Assert.assertTrue(getLogOut().contains("| D | David Luiz        |  7.21 | 18 |"));
        Assert.assertTrue(getLogOut().contains("| M | Fleck John        | 31.51 | 16 |"));
        Assert.assertTrue(getLogOut().contains("| M | Maddison James    | 28.01 | 35 |"));
        Assert.assertTrue(getLogOut().contains("| M | Grealish Jack     | 12.39 | 26 |"));
        Assert.assertTrue(getLogOut().contains("| M | Tielemans Youri   | 12.05 | 24 |"));
        Assert.assertTrue(getLogOut().contains("| M | Wilfred Ndidi     |  6.44 | 27 |"));
        Assert.assertTrue(getLogOut().contains("| M | McGinn John       |  5.38 | 23 |"));
        Assert.assertTrue(getLogOut().contains("| M | Hendrick Jeff     |  4.56 | 14 |"));
        Assert.assertTrue(getLogOut().contains("| A | Vardy Jamie       | 90.22 | 50 |"));
        Assert.assertTrue(getLogOut().contains("| A | Mané Sadio        | 32.99 | 53 |"));
        Assert.assertTrue(getLogOut().contains("| A | Gray Andre        |  9.64 | 10 |"));
        Assert.assertTrue(getLogOut().contains("| A | Wesley            |  3.94 | 16 |"));
    }

    @Test
    public void testMlnstatsEfficiencyYearAverage() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LH9HKBTD-LJV92C9Y-LJT3FXDF.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20191212.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20191218.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.premier-league.20191218.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20191212.html")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isEfficiencyRecentFocus();
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesInclude();
        executeMainProcess(config);

        // Main team
        Assert.assertTrue(getLogOut().contains("| G | Schmeichel Kasper |   5.82 | 24 |"));
        Assert.assertTrue(getLogOut().contains("| G | Pope Nick         |   5.29 | 13 |"));
        Assert.assertTrue(getLogOut().contains("| D | Doherty Matt      |  14.61 | 14 |"));
        Assert.assertTrue(getLogOut().contains("| D | Willems Jetro     |  13.92 | 16 |"));
        Assert.assertTrue(getLogOut().contains("| D | Cresswell Aaron   |  13.78 | 12 |"));
        Assert.assertTrue(getLogOut().contains("| D | David Luiz        |  12.28 | 18 |"));
        Assert.assertTrue(getLogOut().contains("| M | Maddison James    |  36.59 | 35 |"));
        Assert.assertTrue(getLogOut().contains("| M | Grealish Jack     |  27.53 | 26 |"));
        Assert.assertTrue(getLogOut().contains("| M | Fleck John        |  26.61 | 16 |"));
        Assert.assertTrue(getLogOut().contains("| M | McGinn John       |  25.15 | 23 |"));
        Assert.assertTrue(getLogOut().contains("| M | Tielemans Youri   |  23.82 | 24 |"));
        Assert.assertTrue(getLogOut().contains("| M | Wilfred Ndidi     |  18.41 | 27 |"));
        Assert.assertTrue(getLogOut().contains("| M | Hendrick Jeff     |  12.87 | 14 |"));
        Assert.assertTrue(getLogOut().contains("| A | Vardy Jamie       | 123.02 | 50 |"));
        Assert.assertTrue(getLogOut().contains("| A | Mané Sadio        |  66.64 | 53 |"));
        Assert.assertTrue(getLogOut().contains("| A | Wesley            |  26.80 | 16 |"));
        Assert.assertTrue(getLogOut().contains("| A | Gray Andre        |  14.21 | 10 |"));
    }

    @Test
    public void testUseBonusInt0() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LH9HKBTD-LJV92C9Y-LJT3FXDF.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20191220.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mlnstats.builds.20191218.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20191220.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20191220.html")));
        stubFor(post("/league/LJT3FXDF/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LJT3FXDF.20191220-Request.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));

        Config config = spy(getConfig());
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesInclude();
        doReturn(true).when(config).isTeampUpdate();
        doReturn(true).when(config).isEfficiencyRecentFocus();
        executeMainProcess(config);
        // Nothing displayed in log about bonus usage
        Assert.assertTrue(getLogOut().contains("========== Peter Ouch  =========="));
    }

    @Test
    public void testUseBonus() throws Exception {
        prepareMainFrenchLigue2Mocks("LH9HKBTD-LJV92C9Y-LJT3FXDF", "20191212", "20191212", "20191212");
        stubFor(get("/league/LH9HKBTD/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LH9HKBTD.20191212.json")));
        stubFor(post("/league/LH9HKBTD/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LH9HKBTD.20191212-Request.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));

        // EquipeActu past data ... the 20191212 was corrupted
        prepareMainFrenchLigue1Mocks("LH9HKBTD-LJV92C9Y-LJT3FXDF", "20191212", "20191212", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20191212.json")));
        stubFor(post("/league/LJV92C9Y/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LJV92C9Y.20191212-Request.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));

        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20191212.json")));
        stubFor(post("/league/LJT3FXDF/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LJT3FXDF.20191212-Request.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20191212.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20191212.html")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(true).when(config).isEfficiencyRecentFocus();
        executeMainProcess(config);
        // Nothing displayed in log about bonus usage
        Assert.assertTrue(getLogOut().contains("========== D2 MAX =========="));
        Assert.assertTrue(getLogOut().contains("========== GWADA BOYS =========="));
        Assert.assertTrue(getLogOut().contains("========== Peter Ouch  =========="));
    }

    @Test
    public void testUseBonusRedBull() throws Exception {
        prepareMainFrenchLigue2Mocks("LH9HKBTD-LJV92C9Y-LJT3FXDF", "20191212", "20191212", "20191212");
        stubFor(get("/league/LH9HKBTD/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LH9HKBTD.20191212.redbull.json")));
        stubFor(post("/league/LH9HKBTD/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LH9HKBTD.20191212.redbull-Request.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));

        Config config = spy(getConfig());
        doReturn(Arrays.asList("LH9HKBTD")).when(config).getLeaguesInclude();
        doReturn(true).when(config).isTeampUpdate();
        doReturn(true).when(config).isEfficiencyRecentFocus();
        executeMainProcess(config);
        // Nothing displayed in log about bonus usage
        Assert.assertTrue(getLogOut().contains("========== D2 MAX =========="));
    }

    @Test
    public void testMultipleDivisions() throws Exception {
        prepareMainFrenchLigue1Mocks("multiple-divisions", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("========== Division 2 =========="));
    }

    @Test
    public void testSkipChampionsLeague() throws Exception {
        prepareMainFrenchLigueMocks("LM65L48T", null, -1, null, null, null, null);
        executeMainProcess();
        Assert.assertTrue(getLogOut().contains("Sorry, Champions League is currently not supported."));
    }

    @Test
    public void testLeaguesInclude2() throws Exception {
        prepareMainFrenchLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20190818.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190818.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190911.html")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Arrays.asList("LJV92C9Y", "LJT3FXDF")).when(config).getLeaguesInclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertTrue(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesInclude1() throws Exception {
        prepareMainFrenchLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Arrays.asList("LJV92C9Y")).when(config).getLeaguesInclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertFalse(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesInclude0() throws Exception {
        prepareMainFrenchLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20190818.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190818.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190911.html")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Collections.emptyList()).when(config).getLeaguesInclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertTrue(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesExclude2() throws Exception {
        prepareMainFrenchLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Arrays.asList("LJV92C9Y", "LJT3FXDF")).when(config).getLeaguesExclude();
        executeMainProcess(config);
        Assert.assertFalse(getLogOut().contains("FAKE L1"));
        Assert.assertFalse(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesExclude1() throws Exception {
        prepareMainFrenchLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesExclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertFalse(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesExclude0() throws Exception {
        prepareMainFrenchLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20190818.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190818.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190911.html")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Collections.emptyList()).when(config).getLeaguesExclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertTrue(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesIncludeAndExclude() throws Exception {
        prepareMainFrenchLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Arrays.asList("LJV92C9Y", "LJT3FXDF")).when(config).getLeaguesInclude();
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesExclude();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertFalse(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testFrenchL2OutPlayersSecond() throws Exception {
        // Jacob (Niort) should be in team
        prepareMainFrenchLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190818", "20190818", "20190823");
        stubFor(get("/league/LH9HKBTD/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LH9HKBTD.20190818.json")));
        stubFor(post("/league/LH9HKBTD/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LH9HKBTD.20190818-Request-2.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("Out: Boissier Remy"));
        Assert.assertTrue(getLogOut().contains("Out: Gastien Johan"));
        Assert.assertFalse(getLogOut().contains("Out: Jacob Valentin"));
    }

    @Test
    public void testFrenchL2OutPlayersFirst() throws Exception {
        // Boissier (Le Mans) should not be in team
        prepareMainFrenchLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LH9HKBTD/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LH9HKBTD.20190818.json")));
        stubFor(post("/league/LH9HKBTD/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LH9HKBTD.20190818-Request-1.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("Out: Boissier Remy"));
        Assert.assertFalse(getLogOut().contains("Out: Martin Florian"));
    }

    @Test
    public void testNoblankOnSubstitutesBench() throws Exception {
        prepareMainFrenchLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20190818.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190818.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190911.html")));
        stubFor(post("/league/LJT3FXDF/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LJT3FXDF.20190818-Request.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));
        stubFor(post("/league/LJV92C9Y/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.LJV92C9Y.20190818-Request.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertTrue(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testMercatoEnding() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.qyOG7BuuZcv-status-3-teamStatus-2.json")));
        executeMainProcess();
        Assert.assertTrue(getLogOut().contains("Mercato will be ending, ready for your first match"));
    }

    @Test
    public void testUpdateTeamPremierLeague532WithTransactionProposalButNotEnabled() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.KJVB6L7C-status-4.json")));
        stubFor(get("/league/KJVB6L7C/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.KJVB6L7C.20190807.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20190806.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190805.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190911.html")));
        stubFor(post("/league/KJVB6L7C/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.KJVB6L7C.20190807-Request.json")))
                .willReturn(aResponse().withBody("{\"success\":\"teamSaved\"}")));

        // This endpoint should not be called, but provide NPE to reproduce the problem
        stubFor(get("/league/KJVB6L7C/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.error.normal.mode.json")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(true).when(config).isTransactionsProposal();
        doReturn(false).when(config).isTacticalSubstitutes();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut()
                .contains("Transaction proposals can not be achieved, you should buy 'MPG expert mode' for this league (very fun, not expensive!)"));
    }

    @Test
    public void testMercatoLeagueStartHeaderClientRequired() throws Exception {
        prepareMainFrenchLigue1Mocks("qyOG7BuuZcv-status-3", "20190806", "20190807", "20190807");

        // Correct mercato request
        stubFor(get("/league/qyOG7BuuZcv/mercato").withHeader("client-version", equalTo(MpgClient.MPG_CLIENT_VERSION)).atPriority(1)
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.qyOG7BuuZcv.20190807.json")));

        // Incorrect mercato request
        stubFor(get("/league/qyOG7BuuZcv/mercato").atPriority(5).willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(403)
                .withBody("{\"success\":false,\"error\":\"needUpdate\",\"code\":854}")));

        executeMainProcess();
        Assert.assertTrue(getLogOut().contains("Proposal for your mercato"));
    }

    @Test
    public void testSerieASupportSeasonStart() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.XXXXXXXX-status-1.json")));
        stubFor(get("/mercato/5")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.serie-a.20190805.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20190805.json")));
        // 'mpgstats.serie-a.20190805.json' file is about Ligue-1 => this is a 'fake' file without stats about player wanted due to transfer
        stubFor(get("/leagues/Serie-A")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.serie-a.20190805.json")));
        stubFor(get("/football/injuries-suspensions/italy-serie-a/").willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/italie/serie-a")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.serie-a.20190805.html")));
        executeMainProcess();
        Assert.assertTrue(getLogOut()
                .contains("| A | Cristiano Ronaldo       | 0.00 | 53 |                                                                  |"));
    }

    @Test
    public void testMercatoSeasonStart() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LJT3FXDF-status-1.json")));
        stubFor(get("/mercato/2")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.premier-league.20190805.json")));
        stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20190805.json")));
        stubFor(get("/leagues/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190805.json")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190911.html")));

        executeMainProcess();
        String logGlobal = getLogOut();
        Assert.assertTrue(logGlobal.indexOf("Vardy Jamie") > logGlobal.indexOf("Mohamed Salah"));
        Assert.assertFalse(logGlobal.contains("Mohamed Salah             | 0.00 | 50 |"));
        Assert.assertTrue(logGlobal.contains("Aubameyang Pierre-Emerick | 143.03"));

        // With focus on last N days
        ConsoleTestAppender.logTestReset();
        Config config = spy(getConfig());
        doReturn(true).when(config).isEfficiencyRecentFocus();
        executeMainProcess(config);
        String logEfficiency = getLogOut();
        Assert.assertFalse(logEfficiency.contains("Mohamed Salah             | 0.00"));
        Assert.assertTrue(logEfficiency.contains("Aubameyang Pierre-Emerick | 35.89"));
    }

    @Test
    public void testLeague2FocusEfficiencySeasonAfter2Days() throws Exception {
        prepareMainFrenchLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190806", "20190806", "20190818");
        stubFor(get("/league/LH9HKBTD/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LH9HKBTD.20190806.json")));

        // After 2 match, Jeannin Mehdi has played only the first one with 7 as note => should have 3.5 for both (global & efficiency focus)
        // Current bug is with efficiency focus, the note is 1.75

        // With global average
        executeMainProcess();
        String logGlobal = getLogOut();
        // Remove WireMock log at begin
        logGlobal = logGlobal.substring(logGlobal.indexOf("=========="));
        Assert.assertTrue(logGlobal.contains("A | Kadewere Tinotenda | 29.90"));
        Assert.assertTrue(logGlobal.contains("G | Jeannin Mehdi      |  3.50"));
        ConsoleTestAppender.logTestReset();

        // With focus efficiency (8 days) on season start (2 day)
        Config config = spy(getConfig());
        doReturn(true).when(config).isEfficiencyRecentFocus();
        executeMainProcess(config);
        String logFocus = getLogOut();
        Assert.assertTrue(logFocus.contains("A | Kadewere Tinotenda | 29.90"));
        Assert.assertTrue(logFocus.contains("G | Jeannin Mehdi      |  3.50"));
        Assert.assertEquals(logGlobal, logFocus);
    }

    @Test
    public void testLeague2FocusEfficiencySeasonStart() throws Exception {
        prepareMainFrenchLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190724", "20190801", "20190818");
        stubFor(get("/league/LH9HKBTD/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LH9HKBTD.20190724.json")));

        // With global average
        executeMainProcess();
        String logGlobal = getLogOut();
        // Remove WireMock log at begin
        logGlobal = logGlobal.substring(logGlobal.indexOf("=========="));
        Assert.assertTrue(logGlobal.contains("Grbic Adrian        | 27.20"));
        ConsoleTestAppender.logTestReset();

        // With focus efficiency (8 days) on season start (1 day)
        Config config = spy(getConfig());
        doReturn(true).when(config).isEfficiencyRecentFocus();
        executeMainProcess(config);
        String logFocus = getLogOut();
        Assert.assertTrue(logFocus.contains("Grbic Adrian        | 27.20"));
        Assert.assertEquals(logGlobal, logFocus);
    }

    @Test
    public void testLeague2NoData() throws Exception {
        prepareMainFrenchLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190724", "20190724", "20190818");
        stubFor(get("/league/LH9HKBTD/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LH9HKBTD.20190724.json")));

        // The efficiency should not be 'infinity' but 0
        Assert.assertEquals(0, MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port()).getStats(ChampionshipStatsType.LIGUE_2)
                .getPlayer("Rodelin Ronny").getEfficiency(), 0);

        // Use average (not existing data)
        executeMainProcess();

        String log = getLogOut();
        // Check that no infinite character on some players.
        Assert.assertFalse(log.contains("∞"));
        Assert.assertTrue(log.contains("| A | Benkaid Hicham      | 0.00 | 17 |"));
        Assert.assertTrue(log.contains("| A | Rodelin Ronny       | 2.01 | 16 |"));
        // Check order efficiency, value/quotation should be used as second criteria
        // Some players are displayed in WARNING because 0 data
        String logTablePlayers = log.substring(log.lastIndexOf("Optimized team"));
        Assert.assertTrue(logTablePlayers.lastIndexOf("Vachoux") > logTablePlayers.lastIndexOf("Gallon"));
        Assert.assertTrue(logTablePlayers.lastIndexOf("Tramoni") > logTablePlayers.lastIndexOf("Jacob"));
        Assert.assertTrue(logTablePlayers.lastIndexOf("Benkaid") > logTablePlayers.lastIndexOf("Rodelin"));
        Assert.assertTrue(logTablePlayers.lastIndexOf("Abdeldjelil") > logTablePlayers.lastIndexOf("Rodelin"));
        Assert.assertTrue(logTablePlayers.lastIndexOf("Abdeldjelil") > logTablePlayers.lastIndexOf("Benkaid"));

        // Use focus on recent efficiency (not existing data)
        Config config = spy(getConfig());
        doReturn(true).when(config).isEfficiencyRecentFocus();
        executeMainProcess(config);
        Assert.assertFalse(log.contains("∞"));
        Assert.assertTrue(log.contains("| A | Benkaid Hicham      | 0.00 | 17 |"));
    }

    @Test
    public void testLeague2InCreation() throws Exception {
        prepareMainFrenchLigue2Mocks("LH9HKBTD-status-1-championship-4", "20190718", "20190718", "20190818");
        stubFor(get("/mercato/4")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.ligue-2.20190718.json")));

        executeMainProcess();
        Assert.assertFalse(getLogOut(), getLogOut().contains("Players to sell"));
    }

    @Test
    public void testEfficienyRecentFocus() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-4", "20190217", "20190217", "20190217");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190217.json")));
        stubFor(get("/league/KX24XMUG/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.KX24XMUG.20190217.json")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTransactionsProposal();
        doReturn(true).when(config).isEfficiencyRecentFocus();

        executeMainProcess(config);
        Assert.assertFalse(getLogOut(), getLogOut().contains("Players to sell"));
        Assert.assertFalse(getLogOut().contains("Last day stats have not fully reached"));
    }

    @Test
    public void testSellBuy() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-4", "20190202", "20190202", "20190202");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190202.json")));
        stubFor(get("/league/KX24XMUG/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.KX24XMUG.20190202.json")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTransactionsProposal();

        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Achille Needle"));
        Assert.assertFalse(getLogOut(), getLogOut().contains("Neymar"));
        Assert.assertFalse(getLogOut().contains("Last day stats have not fully reached"));
    }

    @Test
    public void testSellBuyNoWarningInLog() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-4", "20190217", "20190217", "20190217");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190217.json")));
        stubFor(get("/league/KX24XMUG/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.KX24XMUG.20190217.json")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTransactionsProposal();

        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Achille Needle"));
        Assert.assertFalse(getLogOut(), getLogOut().contains("WARN: Player can't be found in statistics: Wade Paul"));
        Assert.assertFalse(getLogOut().contains("Last day stats have not fully reached"));
    }

    @Test
    public void testProcessWithLocalMapping() throws Exception {
        // Mock initialization
        MpgClient mpgClient = mock(MpgClient.class);
        when(mpgClient.getCoach(anyString())).thenReturn(new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(new File(TESTFILES_BASE, "mpg.coach.20180926.json"), Coach.class));
        when(mpgClient.getDashboard()).thenReturn(new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(new File(TESTFILES_BASE, "mpg.dashboard.KLGXSSUG-status-4.json"), Dashboard.class));

        MpgStatsClient mpgStatsClient = mock(MpgStatsClient.class);
        when(mpgStatsClient.getStats(any()))
                .thenReturn(new ObjectMapper().readValue(new File(TESTFILES_BASE, "mpgstats.ligue-1.20181017.json"), Championship.class));

        InjuredSuspendedEquipeActuClient outPlayersEquipeActuClient = spy(InjuredSuspendedEquipeActuClient.build(null));
        doReturn(FileUtils.readFileToString(new File(TESTFILES_BASE, "equipeactu.ligue-1.20181017.html"), StandardCharsets.UTF_8))
                .when(outPlayersEquipeActuClient).getHtmlContent(ChampionshipOutType.LIGUE_1);
        InjuredSuspendedWrapperClient outPlayersClient = spy(InjuredSuspendedWrapperClient.class);
        doReturn(outPlayersEquipeActuClient).when(outPlayersClient).useDirectlyOnlyForTestGetEquipeActuClient();
        doThrow(UrlForbiddenException.class).when(outPlayersClient).useDirectlyOnlyForTestGetSportsGamblerClient();

        // Test out (on cloned list)
        List<Player> players = new ArrayList<>(mpgClient.getCoach("fake").getPlayers());
        Assert.assertNotNull("Nkunku should be here",
                players.stream().filter(customer -> "Nkunku".equals(customer.getLastName())).findAny().orElse(null));
        Main.completePlayersClubs(players, mpgClient.getCoach("fake").getTeams());
        Main.removeOutPlayers(players, outPlayersClient, ChampionshipOutType.LIGUE_1, false);
        Assert.assertNull("Nkunku should be removed",
                players.stream().filter(customer -> "Nkunku".equals(customer.getLastName())).findAny().orElse(null));

        // Run global process
        executeMainProcess(mpgClient, mpgStatsClient, outPlayersClient, getConfig());
    }

    @Test
    public void testNoTacticalSubstitutes() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-4", "20190211", "20190211", "20190211");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190211.json")));
        stubFor(post("/league/KX24XMUG/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.20190211-Request.json")))
                .inScenario("Retry Scenario").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody("{\"error\":\"badRequest\"}"))
                .willSetStateTo("Cause Success"));
        stubFor(post("/league/KX24XMUG/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.20190211-Request.json")))
                .inScenario("Retry Scenario").whenScenarioStateIs("Cause Success")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.post.success.json")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(true).when(config).isTeampUpdate();

        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Retrying Team update..."));
    }

    @Test
    public void testUpdateTeamRetryFail() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-4", "20190211", "20190211", "20190211");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190211.json")));
        stubFor(post("/league/KX24XMUG/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.20190211-Request.json")))
                .inScenario("Retry Scenario").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody("{\"error\":\"badRequest\"}"))
                .willSetStateTo("Retry-0"));
        for (int i = 0; i <= 15; i++) {
            stubFor(post("/league/KX24XMUG/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.20190211-Request.json")))
                    .inScenario("Retry Scenario").whenScenarioStateIs("Retry-" + i)
                    .willReturn(aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody("{\"error\":\"badRequest\"}"))
                    .willSetStateTo("Retry-" + (i + 1)));
        }
        stubFor(post("/league/KX24XMUG/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.20190211-Request.json")))
                .inScenario("Retry Scenario").whenScenarioStateIs("Retry-16")
                .willReturn(aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody("{\"error\":\"badRequest\"}"))
                .willSetStateTo("Cause Success"));
        stubFor(post("/league/KX24XMUG/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.20190211-Request.json")))
                .inScenario("Retry Scenario").whenScenarioStateIs("Cause Success")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.post.success.json")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(true).when(config).isTeampUpdate();
        try {
            executeMainProcess(config);
            Assert.fail("Should fail, even if 10 retry");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("400 Bad Request"));
        }
    }

    @Test
    public void testProcess2019January() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-4", "20190123", "20190123", "20190123");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190123.json")));
        stubFor(post("/league/KX24XMUG/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.20190123-Request.json")))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.post.success.json")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Updating team" + ""));
    }

    @Test
    public void testProcessWithMockSimple() throws Exception {
        subTestProcessWithMock("KLGXSSUG", "KLGXSSUG-status-4", "20180926", "20181017", "20181017", "20181017");
        Assert.assertTrue("Assert in previous method", true);
    }

    @Test
    public void testProcessWithMockAndNameDifferentInRootAndStats() throws Exception {
        subTestProcessWithMock("KLGXSSUG", "KLGXSSUG-status-4", "20181212", "20181212", "20181212", "20181212");
        Assert.assertTrue("Assert in previous method", true);
    }

    private void subTestProcessWithMock(String leagueId, String fileRootDashboard, String fileRootCoach, String fileStatsLeagues,
            String dataFileStats, String dataFileEquipeActu) {
        prepareMainFrenchLigue1Mocks(fileRootDashboard, fileStatsLeagues, dataFileStats, dataFileEquipeActu);
        stubFor(get("/league/" + leagueId + "/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach." + fileRootCoach + ".json")));

        // Run global process
        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("=========="));
    }

    @Test
    public void testProcessLeagueInCreationAndTerminated() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-1-KLGXSSUG-status-5", "20181220", "20181220", "20181220");
        stubFor(get("/mercato/1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.ligue-1.20181220.json")));

        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("Proposal for your coming soon mercato"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Thauvin Florian"));
    }

    @Test
    public void testProcessLeagueInMercato() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-3-KLGXSSUG-status-5", "20181220", "20181220", "20181220");
        stubFor(get("/league/KX24XMUG/mercato")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.KX24XMUG.20181220.json")));

        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("Proposal for your mercato"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Thauvin Florian"));
    }

    @Test
    public void testProcessLeagueInMercatoTurnClosed() throws Exception {
        prepareMainFrenchLigue1Mocks("KX24XMUG-status-3+1-KLGXSSUG-status-5", null, null, null);

        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("Mercato round is closed, come back soon for the next"));
    }

    @Test
    public void testProcessNoMoreGames() throws Exception {
        prepareMainFrenchLigue1Mocks("KLGXSSUG-status-4", null, null, null);
        stubFor(get("/league/KLGXSSUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.noMoreGames.json")));

        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("No more games in this league"));
    }

    @Test
    public void testProcessUpdateNoPlayersMiroirOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-noPlayers-MiroirOption");
        Assert.assertTrue("Assert in previous method (NoPlayersMiroirOption)", true);
    }

    @Test
    public void testProcessUpdateCompleteNoOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-Complete-NoOption");
        Assert.assertTrue("Assert in previous method (CompleteNoOption)", true);
    }

    @Test
    public void testProcessUpdateNoSubstitutesRotaldoOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-noSubstitutes-RotaldoOption");
        Assert.assertTrue("Assert in previous method (NoSubstitutesRotaldoOption)", true);
    }

    @Test
    public void testProcessUpdateCompleteBoostPlayerWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-Complete-BoostPlayer");
        Assert.assertTrue("Assert in previous method (CompleteBoostPlayer)", true);
    }

    private void executeMainProcess() {
        executeMainProcess(null);
    }

    private void executeMainProcess(Config config) {
        executeMainProcess(null, null, null, config);
    }

    private void subTestProcessUpdateWithMocks(String coachFileWithoutJsonExtension) throws Exception {
        prepareMainFrenchLigue1Mocks("KLGXSSUG-status-4", "20181114", "20181114", "20181114");
        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        doReturn(false).when(config).isUseBonus();
        stubFor(get("/league/KLGXSSUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile(coachFileWithoutJsonExtension + ".json")));
        stubFor(post("/league/KLGXSSUG/coach").withRequestBody(equalToJson(getTestFileToString(coachFileWithoutJsonExtension + "-Request.json")))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.post.success.json")));
        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Updating team"));
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
            injuredSuspendedClientLocal = InjuredSuspendedWrapperClient.build(c,
                    "http://localhost:" + getServer().port() + "/football/injuries-suspensions/",
                    "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/",
                    "http://localhost:" + getServer().port() + "/2019/08/05/joueurs-blesses-et-suspendus/");
        }
        Main.process(ApiClients.build(mpgClientLocal, mpgStatsClientLocal, injuredSuspendedClientLocal), c);
    }

    private static void prepareMainFrenchLigue1Mocks(String fileRootDashboard, String fileStatsLeagues, String dataFileStats,
            String dataFileSportsGamblerOrEquipeActu) {
        try {
            if (StringUtils.isBlank(dataFileSportsGamblerOrEquipeActu)) {
                prepareMainFrenchLigueMocks(fileRootDashboard, fileStatsLeagues, 1, dataFileStats, null, null, null);
                return;
            }
            final SimpleDateFormat dateParser = new SimpleDateFormat("yyyyMMdddd");
            Date sportsGamblerSwitch = dateParser.parse("20201010");
            Date dataFileDate = dateParser.parse(dataFileSportsGamblerOrEquipeActu);
            if (dataFileDate.after(sportsGamblerSwitch)) {
                prepareMainFrenchLigueMocks(fileRootDashboard, fileStatsLeagues, 1, dataFileStats, dataFileSportsGamblerOrEquipeActu, null, null);
            } else {
                prepareMainFrenchLigueMocks(fileRootDashboard, fileStatsLeagues, 1, dataFileStats, null, dataFileSportsGamblerOrEquipeActu, null);
            }
        } catch (ParseException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static void prepareMainFrenchLigue2Mocks(String fileRootDashboard, String fileStatsLeagues, String dataFileStats,
            String dataFileMaLigue2) {
        prepareMainFrenchLigueMocks(fileRootDashboard, fileStatsLeagues, 2, dataFileStats, null, null, dataFileMaLigue2);
    }

    private static void prepareMainFrenchLigueMocks(String fileRootDashboard, String fileStatsLeagues, int ligue, String dataFileStats,
            String dataFileSportsGambler, String dataFileEquipeActu, String dataFileMaLigue2) {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        if (StringUtils.isNotBlank(fileRootDashboard)) {
            stubFor(get("/user/dashboard").willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard." + fileRootDashboard + ".json")));
        }
        if (StringUtils.isNotBlank(fileStatsLeagues)) {
            stubFor(get("/builds").willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBodyFile(getTestFile("mlnstats.builds." + fileStatsLeagues + ".json", "mpgstats.leagues." + fileStatsLeagues + ".json"))));
        }
        if (StringUtils.isNotBlank(dataFileStats)) {
            stubFor(get("/leagues/Ligue-" + ligue).willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile(getTestFile(
                    "mlnstats.ligue-" + ligue + "." + dataFileStats + ".json", "mpgstats.ligue-" + ligue + "." + dataFileStats + ".json"))));
        }
        if (StringUtils.isNotBlank(dataFileSportsGambler)) {
            stubFor(get("/football/injuries-suspensions/france-ligue-" + ligue + "/")
                    .willReturn(aResponse().withHeader("Content-Type", "application/json")
                            .withBodyFile("sportsgambler.ligue-" + ligue + "." + dataFileSportsGambler + ".html")));
        }
        if (StringUtils.isNotBlank(dataFileEquipeActu)) {
            stubFor(get("/blessures-et-suspensions/fodbold/france/ligue-" + ligue).willReturn(aResponse()
                    .withHeader("Content-Type", "application/json").withBodyFile("equipeactu.ligue-" + ligue + "." + dataFileEquipeActu + ".html")));

            // 403 on SportsGambler, to force FallBack on EquipeActu
            stubFor(get("/football/injuries-suspensions/france-ligue-" + ligue + "/")
                    .willReturn(aResponse().withStatus(Response.Status.FORBIDDEN.getStatusCode())));
        }
        if (StringUtils.isNotBlank(dataFileMaLigue2)) {
            stubFor(get("/2019/08/05/joueurs-blesses-et-suspendus/").willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBodyFile("maligue2.joueurs-blesses-et-suspendus." + dataFileMaLigue2 + ".html")));
        }
    }

    /**
     * Get Test file depending the existence
     * 
     * @param choice1 Choice 1
     * @param choice2 Choice 2
     * @return Choice 1 if file exist, otherwise Choice 2 if file exit
     */
    private static String getTestFile(String choice1, String choice2) {
        if (new File(TESTFILES_BASE, choice1).exists()) {
            return choice1;
        }
        if (new File(TESTFILES_BASE, choice2).exists()) {
            return choice2;
        }
        throw new UnsupportedOperationException(
                String.format("Neither files '%s' or '%s' exist in '%s' directory", choice1, choice2, TESTFILES_BASE));
    }

    private String getTestFileToString(String fileName) throws IOException {
        return FileUtils.readFileToString(new File(TESTFILES_BASE, fileName), StandardCharsets.UTF_8);
    }

}

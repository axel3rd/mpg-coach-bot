package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.ProcessingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.ChampionshipOutType;
import org.blondin.mpg.out.InjuredSuspendedEquipeActuClient;
import org.blondin.mpg.out.InjuredSuspendedWrapperClient;
import org.blondin.mpg.root.MpgClient;
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
    public void testSkipChampionsLeague() throws Exception {
        prepareMainLigueMocks("LM65L48T", null, -1, null, null, null);
        executeMainProcess();
        Assert.assertTrue(getLogOut().contains("Sorry, Champions League is currently not supported."));
    }

    @Test
    public void testLeaguesInclude2() throws Exception {
        prepareMainLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20190818.json")));
        stubFor(get("/customteam.json/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190818.json")));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/championship")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190818.html")));

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
        prepareMainLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
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
        prepareMainLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20190818.json")));
        stubFor(get("/customteam.json/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190818.json")));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/championship")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190818.html")));

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
        prepareMainLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Arrays.asList("LJV92C9Y", "LJT3FXDF")).when(config).getLeaguesExcludes();
        executeMainProcess(config);
        Assert.assertFalse(getLogOut().contains("FAKE L1"));
        Assert.assertFalse(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesExclude1() throws Exception {
        prepareMainLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesExcludes();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertFalse(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesExclude0() throws Exception {
        prepareMainLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20190818.json")));
        stubFor(get("/customteam.json/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190818.json")));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/championship")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190818.html")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Collections.emptyList()).when(config).getLeaguesExcludes();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertTrue(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testLeaguesIncludeAndExclude() throws Exception {
        prepareMainLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));

        Config config = spy(getConfig());
        doReturn(false).when(config).isTeampUpdate();
        doReturn(false).when(config).isTacticalSubstitutes();
        doReturn(Arrays.asList("LJV92C9Y", "LJT3FXDF")).when(config).getLeaguesInclude();
        doReturn(Arrays.asList("LJT3FXDF")).when(config).getLeaguesExcludes();
        executeMainProcess(config);
        Assert.assertTrue(getLogOut().contains("FAKE L1"));
        Assert.assertFalse(getLogOut().contains("FAKE PL"));
    }

    @Test
    public void testFrenchL2OutPlayersSecond() throws Exception {
        // Jacob (Niort) should be in team
        prepareMainLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190818", "20190818", "20190823");
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
        prepareMainLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190818", "20190818", "20190818");
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
        prepareMainLigue1Mocks("LJV92C9Y.LJT3FXDF-status-4", "20190818", "20190818", "20190818");
        stubFor(get("/league/LJV92C9Y/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJV92C9Y.20190818.json")));
        stubFor(get("/league/LJT3FXDF/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.LJT3FXDF.20190818.json")));
        stubFor(get("/customteam.json/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190818.json")));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/championship")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190818.html")));
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
    public void testUpdateTeamPremierLeague352WithTransactionProposalButNotEnabled() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.KJVB6L7C-status-4.json")));
        stubFor(get("/league/KJVB6L7C/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.KJVB6L7C.20190807.json")));
        stubFor(get("/leagues.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20190806.json")));
        stubFor(get("/customteam.json/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190805.json")));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/championship")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190805.html")));
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
        prepareMainLigue1Mocks("qyOG7BuuZcv-status-3", "20190806", "20190807", "20190807");

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
        stubFor(get("/leagues.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20190805.json")));
        stubFor(get("/customteam.json/Serie-A")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.serie-a.20190805.json")));
        stubFor(get("/blessures-et-suspensions/fodbold/italie/serie-a")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.serie-a.20190805.html")));
        executeMainProcess();
        Assert.assertTrue(getLogOut().contains("| A | Cristiano Ronaldo       | 0.00 | 53 |        |"));
    }

    @Test
    public void testMercatoSeasonStart() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.LJT3FXDF-status-1.json")));
        stubFor(get("/mercato/2")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.premier-league.20190805.json")));
        stubFor(get("/leagues.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20190805.json")));
        stubFor(get("/customteam.json/Premier-League")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.premier-league.20190805.json")));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/championship")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20190805.html")));

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
        prepareMainLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190806", "20190806", "20190818");
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
        prepareMainLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190724", "20190801", "20190818");
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
        prepareMainLigue2Mocks("LH9HKBTD-status-4-championship-4", "20190724", "20190724", "20190818");
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
        prepareMainLigue2Mocks("LH9HKBTD-status-1-championship-4", "20190718", "20190718", "20190818");
        stubFor(get("/mercato/4")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.ligue-2.20190718.json")));

        executeMainProcess();
        Assert.assertFalse(getLogOut(), getLogOut().contains("Players to sell"));
    }

    @Test
    public void testEfficienyRecentFocus() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-4", "20190217", "20190217", "20190217");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190217.json")));
        stubFor(get("/league/KX24XMUG/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.20190217.json")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTransactionsProposal();
        doReturn(true).when(config).isEfficiencyRecentFocus();

        executeMainProcess(config);
        Assert.assertFalse(getLogOut(), getLogOut().contains("Players to sell"));
    }

    @Test
    public void testSellBuy() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-4", "20190202", "20190202", "20190202");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190202.json")));
        stubFor(get("/league/KX24XMUG/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.20190202.json")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTransactionsProposal();

        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Achille Needle"));
        Assert.assertFalse(getLogOut(), getLogOut().contains("Neymar"));
    }

    @Test
    public void testSellBuyNoWarningInLog() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-4", "20190217", "20190217", "20190217");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190217.json")));
        stubFor(get("/league/KX24XMUG/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.20190217.json")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTransactionsProposal();

        executeMainProcess(config);
        Assert.assertTrue(getLogOut(), getLogOut().contains("Achille Needle"));
        Assert.assertFalse(getLogOut(), getLogOut().contains("WARN: Player can't be found in statistics: Wade Paul"));
    }

    @Test
    public void testProcessWithLocalMapping() throws Exception {
        // Mock initialization
        MpgClient mpgClient = mock(MpgClient.class);
        when(mpgClient.getCoach(anyString())).thenReturn(new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(new File("src/test/resources/__files", "mpg.coach.20180926.json"), Coach.class));
        when(mpgClient.getDashboard()).thenReturn(new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(new File("src/test/resources/__files", "mpg.dashboard.KLGXSSUG-status-4.json"), Dashboard.class));

        MpgStatsClient mpgStatsClient = mock(MpgStatsClient.class);
        when(mpgStatsClient.getStats(any())).thenReturn(
                new ObjectMapper().readValue(new File("src/test/resources/__files", "mpgstats.ligue-1.20181017.json"), Championship.class));

        InjuredSuspendedEquipeActuClient outPlayersEquipeActuClient = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.ligue-1.20181017.html"), Charset.forName("UTF-8")))
                .when(outPlayersEquipeActuClient).getHtmlContent(ChampionshipOutType.LIGUE_1);
        InjuredSuspendedWrapperClient outPlayersClient = spy(InjuredSuspendedWrapperClient.class);
        doReturn(outPlayersEquipeActuClient).when(outPlayersClient).useOnlyForTestGetEquipeActuClient();

        // Test out (on cloned list)
        List<Player> players = new ArrayList<>(mpgClient.getCoach("fake").getPlayers());
        Assert.assertNotNull("Nkunku should be here",
                players.stream().filter(customer -> "Nkunku".equals(customer.getLastName())).findAny().orElse(null));
        Main.removeOutPlayers(players, outPlayersClient, ChampionshipOutType.LIGUE_1, false);
        Assert.assertNull("Nkunku should be removed",
                players.stream().filter(customer -> "Nkunku".equals(customer.getLastName())).findAny().orElse(null));

        // Run global process
        executeMainProcess(mpgClient, mpgStatsClient, outPlayersClient, getConfig());
    }

    @Test
    public void testNoTacticalSubstitutes() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-4", "20190211", "20190211", "20190211");
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
        Assert.assertTrue(getLogOut(), getLogOut().contains("Retrying ..."));
    }

    @Test
    public void testUpdateTeamRetryFail() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-4", "20190211", "20190211", "20190211");
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
        prepareMainLigue1Mocks("KX24XMUG-status-4", "20190123", "20190123", "20190123");
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
        prepareMainLigue1Mocks(fileRootDashboard, fileStatsLeagues, dataFileStats, dataFileEquipeActu);
        stubFor(get("/league/" + leagueId + "/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach." + fileRootCoach + ".json")));

        // Run global process
        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("=========="));
    }

    @Test
    public void testProcessLeagueInCreationAndTerminated() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-1-KLGXSSUG-status-5", "20181220", "20181220", "20181220");
        stubFor(get("/mercato/1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.ligue-1.20181220.json")));

        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("Proposal for your coming soon mercato"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Thauvin Florian"));
    }

    @Test
    public void testProcessLeagueInMercato() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-3-KLGXSSUG-status-5", "20181220", "20181220", "20181220");
        stubFor(get("/league/KX24XMUG/mercato")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.KX24XMUG.20181220.json")));

        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("Proposal for your mercato"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Thauvin Florian"));
    }

    @Test
    public void testProcessLeagueInMercatoTurnClosed() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-3+1-KLGXSSUG-status-5", null, null, null);

        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("Mercato round is closed, come back soon for the next"));
    }

    @Test
    public void testProcessNoMoreGames() throws Exception {
        prepareMainLigue1Mocks("KLGXSSUG-status-4", null, null, null);
        stubFor(get("/league/KLGXSSUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.noMoreGames.json")));

        executeMainProcess();
        Assert.assertTrue(getLogOut(), getLogOut().contains("No more games in this league"));
    }

    @Test
    public void testProcessUpdateNoPlayersMiroirOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-noPlayers-MiroirOption");
        Assert.assertTrue("Assert in previous method", true);
    }

    @Test
    public void testProcessUpdateCompleteNoOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-Complete-NoOption");
        Assert.assertTrue("Assert in previous method", true);
    }

    @Test
    public void testProcessUpdateNoSubstitutesRotaldoOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-noSubstitutes-RotaldoOption");
        Assert.assertTrue("Assert in previous method", true);
    }

    @Test
    public void testProcessUpdateCompleteBoostPlayerWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-Complete-BoostPlayer");
        Assert.assertTrue("Assert in previous method", true);
    }

    private void executeMainProcess() {
        executeMainProcess(null);
    }

    private void executeMainProcess(Config config) {
        executeMainProcess(null, null, null, config);
    }

    private void subTestProcessUpdateWithMocks(String coachFileWithoutJsonExtension) throws Exception {
        prepareMainLigue1Mocks("KLGXSSUG-status-4", "20181114", "20181114", "20181114");
        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
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
                    "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/",
                    "http://localhost:" + getServer().port() + "/2019/08/05/joueurs-blesses-et-suspendus/");
        }
        Main.process(ApiClients.build(mpgClientLocal, mpgStatsClientLocal, injuredSuspendedClientLocal), c);
    }

    private static void prepareMainLigue1Mocks(String fileRootDashboard, String fileStatsLeagues, String dataFileStats, String dataFileEquipeActu) {
        prepareMainLigueMocks(fileRootDashboard, fileStatsLeagues, 1, dataFileStats, dataFileEquipeActu, null);
    }

    private static void prepareMainLigue2Mocks(String fileRootDashboard, String fileStatsLeagues, String dataFileStats, String dataFileMaLigue2) {
        prepareMainLigueMocks(fileRootDashboard, fileStatsLeagues, 2, dataFileStats, null, dataFileMaLigue2);
    }

    private static void prepareMainLigueMocks(String fileRootDashboard, String fileStatsLeagues, int ligue, String dataFileStats,
            String dataFileEquipeActu, String dataFileMaLigue2) {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        if (StringUtils.isNotBlank(fileRootDashboard)) {
            stubFor(get("/user/dashboard").willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard." + fileRootDashboard + ".json")));
        }
        if (StringUtils.isNotBlank(fileStatsLeagues)) {
            stubFor(get("/leagues.json").willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues." + fileStatsLeagues + ".json")));
        }
        if (StringUtils.isNotBlank(dataFileStats)) {
            stubFor(get("/customteam.json/Ligue-" + ligue).willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBodyFile("mpgstats.ligue-" + ligue + "." + dataFileStats + ".json")));
        }
        if (StringUtils.isNotBlank(dataFileEquipeActu)) {
            stubFor(get("/blessures-et-suspensions/fodbold/france/ligue-" + ligue).willReturn(aResponse()
                    .withHeader("Content-Type", "application/json").withBodyFile("equipeactu.ligue-" + ligue + "." + dataFileEquipeActu + ".html")));
        }
        if (StringUtils.isNotBlank(dataFileMaLigue2)) {
            stubFor(get("/2019/08/05/joueurs-blesses-et-suspendus/").willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBodyFile("maligue2.joueurs-blesses-et-suspendus." + dataFileMaLigue2 + ".html")));
        }
    }

    private String getTestFileToString(String fileName) throws IOException {
        return FileUtils.readFileToString(new File("src/test/resources/__files", fileName), Charset.forName("UTF-8"));
    }

}

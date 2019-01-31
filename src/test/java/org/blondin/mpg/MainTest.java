package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import java.util.List;

import javax.ws.rs.ProcessingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.equipeactu.ChampionshipOutType;
import org.blondin.mpg.equipeactu.InjuredSuspendedClient;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.stats.MpgStatsClient;
import org.blondin.mpg.stats.model.Championship;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainTest extends AbstractMockTestClient {

    @Test
    public void testRealIfCredentials() throws Exception {
        try {
            final String config = "src/test/resources/mpg.properties";
            if (new File(config).exists() || (StringUtils.isNoneBlank(System.getenv("MPG_EMAIL"))
                    && StringUtils.isNoneBlank(System.getenv("MPG_PASSWORD")) && StringUtils.isNoneBlank(System.getenv("MPG_LEAGUE_TEST")))) {
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

        InjuredSuspendedClient outPlayersClient = spy(InjuredSuspendedClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.ligue-1.20181017.html"), Charset.defaultCharset()))
                .when(outPlayersClient).getHtmlContent(ChampionshipOutType.LIGUE_1);

        // Test out (on cloned list)
        List<Player> players = new ArrayList<>(mpgClient.getCoach("fake").getPlayers());
        Assert.assertNotNull("Nkunku should be here",
                players.stream().filter(customer -> "Nkunku".equals(customer.getLastName())).findAny().orElse(null));
        Main.removeOutPlayers(players, outPlayersClient, ChampionshipOutType.LIGUE_1);
        Assert.assertNull("Nkunku should be removed",
                players.stream().filter(customer -> "Nkunku".equals(customer.getLastName())).findAny().orElse(null));

        // Run global process
        Main.process(mpgClient, mpgStatsClient, outPlayersClient, getConfig());
    }

    @Test
    public void testProcess2019January() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-4", "20190123", "20190123", "20190123");
        stubFor(get("/league/KX24XMUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20190123.json")));
        stubFor(post("/league/KX24XMUG/coach").withRequestBody(equalToJson(getTestFileToString("mpg.coach.20190123-Request.json")))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.post.success.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, config);

    }

    @Test
    public void testProcessWithMockSimple() throws Exception {
        subTestProcessWithMock("KLGXSSUG", "KLGXSSUG-status-4", "20180926", "20181017", "20181017", "20181017");
    }

    @Test
    public void testProcessWithMockAndNameDifferentInRootAndStats() throws Exception {
        subTestProcessWithMock("KLGXSSUG", "KLGXSSUG-status-4", "20181212", "20181212", "20181212", "20181212");
    }

    private void subTestProcessWithMock(String leagueId, String fileRootDashboard, String fileRootCoach, String fileStatsLeagues,
            String dataFileStats, String dataFileEquipeActu) {
        prepareMainLigue1Mocks(fileRootDashboard, fileStatsLeagues, dataFileStats, dataFileEquipeActu);
        stubFor(get("/league/" + leagueId + "/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach." + fileRootCoach + ".json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");

        // Run global process
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, getConfig());
    }

    @Test
    public void testProcessLeagueInCreationAndTerminated() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-1-KLGXSSUG-status-5", "20181220", "20181220", "20181220");
        stubFor(get("/mercato/1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.ligue-1.20181220.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, getConfig());
    }

    @Test
    public void testProcessLeagueInMercato() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-3-KLGXSSUG-status-5", "20181220", "20181220", "20181220");
        stubFor(get("/league/KX24XMUG/mercato")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.mercato.KX24XMUG.20181220.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, getConfig());
    }

    @Test
    public void testProcessLeagueInMercatoTurnClosed() throws Exception {
        prepareMainLigue1Mocks("KX24XMUG-status-3+1-KLGXSSUG-status-5", null, null, null);
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, getConfig());
    }

    @Test
    public void testProcessNoMoreGames() throws Exception {
        prepareMainLigue1Mocks("KLGXSSUG-status-4", null, null, null);
        stubFor(get("/league/KLGXSSUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.noMoreGames.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, getConfig());
    }

    @Test
    public void testProcessUpdateNoPlayersMiroirOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-noPlayers-MiroirOption");
    }

    @Test
    public void testProcessUpdateCompleteNoOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-Complete-NoOption");
    }

    @Test
    public void testProcessUpdateNoSubstitutesRotaldoOptionWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-noSubstitutes-RotaldoOption");
    }

    @Test
    public void testProcessUpdateCompleteBoostPlayerWithMock() throws Exception {
        subTestProcessUpdateWithMocks("mpg.coach.20181114-Complete-BoostPlayer");
    }

    private void subTestProcessUpdateWithMocks(String coachFileWithoutJsonExtension) throws Exception {
        prepareMainLigue1Mocks("KLGXSSUG-status-4", "20181114", "20181114", "20181114");
        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        stubFor(get("/league/KLGXSSUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile(coachFileWithoutJsonExtension + ".json")));
        stubFor(post("/league/KLGXSSUG/coach").withRequestBody(equalToJson(getTestFileToString(coachFileWithoutJsonExtension + "-Request.json")))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.post.success.json")));
        MpgClient mpgClient = MpgClient.build(config, "http://localhost:" + server.port());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, config);
    }

    private static void prepareMainLigue1Mocks(String fileRootDashboard, String fileStatsLeagues, String dataFileStats, String dataFileEquipeActu) {
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
            stubFor(get("/customteam.json/Ligue-1").willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.ligue-1." + dataFileStats + ".json")));
        }
        if (StringUtils.isNotBlank(dataFileEquipeActu)) {
            stubFor(get("/blessures-et-suspensions/fodbold/france/ligue-1").willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.ligue-1." + dataFileEquipeActu + ".html")));
        }
    }

    private String getTestFileToString(String fileName) throws IOException {
        return FileUtils.readFileToString(new File("src/test/resources/__files", fileName), Charset.defaultCharset());
    }

}

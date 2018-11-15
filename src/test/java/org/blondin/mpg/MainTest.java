package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
            Assert.assertEquals("Bad credentials", "Unsupported status code: 401 Unauthorized", e.getMessage());
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
                .readValue(new File("src/test/resources/__files", "mpg.dashboard.20180926.json"), Dashboard.class));

        MpgStatsClient mpgStatsClient = mock(MpgStatsClient.class);
        when(mpgStatsClient.getStats(any())).thenReturn(
                new ObjectMapper().readValue(new File("src/test/resources/__files", "mpgstats.ligue-1.20181017.json"), Championship.class));

        InjuredSuspendedClient outPlayersClient = spy(InjuredSuspendedClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.ligue-1.20181017.html"))).when(outPlayersClient)
                .getHtmlContent(ChampionshipOutType.LIGUE_1);

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
    public void testProcessWithMock() throws Exception {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/league/KLGXSSUG/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20180926.json")));
        stubFor(get("/user/dashboard")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.20180926.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());

        stubFor(get("/leagues.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20181017.json")));
        stubFor(get("/customteam.json/Ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.ligue-1.20181017.json")));
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());

        stubFor(get("/blessures-et-suspensions/fodbold/france/ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.ligue-1.20181017.html")));
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");

        // Run global process
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, getConfig());
    }

    private static Config prepareProcessUpdateWithMock() {
        stubFor(post("/user/signIn")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/user/dashboard")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.20181114.json")));
        stubFor(get("/leagues.json")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.leagues.20181114.json")));
        stubFor(get("/customteam.json/Ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpgstats.ligue-1.20181114.json")));
        stubFor(get("/blessures-et-suspensions/fodbold/france/ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.ligue-1.20181114.html")));

        Config config = spy(getConfig());
        doReturn(true).when(config).isTeampUpdate();
        return config;
    }

    @Test
    public void testProcessUpdateNoPlayersMiroirOptionWithMock() throws Exception {
        Config config = prepareProcessUpdateWithMock();
        stubFor(get("/league/KLGXSSUG/coach").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.20181114-noPlayers-MiroirOption.json")));
        MpgClient mpgClient = MpgClient.build(config, "http://localhost:" + server.port());
        MpgStatsClient mpgStatsClient = MpgStatsClient.build(getConfig(), "http://localhost:" + getServer().port());
        InjuredSuspendedClient injuredSuspendedClient = InjuredSuspendedClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        Main.process(mpgClient, mpgStatsClient, injuredSuspendedClient, config);
    }

    @Test
    public void testProcessUpdateComleteNoOptionWithMock() throws Exception {
        // TODO
    }

    @Test
    public void testProcessUpdateNoSubstitutesRotaldoOptionWithMock() throws Exception {
        // TODO
    }

    @Test
    public void testProcessUpdateCompleteBoostPlayerWithMock() throws Exception {
        // TODO
    }

}

package org.blondin.mpg.root;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.root.model.TransferBuy;
import org.junit.Assert;
import org.junit.Test;

public class MpgClientTest extends AbstractMockTestClient {

    @Test
    public void testMockSignInKo() throws Exception {
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withStatus(401).withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.bad.json")));
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        try {
            MpgClient.build(config, url);
            Assert.fail("Invalid password is invalid");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Bad credentials",
                    "Unsupported status code: 401 Unauthorized / Content: {\"success\":false,\"error\":\"incorrectPasswordUser\"}", e.getMessage());
        }
    }

    @Test
    public void testMockSignInOk() throws Exception {
        stubFor(post("/user/sign-in")
                .withRequestBody(equalToJson("{\"login\":\"firstName.lastName@gmail.com\",\"password\":\"foobar\",\"language\":\"fr-FR\"}"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Assert.assertTrue(true);
    }

    @Test
    public void testMockCoachMobileApp() throws Exception {
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/coach")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLEFEX6G.20210804.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Coach coach = mpgClient.getCoach("MLEFEX6G");
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
    public void testMockDashboard() throws Exception {
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/dashboard/leagues")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.MLEFEX6G-status-4.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Dashboard dashboard = mpgClient.getDashboard();
        Assert.assertNotNull(dashboard);
        Assert.assertNotNull(dashboard.getLeagues());
        Assert.assertEquals("MLEFEX6G", dashboard.getLeagues().get(0).getId());
        Assert.assertEquals("mpg_division_MLEFEX6G_3_1", dashboard.getLeagues().get(0).getDivisionId());
        Assert.assertEquals("Ligue 2 Fous", dashboard.getLeagues().get(0).getName());
    }

    @Test
    public void testMockTransferBuy() throws Exception {
        stubFor(post("/user/sign-in")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/league/KX24XMUG/transfer/buy")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.transfer.buy.KX24XMUG.20190202.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        TransferBuy tb = mpgClient.getTransferBuy("KX24XMUG");
        Assert.assertEquals(1, tb.getBudget());
        Assert.assertTrue(tb.getAvailablePlayers().size() > 10);
    }
}

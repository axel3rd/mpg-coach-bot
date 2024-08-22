package org.blondin.mpg.out;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.out.model.Position;
import org.junit.Assert;
import org.junit.Test;

import com.github.tomakehurst.wiremock.http.Fault;

public class InjuredSuspendedWrapperClientTest extends AbstractMockTestClient {

    @Test
    public void testType() {
        stubFor(get("/injuries/football/france-ligue-1/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.ligue-1.20230807.html")));
        stubFor(get("/2020/08/20/joueurs-blesses-et-suspendus/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("maligue2.joueurs-blesses-et-suspendus.20230802.html")));

        InjuredSuspendedWrapperClient client = InjuredSuspendedWrapperClient.build(getConfig(), "http://localhost:" + getServer().port() + "/injuries/football/",
                "http://localhost:" + getServer().port() + "/2020/08/20/joueurs-blesses-et-suspendus/");

        Assert.assertNotNull("L1", client.getPlayer(ChampionshipOutType.LIGUE_1, "Sergio Rico", Position.G, "Paris SG"));
        Assert.assertNotNull("L2", client.getPlayer(ChampionshipOutType.LIGUE_2, "Guidi", Position.D, "Bastia"));
    }

    @Test
    public void testFailSportsgambler() {
        stubFor(get("/injuries/football/france-ligue-1/").willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        InjuredSuspendedWrapperClient client = InjuredSuspendedWrapperClient.build(getConfig(), "http://localhost:" + getServer().port() + "/injuries/football/", null);
        Assert.assertNull(client.getPlayer(ChampionshipOutType.LIGUE_1, "Sergio Rico", Position.G, "Paris SG"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("WARN: SportsGambler is unavailable, injured/suspended players not taken into account"));
    }

    @Test
    public void testFailMaligue2() {
        stubFor(get("/2020/08/20/joueurs-blesses-et-suspendus/").willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        InjuredSuspendedWrapperClient client = InjuredSuspendedWrapperClient.build(getConfig(), null, "http://localhost:" + getServer().port() + "/2020/08/20/joueurs-blesses-et-suspendus/");
        Assert.assertNull(client.getPlayer(ChampionshipOutType.LIGUE_2, "Guidi", Position.D, "Bastia"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("WARN: Maligue2.fr is unavailable, L2 injured/suspended players not taken into account"));
    }
}

package org.blondin.mpg.out;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class InjuredSuspendedMaLigue2ClientTest extends AbstractMockTestClient {

    @Test
    public void testCheckTeams2021L2() throws Exception {
        List<String> mpgTeams = Arrays.asList("Ajaccio", "Amiens", "Auxerre", "Bastia", "Caen", "Dijon", "Dunkerque", "Grenoble", "Guingamp",
                "Le Havre", "Nancy", "Nîmes", "Niort", "Paris FC", "Pau", "Quevilly Rouen", "Rodez", "Sochaux", "Toulouse", "Valenciennes");
        Document doc = Jsoup.parse(FileUtils.readFileToString(new File(TESTFILES_BASE, "maligue2.joueurs-blesses-et-suspendus.20210804.html"),
                Charset.forName("UTF-8")));
        List<String> maLigue2Teams = new ArrayList<>();
        for (Element item : doc.select("tr")) {
            if (item.selectFirst("th.column-1") != null && "Club".equals(item.selectFirst("th.column-1").text())) {
                continue;
            }
            maLigue2Teams.add(InjuredSuspendedMaLigue2Client.getMpgTeamName(item.selectFirst("td.column-1").text()));
        }
        for (String mpgTeam : mpgTeams) {
            boolean contains = false;
            for (String maLigue2Team : maLigue2Teams) {
                if (maLigue2Team.contains(mpgTeam)) {
                    contains = true;
                }
            }
            Assert.assertTrue(mpgTeam, contains);
        }
    }

    @Test
    public void testReasons() throws Exception {
        InjuredSuspendedMaLigue2Client client = spy(InjuredSuspendedMaLigue2Client.build(null));
        doReturn(
                FileUtils.readFileToString(new File(TESTFILES_BASE, "maligue2.joueurs-blesses-et-suspendus.20210804.html"), Charset.forName("UTF-8")))
                        .when(client).getHtmlContent();

        Assert.assertNotNull("Absent : Diallo (JO) / AC Ajaccio", client.getPlayer("Diallo", "Ajaccio"));
        Assert.assertEquals("Absent : Diallo (JO) / AC Ajaccio", OutType.ASBENT, client.getPlayer("Diallo", "Ajaccio").getOutType());
        Assert.assertNotNull("Absent : Gioacchini (Gold Cup) / SM Caen", client.getPlayer("Gioacchini", "Caen"));
        Assert.assertEquals("Absent : Gioacchini (Gold Cup) / SM Caen", OutType.ASBENT, client.getPlayer("Gioacchini", "Caen").getOutType());
        Assert.assertNotNull("Injuries : Vandermersch / SM Caen", client.getPlayer("Vandermersch", "Caen"));
        Assert.assertEquals("Injuries : Vandermersch / SM Caen", OutType.INJURY_RED, client.getPlayer("Vandermersch", "Caen").getOutType());
        Assert.assertNotNull("Suspended : Traoré (J3) / Dijon FCO", client.getPlayer("Traore", "Dijon"));
        Assert.assertEquals("Suspended : Traoré (J3) / Dijon FCO", OutType.SUSPENDED, client.getPlayer("Traore", "Dijon").getOutType());
    }

    @Test
    public void testInjuriesSameName() throws Exception {
        InjuredSuspendedMaLigue2Client client = spy(InjuredSuspendedMaLigue2Client.build(null));
        doReturn(
                FileUtils.readFileToString(new File(TESTFILES_BASE, "maligue2.joueurs-blesses-et-suspendus.20210804.html"), Charset.forName("UTF-8")))
                        .when(client).getHtmlContent();

        Assert.assertNull("Ba from Niort is NOT injured", client.getPlayer("Ba", "Niort"));
        Assert.assertNotNull("Ba from Dunkerque is injured", client.getPlayer("Ba", "Dunkerque"));
        Assert.assertNotNull("Ba from Guingamp is injured", client.getPlayer("Ba", "Guingamp"));
    }

    @Test
    public void testCheckTeams2019L2() throws Exception {
        List<String> mpgTeams = Arrays.asList("Ajaccio", "Auxerre", "Caen", "Chambly", "Châteauroux", "Clermont", "Grenoble", "Guingamp", "Le Havre",
                "Le Mans", "Lens", "Lorient", "Nancy", "Niort", "Orléans", "Paris", "Rodez", "Sochaux", "Troyes", "Valenciennes");
        Document doc = Jsoup.parse(FileUtils.readFileToString(new File(TESTFILES_BASE, "maligue2.joueurs-blesses-et-suspendus.20190823.html"),
                Charset.forName("UTF-8")));
        List<String> maLigue2Teams = new ArrayList<>();
        for (Element item : doc.select("tr")) {
            if (item.selectFirst("th.column-1") != null && "Club".equals(item.selectFirst("th.column-1").text())) {
                continue;
            }
            maLigue2Teams.add(item.selectFirst("td.column-1").text());
        }
        for (String mpgTeam : mpgTeams) {
            boolean contains = false;
            for (String maLigue2Team : maLigue2Teams) {
                if (maLigue2Team.contains(mpgTeam)) {
                    contains = true;
                }
            }
            Assert.assertTrue(mpgTeam, contains);
        }
    }

    @Test
    public void testLocalMapping() throws Exception {
        List<Player> players = InjuredSuspendedMaLigue2Client.build(Config.build("src/test/resources/mpg.properties.here")).getPlayers(FileUtils
                .readFileToString(new File(TESTFILES_BASE, "maligue2.joueurs-blesses-et-suspendus.20190818.html"), Charset.forName("UTF-8")));
        Assert.assertNotNull(players);
        Assert.assertEquals(32, players.size());
        for (Player player : players) {
            Assert.assertNotNull(player);
            Assert.assertNotNull(player.getFullNameWithPosition());
            Assert.assertNotNull(player.getFullNameWithPosition(), player.getDescription());
            Assert.assertNotNull(player.getFullNameWithPosition(), player.getLength());
        }
    }

    @Test
    public void testFrenchAccent() throws Exception {
        InjuredSuspendedMaLigue2Client client = spy(InjuredSuspendedMaLigue2Client.build(null));
        doReturn(
                FileUtils.readFileToString(new File(TESTFILES_BASE, "maligue2.joueurs-blesses-et-suspendus.20201006.html"), Charset.forName("UTF-8")))
                        .when(client).getHtmlContent();

        Assert.assertNotNull("Barthelme Maxime injured", client.getPlayer("Barthelme Maxime", "Troyes"));
    }

    @Test
    public void testSomeInjuriesWithNoParentheseEnding() throws Exception {
        InjuredSuspendedMaLigue2Client client = spy(InjuredSuspendedMaLigue2Client.build(null));
        doReturn(
                FileUtils.readFileToString(new File(TESTFILES_BASE, "maligue2.joueurs-blesses-et-suspendus.20190822.html"), Charset.forName("UTF-8")))
                        .when(client).getHtmlContent();

        Assert.assertNotNull("Boissier Remy is injured", client.getPlayer("Boissier Remy", "Le Mans"));
        Assert.assertEquals("Boissier J5", "J5", client.getPlayer("Boissier Remy", "Le Mans").getLength());
        Assert.assertNotNull("Julienne is injured", client.getPlayer("Julienne", "Le Mans"));
    }

    @Test
    public void testSomeInjuries() throws Exception {
        InjuredSuspendedMaLigue2Client client = spy(InjuredSuspendedMaLigue2Client.build(null));
        doReturn(
                FileUtils.readFileToString(new File(TESTFILES_BASE, "maligue2.joueurs-blesses-et-suspendus.20190818.html"), Charset.forName("UTF-8")))
                        .when(client).getHtmlContent();

        Assert.assertNotNull("Boissier Remy is injured", client.getPlayer("Boissier Remy", "Le Mans"));
        Assert.assertNotNull("Valette is injured", client.getPlayer("Valette", "Nancy"));
        Assert.assertNotNull("Seka is injured", client.getPlayer("Seka", "Nancy"));
        Assert.assertNotNull("Boli is injured", client.getPlayer("Saint-Ruf Nicolas", "Orléans"));
        Assert.assertNotNull("Martins Pereira injured", client.getPlayer("Martins Pereira Jonathan", "Lorient"));
        Assert.assertNotNull("Saint-Ruf is injured", client.getPlayer("Boli", "Lens"));
        Assert.assertNull("Tramoni Matteo is not injured", client.getPlayer("Tramoni Matteo", "Ajaccio"));
    }

    @Test
    public void testMock() throws Exception {
        stubFor(get("/2020/08/20/joueurs-blesses-et-suspendus/").willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("maligue2.joueurs-blesses-et-suspendus.20190818.html")));

        InjuredSuspendedMaLigue2Client injuredSuspendedClient = InjuredSuspendedMaLigue2Client.build(getConfig(),
                "http://localhost:" + getServer().port() + "/2020/08/20/joueurs-blesses-et-suspendus/");

        // Remove cache
        File tmpFile = InjuredSuspendedMaLigue2Client
                .getCacheFile("http://localhost:" + getServer().port() + "/2020/08/20/joueurs-blesses-et-suspendus/", "");
        tmpFile.delete();
        Assert.assertFalse(tmpFile.exists());

        List<Player> players = injuredSuspendedClient.getPlayers();
        Assert.assertNotNull(players);
        Assert.assertEquals(32, players.size());

        // Verify cache file has been created, recall and verify date file doesn't change
        Assert.assertTrue(tmpFile.exists());
        long cacheDate = tmpFile.lastModified();
        injuredSuspendedClient = InjuredSuspendedMaLigue2Client.build(getConfig(),
                "http://localhost:" + getServer().port() + "/2020/08/20/joueurs-blesses-et-suspendus/");
        injuredSuspendedClient.getPlayers();
        Assert.assertEquals(cacheDate, tmpFile.lastModified());
    }
}

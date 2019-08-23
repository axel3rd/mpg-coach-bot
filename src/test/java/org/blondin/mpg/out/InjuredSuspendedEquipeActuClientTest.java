package org.blondin.mpg.out;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.blondin.mpg.out.model.Position;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class InjuredSuspendedEquipeActuClientTest extends AbstractMockTestClient {

    private List<String> getEquipeActuTeams(String file) throws IOException {
        Document doc = Jsoup.parse(FileUtils.readFileToString(new File("src/test/resources/__files", file), Charset.defaultCharset()));
        List<String> teams = new ArrayList<>();
        for (Element item : doc.select("h1")) {
            teams.add(item.text().trim());
        }
        teams.remove(0);
        return teams;
    }

    private void testMappingTeams(ChampionshipOutType championship, List<String> mpgTeams, List<String> equipeActuTeams) throws Exception {
        for (String mpgTeam : mpgTeams) {
            boolean contains = false;
            for (String equipeActuTeam : equipeActuTeams) {
                if (equipeActuTeam.contains(InjuredSuspendedEquipeActuClient.getTeamName(championship, mpgTeam))) {
                    contains = true;
                }
            }
            Assert.assertTrue(mpgTeam, contains);
        }
    }

    @Test
    public void testCheckTeamsL1() throws Exception {
        List<String> mpgTeams = Arrays.asList("Amiens", "Angers", "Brest", "Dijon", "Lille", "Lyon", "Marseille", "Metz", "Monaco", "Montpellier",
                "Nantes", "Nice", "Nîmes", "Paris", "Reims", "Rennes", "Saint-Étienne", "Strasbourg", "Toulouse");
        List<String> equipeActuTeams = getEquipeActuTeams("equipeactu.ligue-1.20190818.html");
        testMappingTeams(ChampionshipOutType.LIGUE_1, mpgTeams, equipeActuTeams);
    }

    @Test
    public void testCheckTeamsL2() throws Exception {
        List<String> mpgTeams = Arrays.asList("Ajaccio", "Auxerre", "Caen", "Chambly", "Châteauroux", "Clermont", "Grenoble", "Guingamp", "Le Havre",
                "Le Mans", "Lens", "Lorient", "Nancy", "Niort", "Orléans", "Paris", "Rodez", "Sochaux", "Troyes", "Valenciennes");
        List<String> equipeActuTeams = getEquipeActuTeams("equipeactu.ligue-2.20190724.html");
        testMappingTeams(ChampionshipOutType.LIGUE_2, mpgTeams, equipeActuTeams);
    }

    @Test
    public void testCheckTeamsPL() throws Exception {
        List<String> mpgTeams = Arrays.asList("Arsenal", "Aston Villa", "Bournemouth", "Brighton", "Burnley", "Chelsea", "Crystal Palace", "Everton",
                "Leicester", "Liverpool", "Man. City", "Man. United", "Newcastle", "Norwich", "Sheffield", "Southampton", "Tottenham", "Watford",
                "West Ham", "Wolverhampton");
        List<String> equipeActuTeams = getEquipeActuTeams("equipeactu.premier-league.20190826.html");
        testMappingTeams(ChampionshipOutType.PREMIER_LEAGUE, mpgTeams, equipeActuTeams);
    }

    @Test
    public void testCheckTeamsSerieA() throws Exception {
        List<String> mpgTeams = Arrays.asList("Atalanta", "Bologna", "Brescia", "Cagliari", "Fiorentina", "Genoa", "Inter", "Juventus", "Lazio",
                "Lecce", "Milan", "Napoli", "Parma", "Roma", "Sampdoria", "Sassuolo", "Spal", "Torino", "Udinese", "Verona");
        List<String> equipeActuTeams = getEquipeActuTeams("equipeactu.serie-a.20190805.html");
        testMappingTeams(ChampionshipOutType.SERIE_A, mpgTeams, equipeActuTeams);
    }

    @Test
    public void testCheckTeamsLigA() throws Exception {
        List<String> mpgTeams = Arrays.asList("Alavés", "Atlético", "Barcelona", "Betis", "Bilbao", "Celta", "Eibar", "Espanyol", "Getafe", "Granada",
                "Leganés", "Levante", "Mallorca", "Osasuna", "Real Madrid", "Real Sociedad", "Sevilla", "Valencia", "Valladolid", "Villarreal");
        List<String> equipeActuTeams = getEquipeActuTeams("equipeactu.liga.20190827.html");
        testMappingTeams(ChampionshipOutType.LIGA, mpgTeams, equipeActuTeams);
    }

    @Test
    public void testLocalMapping() throws Exception {
        for (String subFile : Arrays.asList("ligue-1", "premier-league", "liga")) {
            List<Player> players = InjuredSuspendedEquipeActuClient.build(Config.build("src/test/resources/mpg.properties.here")).getPlayers(FileUtils
                    .readFileToString(new File("src/test/resources/__files", "equipeactu." + subFile + ".20181017.html"), Charset.defaultCharset()));
            Assert.assertNotNull(players);
            Assert.assertTrue(players.size() > 10);
            for (Player player : players) {
                Assert.assertNotNull(player);
                Assert.assertNotNull(player.getFullNameWithPosition());
                Assert.assertNotNull(player.getFullNameWithPosition(), player.getDescription());
                Assert.assertNotNull(player.getFullNameWithPosition(), player.getLength());
            }
        }
    }

    @Test
    public void testSomeInjuries() throws Exception {
        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.ligue-1.20190131.html"), Charset.defaultCharset()))
                .when(client).getHtmlContent(ChampionshipOutType.LIGUE_1);

        Assert.assertNotNull("Fares Bahlouli is injured",
                client.getPlayer(ChampionshipOutType.LIGUE_1, "Fares Bahlouli", Position.UNDEFINED, "Lille"));
        Assert.assertNotNull("Neymar is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Neymar", Position.UNDEFINED, "PSG"));
        Assert.assertNotNull("Neymar is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Neymar", Position.A, "PSG"));
        Assert.assertNotNull("Pablo Chavarria is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Pablo Chavarria", Position.A, "Reims"));
        Assert.assertNull("Pablo is not injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Pablo", Position.D, "Bordeaux"));
    }

    @Test
    public void testFeaturesLigue1() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.LIGUE_1;

        // Mock
        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.ligue-1.20181017.html"), Charset.defaultCharset()))
                .when(client).getHtmlContent(ChampionshipOutType.LIGUE_1);

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Presnel Kimpembe", Position.UNDEFINED, "PSG"));
        Assert.assertNotNull(client.getPlayer(c, "preSnel kimpeMbe", Position.UNDEFINED, "PSG"));
        Assert.assertNotNull(client.getPlayer(c, "Kimpembe Presnel", Position.UNDEFINED, "PSG"));

        Player p = client.getPlayer(c, "Jesé", Position.UNDEFINED, "PSG");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure à la hanche (depuis 29/09)", p.getDescription());
        Assert.assertEquals("Inconnu", p.getLength());

        Assert.assertNull(client.getPlayer(c, "Presnel Kimpembe", Position.UNDEFINED, "PSG", OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesPremierLeague() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.PREMIER_LEAGUE;

        // Mock
        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.premier-league.20181017.html"),
                Charset.defaultCharset())).when(client).getHtmlContent(ChampionshipOutType.PREMIER_LEAGUE);

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Yoshinori Muto", Position.UNDEFINED, "Newcastle"));
        Assert.assertNotNull(client.getPlayer(c, "yoshinori muto", Position.UNDEFINED, "Newcastle"));
        Assert.assertNotNull(client.getPlayer(c, "Kenedy", Position.UNDEFINED, "Newcastle"));

        Player p = client.getPlayer(c, "Danilo", Position.UNDEFINED, "Man. City");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure à la cheville (depuis 16/10)", p.getDescription());
        Assert.assertEquals("Inconnu", p.getLength());

        Assert.assertNotNull(client.getPlayer(c, "José Holebas", Position.UNDEFINED, "Watford"));
        Assert.assertNull(client.getPlayer(c, "José Holebas", Position.UNDEFINED, "Watford", OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesLiga() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.LIGA;

        // Mock
        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.class);
        doReturn(FileUtils.readFileToString(new File("src/test/resources/__files", "equipeactu.liga.20181017.html"), Charset.defaultCharset()))
                .when(client).getHtmlContent(ChampionshipOutType.LIGA);

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Unai Bustinza", Position.UNDEFINED, "Leganés"));
        Assert.assertNotNull(client.getPlayer(c, "unai bustinza", Position.UNDEFINED, "Leganés"));
        Assert.assertNotNull(client.getPlayer(c, "CheMa", Position.UNDEFINED, "Levante"));

        Player p = client.getPlayer(c, "Antonio Luna Rodriguez", Position.UNDEFINED, "Levante");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure musculaire (depuis 03/10)", p.getDescription());
        Assert.assertEquals("Au jour le jour", p.getLength());

        Assert.assertNotNull(client.getPlayer(c, "Álvaro Medrán", Position.UNDEFINED, "Rayo Vallecano"));
        Assert.assertNull(client.getPlayer(c, "Álvaro Medrán", Position.UNDEFINED, "Rayo Vallecano", OutType.SUSPENDED, OutType.INJURY_ORANGE));
    }

    @Test
    public void testMock() throws Exception {
        stubFor(get("/blessures-et-suspensions/fodbold/france/ligue-1")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.ligue-1.20181017.html")));
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/championship")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.premier-league.20181017.html")));
        stubFor(get("/blessures-et-suspensions/fodbold/espagne/primera-division")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("equipeactu.liga.20181017.html")));

        InjuredSuspendedEquipeActuClient injuredSuspendedClient = InjuredSuspendedEquipeActuClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");

        // Remove cache
        File tmpFile = InjuredSuspendedEquipeActuClient.getCacheFile("http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/",
                "france/ligue-1");
        tmpFile.delete();
        Assert.assertFalse(tmpFile.exists());

        for (ChampionshipOutType type : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.PREMIER_LEAGUE, ChampionshipOutType.LIGA)) {
            List<Player> players = injuredSuspendedClient.getPlayers(type);
            Assert.assertNotNull(players);
            Assert.assertTrue(String.valueOf(players.size()), players.size() > 10);
        }

        // Verify cache file has been created, recall and verify date file doesn't change
        Assert.assertTrue(tmpFile.exists());
        long cacheDate = tmpFile.lastModified();
        injuredSuspendedClient = InjuredSuspendedEquipeActuClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/blessures-et-suspensions/fodbold/");
        injuredSuspendedClient.getPlayers(ChampionshipOutType.LIGUE_1);
        Assert.assertEquals(cacheDate, tmpFile.lastModified());
    }
}

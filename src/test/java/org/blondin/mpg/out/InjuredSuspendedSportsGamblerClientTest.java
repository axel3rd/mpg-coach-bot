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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.blondin.mpg.out.model.Position;
import org.junit.Assert;
import org.junit.Test;

public class InjuredSuspendedSportsGamblerClientTest extends AbstractMockTestClient {

    private InjuredSuspendedSportsGamblerClient getClientFromFile(ChampionshipOutType type, String date) throws Exception {
        String name = null;
        switch (type) {
        case LIGA:
            name = "liga";
            break;
        case LIGUE_1:
            name = "ligue-1";
            break;
        case LIGUE_2:
            name = "ligue-2";
            break;
        case PREMIER_LEAGUE:
            name = "premier-league";
            break;
        case SERIE_A:
            name = "serie-a";
            break;
        default:
            throw new UnsupportedOperationException("Unknow championship type");
        }

        InjuredSuspendedSportsGamblerClient client = spy(InjuredSuspendedSportsGamblerClient.class);
        doReturn(FileUtils.readFileToString(new File(TESTFILES_BASE, "sportsgambler." + name + "." + date + ".html"), Charset.forName("UTF-8")))
                .when(client).getHtmlContent(type);
        return client;
    }

    private void testMappingTeams(List<String> mpgTeams, ChampionshipOutType championship, String date) throws Exception {
        Collection<String> teams = getTeams(getClientFromFile(championship, date).getPlayers(championship));
        for (String mpgTeam : mpgTeams) {
            boolean contains = false;
            for (String sportsgamblerTeam : teams) {
                if (sportsgamblerTeam.equals(mpgTeam)) {
                    contains = true;
                }
            }
            Assert.assertTrue(mpgTeam + " not in '" + StringUtils.join(teams, ", ") + "'", contains);
        }
    }

    /**
     * Get Teams of injured/suspended players
     * 
     * @param players List of players
     * @return List of Teams
     */
    private Collection<String> getTeams(List<Player> players) {
        Collection<String> teams = new HashSet<>();
        players.stream().forEach(p -> {
            if (StringUtils.isNotBlank(p.getTeam())) {
                teams.add(p.getTeam());
            }
        });
        return teams;
    }

    @Test
    public void testCheckTeams20201020L1() throws Exception {
        List<String> mpgTeams = Arrays.asList("Angers", "Bordeaux", "Brest", "Dijon", "Lens", "Lille", "Lorient", "Lyon", "Marseille", "Metz",
                "Monaco", "Montpellier", "Nantes", "Nice", "Nîmes", "Paris", "Reims", "Rennes", "Saint-Étienne", "Strasbourg");

        // Some teams has no injured at this date
        List<String> mpgTeamsWithoutNoInjured = new ArrayList<String>(mpgTeams);
        mpgTeamsWithoutNoInjured.remove("Lille");
        testMappingTeams(mpgTeamsWithoutNoInjured, ChampionshipOutType.LIGUE_1, "20201020");
    }

    @Test
    public void testCheckTeams20201020PL() throws Exception {
        List<String> mpgTeams = Arrays.asList("Arsenal", "Aston Villa", "Brighton", "Burnley", "Chelsea", "Crystal Palace", "Everton", "Fulham",
                "Leeds", "Leicester", "Liverpool", "Man. City", "Man. United", "Newcastle", "Sheffield", "Southampton", "Tottenham", "West Bromwich",
                "West Ham", "Wolverhampton");

        testMappingTeams(mpgTeams, ChampionshipOutType.PREMIER_LEAGUE, "20201020");
    }

    @Test
    public void testCheckTeams20201020SerieA() throws Exception {
        List<String> mpgTeams = Arrays.asList("Atalanta", "Benevento", "Bologna", "Cagliari", "Crotone", "Fiorentina", "Genoa", "Inter", "Juventus",
                "Lazio", "Milan", "Napoli", "Parma", "Roma", "Sampdoria", "Sassuolo", "Spezia", "Torino", "Udinese", "Verona");

        // Some teams has no injured at this date
        List<String> mpgTeamsWithoutNoInjured = new ArrayList<String>(mpgTeams);
        mpgTeamsWithoutNoInjured.remove("Fiorentina");

        testMappingTeams(mpgTeamsWithoutNoInjured, ChampionshipOutType.SERIE_A, "20201020");
    }

    @Test
    public void testCheckTeams20201020LigA() throws Exception {
        List<String> mpgTeams = Arrays.asList("Alavés", "Atlético", "Barcelona", "Betis", "Bilbao", "Cadix", "Celta", "Eibar", "Elche", "Getafe",
                "Granada", "Huesca", "Levante", "Osasuna", "Real Madrid", "Real Sociedad", "Sevilla", "Valencia", "Valladolid", "Villarreal");

        // Some teams has no injured at this date
        List<String> mpgTeamsWithoutNoInjured = new ArrayList<String>(mpgTeams);
        mpgTeamsWithoutNoInjured.remove("Alavés");

        testMappingTeams(mpgTeamsWithoutNoInjured, ChampionshipOutType.LIGA, "20201020");
    }

    @Test
    public void testLocalMapping() throws Exception {
        for (ChampionshipOutType c : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.PREMIER_LEAGUE, ChampionshipOutType.LIGA,
                ChampionshipOutType.SERIE_A)) {
            List<Player> players = getClientFromFile(c, "20201020").getPlayers(c);
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
    public void testFeaturesLigue1() throws Exception {
        // TODO: Team different name ('-'), "Saint-Etienne" vs "Saint Etienne"

        ChampionshipOutType c = ChampionshipOutType.LIGUE_1;

        // Mock
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(c, "20201020");

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Zinedine Ferhat", Position.UNDEFINED, "Nîmes"));
        Assert.assertNotNull(client.getPlayer(c, "zinEdine ferHat", Position.UNDEFINED, "Nîmes"));
        Assert.assertNotNull(client.getPlayer(c, "Ferhat Zinedine", Position.UNDEFINED, "Nîmes"));

        Player p = client.getPlayer(c, "Kasper Dolberg", Position.UNDEFINED, "Nice");
        Assert.assertNotNull(p);
        Assert.assertEquals("Kasper Dolberg", p.getFullNameWithPosition());
        Assert.assertEquals(OutType.INJURY_RED, p.getOutType());
        Assert.assertEquals("Sprained ankle", p.getDescription());
        Assert.assertEquals("Early November", p.getLength());

        p = client.getPlayer(c, "Angel Di Maria", Position.UNDEFINED, "Paris");
        Assert.assertNotNull(p);
        Assert.assertEquals("Angel Di Maria", p.getFullNameWithPosition());
        Assert.assertEquals(OutType.SUSPENDED, p.getOutType());
        Assert.assertEquals("Disciplinary", p.getDescription());
        Assert.assertEquals("2 games", p.getLength());

        p = client.getPlayer(c, "Timothée Kolodziejczak", Position.UNDEFINED, "Saint-Étienne");
        Assert.assertNotNull(p);
        Assert.assertEquals("Timothee Kolodziejczak", p.getFullNameWithPosition());
        Assert.assertEquals(OutType.SUSPENDED, p.getOutType());
        Assert.assertEquals("Straight red", p.getDescription());
        Assert.assertEquals("1 game", p.getLength());
    }

    @Test
    public void testFeaturesPremierLeague() throws Exception {

        ChampionshipOutType c = ChampionshipOutType.PREMIER_LEAGUE;

        // Mock
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(c, "20201020");

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Vardy Jamie", Position.UNDEFINED, "Leicester"));
        Assert.assertNotNull(client.getPlayer(c, "Jamie Vardy", Position.UNDEFINED, "Leicester"));
        Assert.assertNotNull(client.getPlayer(c, "jaMie VardY", Position.UNDEFINED, "Leicester"));

        Player p = client.getPlayer(c, "Söyüncü Çaglar", Position.UNDEFINED, "Leicester");
        Assert.assertNotNull(p);
        Assert.assertEquals("Caglar Soyuncu", p.getFullNameWithPosition());
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Knock injury", p.getDescription());
        Assert.assertEquals("Doubtful", p.getLength());

        Assert.assertNotNull(client.getPlayer(c, "Anthony Martial", Position.UNDEFINED, "Man. United"));
        Assert.assertNull(client.getPlayer(c, "Anthony Martial", Position.UNDEFINED, "Man. United", OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesLiga() throws Exception {

        ChampionshipOutType c = ChampionshipOutType.LIGA;

        // Mock
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(c, "20201020");

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Saúl Ñíguez", Position.UNDEFINED, "Atlético"));
        Assert.assertNotNull(client.getPlayer(c, "Lodi Renan", Position.UNDEFINED, "Atlético"));

        Player p = client.getPlayer(c, "Álvaro Odriozola", Position.UNDEFINED, "Real Madrid");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Calf Injury", p.getDescription());
        Assert.assertEquals("Doubtful", p.getLength());

    }

    @Test
    public void testFeaturesSerieA() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.SERIE_A;

        // Mock
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(c, "20201020");

        // Test
        Assert.assertNull(client.getPlayer(c, "Alex Sandro", Position.UNDEFINED, "Juventus", OutType.SUSPENDED, OutType.INJURY_ORANGE));
    }

    @Test
    public void testMock() throws Exception {
        stubFor(get("/football/injuries-suspensions/france-ligue-1/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.ligue-1.20201020.html")));
        stubFor(get("/football/injuries-suspensions/england-premier-league/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.premier-league.20201020.html")));
        stubFor(get("/football/injuries-suspensions/spain-la-liga/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.liga.20201020.html")));
        stubFor(get("/football/injuries-suspensions/italy-serie-a/")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.serie-a.20201020.html")));

        InjuredSuspendedSportsGamblerClient injuredSuspendedClient = InjuredSuspendedSportsGamblerClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/football/injuries-suspensions/");

        // Remove cache
        File tmpFile = InjuredSuspendedSportsGamblerClient.getCacheFile("http://localhost:" + getServer().port() + "/football/injuries-suspensions/",
                "france-ligue-1/");
        tmpFile.delete();
        Assert.assertFalse(tmpFile.exists());

        for (ChampionshipOutType type : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.PREMIER_LEAGUE, ChampionshipOutType.LIGA,
                ChampionshipOutType.SERIE_A)) {
            List<Player> players = injuredSuspendedClient.getPlayers(type);
            Assert.assertNotNull(players);
            Assert.assertTrue(String.valueOf(players.size()), players.size() > 10);
        }

        // Verify cache file has been created, recall and verify date file doesn't change
        Assert.assertTrue(tmpFile.exists());
        long cacheDate = tmpFile.lastModified();
        injuredSuspendedClient = InjuredSuspendedSportsGamblerClient.build(getConfig(),
                "http://localhost:" + getServer().port() + "/football/injuries-suspensions/");
        injuredSuspendedClient.getPlayers(ChampionshipOutType.LIGUE_1);
        Assert.assertEquals(cacheDate, tmpFile.lastModified());
    }
}

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

        InjuredSuspendedSportsGamblerClient client = spy(InjuredSuspendedSportsGamblerClient.build(null));
        doReturn(FileUtils.readFileToString(new File(TESTFILES_BASE, "sportsgambler." + name + "." + date + ".html"), Charset.forName("UTF-8"))).when(client).getHtmlContent(type);
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
    public void testCheckTeams20230807SerieA() throws Exception {
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(ChampionshipOutType.SERIE_A, "20230807");
        Assert.assertEquals(19, client.getPlayers(ChampionshipOutType.SERIE_A).size());

        List<String> mpgTeams = Arrays.asList("Atalanta", "Bologna", "Cagliari", "Empoli", "Fiorentina", "Frosinone", "Genoa", "Inter", "Juventus", "Lazio", "Lecce", "Milan", "Monza", "Napoli",
                "Roma", "Salernitana", "Sassuolo", "Torino", "Udinese", "Verona");

        // Some teams has no injured at this date
        List<String> mpgTeamsWithoutNoInjured = new ArrayList<String>(mpgTeams);
        mpgTeamsWithoutNoInjured.remove("Cagliari");
        mpgTeamsWithoutNoInjured.remove("Fiorentina");
        mpgTeamsWithoutNoInjured.remove("Lazio");
        mpgTeamsWithoutNoInjured.remove("Lecce");
        mpgTeamsWithoutNoInjured.remove("Monza");
        mpgTeamsWithoutNoInjured.remove("Salernitana");
        mpgTeamsWithoutNoInjured.remove("Torino");

        testMappingTeams(mpgTeamsWithoutNoInjured, ChampionshipOutType.SERIE_A, "20230807");
    }

    @Test
    public void testCheckTeams20230807LigA() throws Exception {
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(ChampionshipOutType.LIGA, "20230807");
        Assert.assertEquals(20, client.getPlayers(ChampionshipOutType.LIGA).size());

        List<String> mpgTeams = Arrays.asList("Alavés", "Almería", "Athletic", "Atlético", "Barcelona", "Betis", "Celta", "Cádiz", "Getafe", "Girona", "Granada", "Las Palmas", "Mallorca", "Osasuna",
                "Rayo Vallecano", "Real Madrid", "Real Sociedad", "Sevilla", "Valencia", "Villarreal");

        // Some teams has no injured at this date
        List<String> mpgTeamsWithoutNoInjured = new ArrayList<String>(mpgTeams);
        mpgTeamsWithoutNoInjured.remove("Athletic");
        mpgTeamsWithoutNoInjured.remove("Celta");
        mpgTeamsWithoutNoInjured.remove("Alavés");
        mpgTeamsWithoutNoInjured.remove("Granada");
        mpgTeamsWithoutNoInjured.remove("Mallorca");
        mpgTeamsWithoutNoInjured.remove("Rayo Vallecano");
        mpgTeamsWithoutNoInjured.remove("Real Madrid");
        mpgTeamsWithoutNoInjured.remove("Sevilla");

        testMappingTeams(mpgTeamsWithoutNoInjured, ChampionshipOutType.LIGA, "20230807");
    }

    @Test
    public void testCheckTeams20230807PL() throws Exception {
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(ChampionshipOutType.PREMIER_LEAGUE, "20230807");
        Assert.assertEquals(29, client.getPlayers(ChampionshipOutType.PREMIER_LEAGUE).size());

        Assert.assertNotNull(client.getPlayer(ChampionshipOutType.PREMIER_LEAGUE, "Tyrell Malacia", "Man. United"));
        Assert.assertNotNull(client.getPlayer(ChampionshipOutType.PREMIER_LEAGUE, "Tyrell Malacia", Position.D, "Man. United"));
        Assert.assertNotNull(client.getPlayer(ChampionshipOutType.PREMIER_LEAGUE, " Nathan Aké", Position.D, "Man. City"));

        List<String> mpgTeams = Arrays.asList("Arsenal", "Aston Villa", "Bournemouth", "Brentford", "Brighton", "Burnley", "Chelsea", "Crystal Palace", "Everton", "Fulham", "Liverpool", "Luton Town",
                "Man. City", "Man. United", "Newcastle", "Nottigham Forest", "Sheffield", "Tottenham", "West Ham", "Wolverhampton");

        // Some teams has no injured at this date
        List<String> mpgTeamsWithoutNoInjured = new ArrayList<String>(mpgTeams);
        mpgTeamsWithoutNoInjured.remove("Aston Villa");
        mpgTeamsWithoutNoInjured.remove("Brighton");
        mpgTeamsWithoutNoInjured.remove("Luton Town");
        mpgTeamsWithoutNoInjured.remove("Sheffield");
        mpgTeamsWithoutNoInjured.remove("West Ham");
        mpgTeamsWithoutNoInjured.remove("Wolverhampton");

        testMappingTeams(mpgTeamsWithoutNoInjured, ChampionshipOutType.PREMIER_LEAGUE, "20230807");
    }

    @Test
    public void testCheckTeamsAndParsing20230807L1() throws Exception {
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(ChampionshipOutType.LIGUE_1, "20230807");
        Assert.assertEquals(6, client.getPlayers(ChampionshipOutType.LIGUE_1).size());

        Assert.assertNotNull(client.getPlayer(ChampionshipOutType.LIGUE_1, "Leonardo Balerdi", Position.D, "OM", OutType.ASBENT, OutType.INJURY_GREEN, OutType.INJURY_ORANGE, OutType.INJURY_RED));
        Assert.assertNotNull(client.getPlayer(ChampionshipOutType.LIGUE_1, "Breel Embolo", Position.A, "AS Monaco"));
        Assert.assertNotNull(client.getPlayer(ChampionshipOutType.LIGUE_1, "Sergio Rico", Position.G, "Paris SG"));
        Assert.assertNotNull(client.getPlayer(ChampionshipOutType.LIGUE_1, "Emmanuel Agbadou", Position.D, "Reims"));
        Assert.assertNotNull(client.getPlayer(ChampionshipOutType.LIGUE_1, "Ludovic Blas", Position.M, "Rennes"));

        List<String> mpgTeams = Arrays.asList("AS Monaco", "OM", "Paris SG", "Reims", "Rennes", "Strasbourg");
        testMappingTeams(mpgTeams, ChampionshipOutType.LIGUE_1, "20230807");

        Assert.assertEquals("Brest", client.getMpgTeamName("Brest"));
        Assert.assertEquals("Clermont", client.getMpgTeamName("Clermont"));
        Assert.assertEquals("Havre AC", client.getMpgTeamName("Le Havre"));
        Assert.assertEquals("RC Lens", client.getMpgTeamName("Lens"));
        Assert.assertEquals("LOSC", client.getMpgTeamName("Lille"));
        Assert.assertEquals("FC Lorient", client.getMpgTeamName("Lorient"));
        Assert.assertEquals("OL", client.getMpgTeamName("Lyon"));
        Assert.assertEquals("OM", client.getMpgTeamName("Marseille"));
        Assert.assertEquals("FC Metz", client.getMpgTeamName("Metz"));
        Assert.assertEquals("AS Monaco", client.getMpgTeamName("Monaco"));
        Assert.assertEquals("Montpellier", client.getMpgTeamName("Montpellier"));
        Assert.assertEquals("FC Nantes", client.getMpgTeamName("Nantes"));
        Assert.assertEquals("OGC Nice", client.getMpgTeamName("Nice"));
        Assert.assertEquals("Paris SG", client.getMpgTeamName("PSG"));
        Assert.assertEquals("Reims", client.getMpgTeamName("Reims"));
        Assert.assertEquals("Rennes", client.getMpgTeamName("Rennes"));
        Assert.assertEquals("Strasbourg", client.getMpgTeamName("Strasbourg"));
        Assert.assertEquals("Toulouse FC", client.getMpgTeamName("Toulouse"));
    }

    @Test
    public void testCheckTeams20210224SerieA() throws Exception {
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(ChampionshipOutType.SERIE_A, "20210224");
        Assert.assertEquals(69, client.getPlayers(ChampionshipOutType.SERIE_A).size());

        List<String> mpgTeams = Arrays.asList("Atalanta", "Benevento", "Bologna", "Cagliari", "Crotone", "Fiorentina", "Genoa", "Inter", "Juventus", "Lazio", "Milan", "Napoli", "Parma", "Roma",
                "Sampdoria", "Sassuolo", "Spezia", "Torino", "Udinese", "Verona");
        testMappingTeams(mpgTeams, ChampionshipOutType.SERIE_A, "20210224");
    }

    @Test
    public void testCheckTeams20201020SerieA() throws Exception {
        List<String> mpgTeams = Arrays.asList("Atalanta", "Benevento", "Bologna", "Cagliari", "Crotone", "Fiorentina", "Genoa", "Inter", "Juventus", "Lazio", "Milan", "Napoli", "Parma", "Roma",
                "Sampdoria", "Sassuolo", "Spezia", "Torino", "Udinese", "Verona");

        // Some teams has no injured at this date
        List<String> mpgTeamsWithoutNoInjured = new ArrayList<String>(mpgTeams);
        mpgTeamsWithoutNoInjured.remove("Fiorentina");

        testMappingTeams(mpgTeamsWithoutNoInjured, ChampionshipOutType.SERIE_A, "20201020");
    }

    @Test
    public void testLocalMapping() throws Exception {
        for (ChampionshipOutType c : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.PREMIER_LEAGUE, ChampionshipOutType.LIGA, ChampionshipOutType.SERIE_A)) {
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

        ChampionshipOutType c = ChampionshipOutType.LIGUE_1;

        // Mock
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(c, "20201020");

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Zinedine Ferhat", "Nîmes"));
        Assert.assertNotNull(client.getPlayer(c, "zinEdine ferHat", "Nîmes"));
        Assert.assertNotNull(client.getPlayer(c, "Ferhat Zinedine", "Nîmes"));

        Player p = client.getPlayer(c, "Angel Di Maria", "Paris");
        Assert.assertNotNull(p);
        Assert.assertEquals("Angel Di Maria", p.getFullNameWithPosition());
        Assert.assertEquals(OutType.SUSPENDED, p.getOutType());
        Assert.assertEquals("Disciplinary", p.getDescription());
        Assert.assertEquals("2 games", p.getLength());

        p = client.getPlayer(c, "Timothée Kolodziejczak", "Saint-Étienne");
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
        Assert.assertNotNull(client.getPlayer(c, "Vardy Jamie", "Leicester"));
        Assert.assertNotNull(client.getPlayer(c, "Jamie Vardy", "Leicester"));
        Assert.assertNotNull(client.getPlayer(c, "jaMie VardY", "Leicester"));

        Player p = client.getPlayer(c, "Söyüncü Çaglar", "Leicester");
        Assert.assertNotNull(p);
        Assert.assertEquals("Caglar Soyuncu", p.getFullNameWithPosition());
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Knock injury", p.getDescription());
        Assert.assertEquals("Doubtful", p.getLength());

        Assert.assertNotNull(client.getPlayer(c, "Anthony Martial", "Man. United"));
        Assert.assertNull(client.getPlayer(c, "Anthony Martial", "Man. United", OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesLiga() throws Exception {

        ChampionshipOutType c = ChampionshipOutType.LIGA;

        // Mock
        InjuredSuspendedSportsGamblerClient client = getClientFromFile(c, "20201020");

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Saúl Ñíguez", "Atlético"));
        Assert.assertNotNull(client.getPlayer(c, "Lodi Renan", "Atlético"));

        Player p = client.getPlayer(c, "Álvaro Odriozola", "Real Madrid");
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
        Assert.assertNull(client.getPlayer(c, "Alex Sandro", "Juventus", OutType.SUSPENDED, OutType.INJURY_ORANGE));
    }

    @Test
    public void testMock() throws Exception {
        stubFor(get("/injuries/football/france-ligue-1/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.ligue-1.20201020.html")));
        stubFor(get("/injuries/football/england-premier-league/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.premier-league.20201020.html")));
        stubFor(get("/injuries/football/spain-la-liga/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.liga.20201020.html")));
        stubFor(get("/injuries/football/italy-serie-a/").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("sportsgambler.serie-a.20201020.html")));

        InjuredSuspendedSportsGamblerClient injuredSuspendedClient = InjuredSuspendedSportsGamblerClient.build(getConfig(), "http://localhost:" + getServer().port() + "/injuries/football/");

        // Remove cache
        File tmpFile = InjuredSuspendedSportsGamblerClient.getCacheFile("http://localhost:" + getServer().port() + "/injuries/football/", "france-ligue-1/");
        tmpFile.delete();
        Assert.assertFalse(tmpFile.exists());

        for (ChampionshipOutType type : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.PREMIER_LEAGUE, ChampionshipOutType.LIGA, ChampionshipOutType.SERIE_A)) {
            List<Player> players = injuredSuspendedClient.getPlayers(type);
            Assert.assertNotNull(players);
            Assert.assertTrue(String.valueOf(players.size()), players.size() > 10);
        }

        // Verify cache file has been created, recall and verify date file doesn't change
        Assert.assertTrue(tmpFile.exists());
        long cacheDate = tmpFile.lastModified();
        injuredSuspendedClient = InjuredSuspendedSportsGamblerClient.build(getConfig(), "http://localhost:" + getServer().port() + "/injuries/football/");
        injuredSuspendedClient.getPlayers(ChampionshipOutType.LIGUE_1);
        Assert.assertEquals(cacheDate, tmpFile.lastModified());
    }
}

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

public class InjuredSuspendedEquipeActuClientTest extends AbstractMockTestClient {

    private InjuredSuspendedEquipeActuClient getClientFromFile(ChampionshipOutType type, String date) throws Exception {
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

        InjuredSuspendedEquipeActuClient client = spy(InjuredSuspendedEquipeActuClient.build(null));
        doReturn(FileUtils.readFileToString(new File(TESTFILES_BASE, "equipeactu." + name + "." + date + ".html"), Charset.forName("UTF-8")))
                .when(client).getHtmlContent(type);
        return client;
    }

    private void testMappingTeams(List<String> mpgTeams, ChampionshipOutType championship, String date) throws Exception {
        Collection<String> teams = getTeams(getClientFromFile(championship, date).getPlayers(championship));
        for (String mpgTeam : mpgTeams) {
            boolean contains = false;
            for (String equipeActuTeam : teams) {
                if (equipeActuTeam.equals(mpgTeam)) {
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
    public void testFrenchAccent() throws Exception {
        InjuredSuspendedEquipeActuClient clientl1 = getClientFromFile(ChampionshipOutType.LIGUE_1, "20201006");
        Assert.assertNotNull("Michelin Clement injured", clientl1.getPlayer(ChampionshipOutType.LIGUE_1, "Michelin Clement", Position.D, "Lens"));
    }

    @Test
    public void testParseWithoutTeamName() throws Exception {
        InjuredSuspendedEquipeActuClient clientl1 = getClientFromFile(ChampionshipOutType.LIGUE_1, "20191220");
        Assert.assertNotNull("Presnel Kimpembe is injured", clientl1.getPlayer(ChampionshipOutType.LIGUE_1, "Presnel Kimpembe", Position.D, "Paris"));

        InjuredSuspendedEquipeActuClient clientpl = getClientFromFile(ChampionshipOutType.PREMIER_LEAGUE, "20191220");
        Assert.assertNotNull("Pogba is injured", clientpl.getPlayer(ChampionshipOutType.PREMIER_LEAGUE, "Pogba", Position.M, "Man. United"));

        InjuredSuspendedEquipeActuClient clientligua = getClientFromFile(ChampionshipOutType.LIGA, "20191220");
        Assert.assertNotNull("Tomas Pina is injured", clientligua.getPlayer(ChampionshipOutType.LIGA, "Tomas Pina", Position.M, "Alavés"));
    }

    @Test
    public void testCheckTeams20182019L1() throws Exception {
        List<String> mpgTeams = Arrays.asList("Amiens", "Angers", "Brest", "Dijon", "Lille", "Lyon", "Marseille", "Metz", "Monaco", "Montpellier",
                "Nantes", "Nice", "Nîmes", "Paris", "Reims", "Rennes", "Saint-Étienne", "Strasbourg", "Toulouse");
        testMappingTeams(mpgTeams, ChampionshipOutType.LIGUE_1, "20190818");
    }

    @Test
    public void testCheckTeams20192020L1() throws Exception {
        List<String> mpgTeams = Arrays.asList("Angers", "Bordeaux", "Brest", "Dijon", "Lille", "Lyon", "Marseille", "Metz", "Monaco", "Montpellier",
                "Nantes", "Nice", "Nîmes", "Paris", "Reims", "Rennes", "Saint-Étienne", "Strasbourg", "Toulouse");
        testMappingTeams(mpgTeams, ChampionshipOutType.LIGUE_1, "20191223");
    }

    @Test
    public void testCheckTeams20192020L1WithLogoTeamName() throws Exception {
        List<String> mpgTeams = Arrays.asList("Angers", "Bordeaux", "Brest", "Dijon", "Lille", "Lyon", "Marseille", "Metz", "Monaco", "Montpellier",
                "Nantes", "Nice", "Nîmes", "Paris", "Reims", "Rennes", "Saint-Étienne", "Strasbourg", "Toulouse");
        // No injuries/suspended for some teams at this date
        List<String> mpgTeams20191220 = new ArrayList<String>(mpgTeams);
        mpgTeams20191220.remove("Bordeaux");
        mpgTeams20191220.remove("Brest");
        testMappingTeams(mpgTeams20191220, ChampionshipOutType.LIGUE_1, "20191220");
    }

    @Test
    public void testCheckTeams20182019PL() throws Exception {
        List<String> mpgTeams = Arrays.asList("Arsenal", "Aston Villa", "Bournemouth", "Brighton", "Burnley", "Chelsea", "Crystal Palace", "Everton",
                "Leicester", "Liverpool", "Man. City", "Man. United", "Newcastle", "Norwich", "Sheffield", "Southampton", "Tottenham", "Watford",
                "West Ham", "Wolverhampton");

        // No injuries/suspended for some teams at this date
        List<String> mpgTeams20190826 = new ArrayList<String>(mpgTeams);
        mpgTeams20190826.remove("Southampton");
        mpgTeams20190826.remove("Wolverhampton");
        testMappingTeams(mpgTeams20190826, ChampionshipOutType.PREMIER_LEAGUE, "20190826");
    }

    @Test
    public void testCheckTeams20192020PL() throws Exception {
        List<String> mpgTeams = Arrays.asList("Arsenal", "Aston Villa", "Bournemouth", "Brighton", "Burnley", "Chelsea", "Crystal Palace", "Everton",
                "Leicester", "Liverpool", "Man. City", "Man. United", "Newcastle", "Norwich", "Sheffield", "Southampton", "Tottenham", "Watford",
                "West Ham", "Wolverhampton");

        // No injuries/suspended for some teams at this date
        List<String> mpgTeams20191223 = new ArrayList<String>(mpgTeams);
        mpgTeams20191223.remove("Sheffield");
        testMappingTeams(mpgTeams20191223, ChampionshipOutType.PREMIER_LEAGUE, "20191223");
    }

    @Test
    public void testCheckTeams20192020PLWithLogoTeamName() throws Exception {
        List<String> mpgTeams = Arrays.asList("Arsenal", "Aston Villa", "Bournemouth", "Brighton", "Burnley", "Chelsea", "Crystal Palace", "Everton",
                "Leicester", "Liverpool", "Man. City", "Man. United", "Newcastle", "Norwich", "Sheffield", "Southampton", "Tottenham", "Watford",
                "West Ham", "Wolverhampton");

        // blank logo for some teams
        List<String> mpgTeams20191220 = new ArrayList<String>(mpgTeams);
        mpgTeams20191220.remove("Man. United");
        mpgTeams20191220.remove("Tottenham");
        testMappingTeams(mpgTeams20191220, ChampionshipOutType.PREMIER_LEAGUE, "20191220");
    }

    @Test
    public void testCheckTeams20182019SerieA() throws Exception {
        List<String> mpgTeams = Arrays.asList("Atalanta", "Bologna", "Brescia", "Cagliari", "Fiorentina", "Genoa", "Inter", "Juventus", "Lazio",
                "Lecce", "Milan", "Napoli", "Parma", "Roma", "Sampdoria", "Sassuolo", "Spal", "Torino", "Udinese", "Verona");

        // No injuries/suspended for some teams at this date
        List<String> mpgTeams20190805 = new ArrayList<String>(mpgTeams);
        mpgTeams20190805.remove("Genoa");
        mpgTeams20190805.remove("Napoli");
        mpgTeams20190805.remove("Sampdoria");
        mpgTeams20190805.remove("Verona");
        testMappingTeams(mpgTeams20190805, ChampionshipOutType.SERIE_A, "20190805");
    }

    @Test
    public void testCheckTeams20192020SerieA() throws Exception {
        List<String> mpgTeams = Arrays.asList("Atalanta", "Bologna", "Brescia", "Cagliari", "Fiorentina", "Genoa", "Inter", "Juventus", "Lazio",
                "Lecce", "Milan", "Napoli", "Parma", "Roma", "Sampdoria", "Sassuolo", "Spal", "Torino", "Udinese", "Verona");

        // blank logo for some teams
        List<String> mpgTeams20191223 = new ArrayList<String>(mpgTeams);
        mpgTeams20191223.remove("Parma");
        testMappingTeams(mpgTeams20191223, ChampionshipOutType.SERIE_A, "20191223");
    }

    @Test
    public void testCheckTeams20192020SerieAWithLogoTeamName() throws Exception {
        List<String> mpgTeams = Arrays.asList("Atalanta", "Bologna", "Brescia", "Cagliari", "Fiorentina", "Genoa", "Inter", "Juventus", "Lazio",
                "Lecce", "Milan", "Napoli", "Parma", "Roma", "Sampdoria", "Sassuolo", "Spal", "Torino", "Udinese", "Verona");

        // blank logo for some teams
        List<String> mpgTeams20191220 = new ArrayList<String>(mpgTeams);
        mpgTeams20191220.remove("Juventus");
        mpgTeams20191220.remove("Parma");
        testMappingTeams(mpgTeams20191220, ChampionshipOutType.SERIE_A, "20191220");
    }

    @Test
    public void testCheckTeams20192020LigA() throws Exception {
        List<String> mpgTeams = Arrays.asList("Alavés", "Atlético", "Barcelona", "Betis", "Bilbao", "Celta", "Eibar", "Espanyol", "Getafe", "Granada",
                "Leganés", "Levante", "Mallorca", "Osasuna", "Real Madrid", "Real Sociedad", "Sevilla", "Valencia", "Valladolid", "Villarreal");
        testMappingTeams(mpgTeams, ChampionshipOutType.LIGA, "20190827");
        testMappingTeams(mpgTeams, ChampionshipOutType.LIGA, "20191223");
    }

    @Test
    public void testCheckTeams20192020LigAWithLogoTeamName() throws Exception {
        List<String> mpgTeams = Arrays.asList("Alavés", "Atlético", "Barcelona", "Betis", "Bilbao", "Celta", "Eibar", "Espanyol", "Getafe", "Granada",
                "Leganés", "Levante", "Mallorca", "Osasuna", "Real Madrid", "Real Sociedad", "Sevilla", "Valencia", "Valladolid", "Villarreal");
        List<String> mpgTeams20191220 = new ArrayList<String>(mpgTeams);
        mpgTeams20191220.remove("Alavés");
        testMappingTeams(mpgTeams20191220, ChampionshipOutType.LIGA, "20191220");
    }

    @Test
    public void testLocalMapping() throws Exception {
        for (ChampionshipOutType c : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.PREMIER_LEAGUE, ChampionshipOutType.LIGA)) {
            List<Player> players = getClientFromFile(c, "20181017").getPlayers(c);
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
        InjuredSuspendedEquipeActuClient client = getClientFromFile(ChampionshipOutType.LIGUE_1, "20190131");

        Assert.assertNotNull("Fares Bahlouli is injured",
                client.getPlayer(ChampionshipOutType.LIGUE_1, "Fares Bahlouli", Position.UNDEFINED, "Lille"));
        Assert.assertNotNull("Neymar is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Neymar", Position.UNDEFINED, "Paris"));
        Assert.assertNotNull("Neymar is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Neymar", Position.A, "Paris"));
        Assert.assertNotNull("Pablo Chavarria is injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Pablo Chavarria", Position.A, "Reims"));
        Assert.assertNull("Pablo is not injured", client.getPlayer(ChampionshipOutType.LIGUE_1, "Pablo", Position.D, "Bordeaux"));
    }

    @Test
    public void testFeaturesLigue1() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.LIGUE_1;

        // Mock
        InjuredSuspendedEquipeActuClient client = getClientFromFile(c, "20181017");

        // Test
        Assert.assertNotNull(client.getPlayer(c, "Presnel Kimpembe", Position.UNDEFINED, "Paris"));
        Assert.assertNotNull(client.getPlayer(c, "preSnel kimpeMbe", Position.UNDEFINED, "Paris"));
        Assert.assertNotNull(client.getPlayer(c, "Kimpembe Presnel", Position.UNDEFINED, "Paris"));

        Player p = client.getPlayer(c, "Jesé", Position.UNDEFINED, "Paris");
        Assert.assertNotNull(p);
        Assert.assertEquals(OutType.INJURY_ORANGE, p.getOutType());
        Assert.assertEquals("Blessure à la hanche (depuis 29/09)", p.getDescription());
        Assert.assertEquals("Inconnu", p.getLength());

        Assert.assertNull(client.getPlayer(c, "Presnel Kimpembe", Position.UNDEFINED, "Paris", OutType.SUSPENDED));
    }

    @Test
    public void testFeaturesPremierLeague() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.PREMIER_LEAGUE;

        // Mock
        InjuredSuspendedEquipeActuClient client = getClientFromFile(c, "20181017");

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
    public void testFeaturesPremierLeagueSecond() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.PREMIER_LEAGUE;

        // Mock
        InjuredSuspendedEquipeActuClient client = getClientFromFile(c, "20190911");

        Assert.assertNotNull(client.getPlayer(c, "Willy Boly", Position.D, "Wolverhampton"));
        Assert.assertNull(client.getPlayer(c, "Willy Boly", Position.D, "Arsenal"));
        Assert.assertNull(client.getPlayer(c, "Willy Boly", Position.M, "Wolverhampton"));
    }

    @Test
    public void testFeaturesLiga() throws Exception {
        ChampionshipOutType c = ChampionshipOutType.LIGA;

        // Mock
        InjuredSuspendedEquipeActuClient client = getClientFromFile(c, "20181017");

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
        stubFor(get("/blessures-et-suspensions/fodbold/angleterre/premier-league")
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

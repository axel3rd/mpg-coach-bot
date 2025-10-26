package org.blondin.mpg.root;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.AvailablePlayers;
import org.blondin.mpg.root.model.ChampionshipType;
import org.blondin.mpg.root.model.Club;
import org.blondin.mpg.root.model.Clubs;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.Division;
import org.blondin.mpg.root.model.League;
import org.blondin.mpg.root.model.LeagueStatus;
import org.blondin.mpg.root.model.Mode;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.root.model.PoolPlayers;
import org.blondin.mpg.root.model.Position;
import org.blondin.mpg.root.model.Team;
import org.junit.Assert;
import org.junit.Test;

public class MpgClientTest extends AbstractMockTestClient {

    @Test
    public void testMockSignInKo() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withStatus(403).withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.bad.json")));
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        try {
            MpgClient.build(config, url);
            Assert.fail("Invalid password is invalid");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Bad credentials", "Forbidden URL: " + url, e.getMessage());
        }
    }

    @Test
    public void testMockSignInOk() {
        stubFor(post("/user/sign-in").withRequestBody(equalToJson("{\"login\":\"firstName.lastName@gmail.com\",\"password\":\"foobar\",\"language\":\"fr-FR\"}"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Assert.assertTrue(true);
    }

    @Test
    public void testMockTeamPL() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/team/mpg_team_MLMHBPCB_10_1_3").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLMHBPCB.20250112.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Team team = mpgClient.getTeam("mpg_team_MLMHBPCB_10_1_3");
        Assert.assertNotNull(team);
        Assert.assertEquals("Axel Football Club", team.getName());
        Assert.assertEquals(0, team.getBudget());
        Assert.assertNotNull(team.getSquad());
        Assert.assertEquals(19, team.getSquad().size());
        Player p = team.getSquad().get("mpg_championship_player_223094");
        Assert.assertNotNull(p);
        Assert.assertEquals(104, p.getPricePaid());
        Assert.assertNotNull(team.getBids());
        Assert.assertEquals(0, team.getBids().size());
        Assert.assertNotNull(team.getBonuses());
        Assert.assertEquals(11, team.getBonusesNumber());
    }

    @Test
    public void testMockDashboardGame() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/dashboard/leagues").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.MLEFEX6G-status-4.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Dashboard dashboard = mpgClient.getDashboard();
        Assert.assertNotNull(dashboard);
        Assert.assertNotNull(dashboard.getLeagues());
        League l = dashboard.getLeagues().get(0);
        Assert.assertEquals("MLEFEX6G", l.getId());
        Assert.assertEquals("mpg_division_MLEFEX6G_3_1", l.getDivisionId());
        Assert.assertEquals("Ligue 2 Fous", l.getName());
        Assert.assertEquals(ChampionshipType.LIGUE_2, l.getChampionship());
        Assert.assertEquals(Mode.EXPERT, l.getMode());
        Assert.assertEquals(LeagueStatus.GAMES, l.getStatus());
        Assert.assertEquals(10, l.getDivisionTotalUsers());
    }

    @Test
    public void testMockDashboardMercatoWaitNextTurn() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/dashboard/leagues").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.dashboard.MLAX7HMK-status-3-waitMercatoNextTurn.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Dashboard dashboard = mpgClient.getDashboard();
        Assert.assertNotNull(dashboard);
        Assert.assertNotNull(dashboard.getLeagues());
        League l = dashboard.getLeagues().get(0);
        Assert.assertEquals("MLAX7HMK", l.getId());
        Assert.assertEquals("mpg_division_MLAX7HMK_3_1", l.getDivisionId());
        Assert.assertEquals(ChampionshipType.LIGUE_1, l.getChampionship());
        Assert.assertEquals(Mode.NORMAL, l.getMode());
        Assert.assertEquals(LeagueStatus.MERCATO, l.getStatus());
        Assert.assertEquals(10, l.getDivisionTotalUsers());

        // The current mercato state: wait next turn
        Assert.assertEquals(2, l.getCurrentTeamStatus());
    }

    @Test
    public void testMockDivision() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.MLEFEX6G.20210804.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Division division = mpgClient.getDivision("mpg_division_MLEFEX6G_3_1");
        Assert.assertNotNull(division);
        Assert.assertNotNull(division.getUsersTeams());
        Assert.assertTrue(division.getUsersTeams().size() > 0);
        Assert.assertEquals("mpg_team_MLEFEX6G_3_1_2", division.getTeam(mpgClient.getUserId()));
        Assert.assertEquals(2, division.getGameCurrent());
        Assert.assertEquals(18, division.getGameTotal());
        Assert.assertEquals(17, division.getGameRemaining());
    }

    @Test
    public void testMockTeam() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/team/mpg_team_MLEFEX6G_3_1_2").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.team.MLEFEX6G.20210804.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Team team = mpgClient.getTeam("mpg_team_MLEFEX6G_3_1_2");
        Assert.assertNotNull(team);
        Assert.assertEquals("Axel Football Club", team.getName());
        Assert.assertEquals(42, team.getBudget());
        Assert.assertNotNull(team.getBonuses());
        Assert.assertEquals(10, team.getBonusesNumber());
        Assert.assertNotNull(team.getSquad());
        Assert.assertEquals(20, team.getSquad().size());
        Player p = team.getSquad().get("mpg_championship_player_220359");
        Assert.assertNotNull(p);
        Assert.assertEquals(32, p.getPricePaid());
        Assert.assertNotNull(team.getBids());
        Assert.assertEquals(1, team.getBids().size());
    }

    @Test
    public void testMockCoachEmpty() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/coach").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLEFEX6G.20210804.empty.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Coach coach = mpgClient.getCoach("mpg_division_MLEFEX6G_3_1");
        Assert.assertNotNull(coach);
        Assert.assertTrue(coach.getComposition() > 0);
        Assert.assertEquals("mpg_match_team_formation_MLEFEX6G_3_1_2_2_2", coach.getIdMatch());
    }

    @Test
    public void testMockCoachNotEmpty() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/coach").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLAX7HMK.20210812.withCaptain.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Coach coach = mpgClient.getCoach("mpg_division_MLEFEX6G_3_1");
        Assert.assertNotNull(coach);
        Assert.assertTrue(coach.getComposition() > 0);
        Assert.assertEquals("mpg_match_team_formation_MLAX7HMK_3_1_1_5_6", coach.getIdMatch());
        Assert.assertEquals("mpg_championship_player_220237", coach.getCaptain());
    }

    @Test
    public void testMockCoachBonus() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/coach").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.coach.MLEFEX6G.20211019.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Coach coach = mpgClient.getCoach("mpg_division_MLEFEX6G_3_1");
        Assert.assertNotNull(coach);
        Assert.assertTrue(coach.getComposition() > 0);
        Assert.assertEquals("mpg_match_team_formation_MLEFEX6G_3_1_12_5_2", coach.getIdMatch());
        Assert.assertEquals("mpg_championship_player_177449", coach.getCaptain());
        Assert.assertNotNull(coach.getBonusSelected());
        Assert.assertEquals("boostOnePlayer", coach.getBonusSelected().getName());
        Assert.assertEquals("mpg_championship_player_437503", coach.getBonusSelected().getPlayerId());
    }

    @Test
    public void testMockClubs() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/championship-clubs").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.clubs.2021.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        Clubs clubs = mpgClient.getClubs();
        Assert.assertNotNull(clubs);
        Assert.assertNotNull(clubs.getChampionshipClubs());
        Assert.assertTrue(clubs.getChampionshipClubs().size() > 40);
        Club club = clubs.getChampionshipClubs().get("mpg_championship_club_693");
        Assert.assertNotNull(club);
        Assert.assertEquals("Sochaux", club.getName());
    }

    @Test
    public void testMockPoolPlayers() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/championship-players-pool/4").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.poolPlayers.4.2021.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        PoolPlayers pool = mpgClient.getPoolPlayers(ChampionshipType.LIGUE_2);
        Assert.assertNotNull(pool);
        Assert.assertNotNull(pool.getPlayers());
        Assert.assertTrue(pool.getPlayers().size() > 40);

        Player p = mpgClient.getPoolPlayers(ChampionshipType.LIGUE_2).getPlayer("mpg_championship_player_220359");
        Assert.assertNotNull(p);
        Assert.assertEquals("Prevot Maxence", p.getName());
        Assert.assertEquals("Prevot", p.getLastName());
        Assert.assertEquals("Maxence", p.getFirstName());
        Assert.assertEquals(Position.G, p.getPosition());
        Assert.assertEquals(18, p.getQuotation());
        Assert.assertEquals("mpg_championship_player_220359", p.getId());
    }

    @Test
    public void testMockAvailablePlayers() {
        stubFor(post("/user/sign-in").willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.user-signIn.fake.json")));
        stubFor(get("/division/mpg_division_MLEFEX6G_3_1/available-players")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("mpg.division.available.players.MLEFEX6G.20210804.json")));
        MpgClient mpgClient = MpgClient.build(getConfig(), "http://localhost:" + server.port());
        AvailablePlayers tb = mpgClient.getAvailablePlayers("mpg_division_MLEFEX6G_3_1");
        Assert.assertTrue(tb.getList().size() > 10);
    }
}

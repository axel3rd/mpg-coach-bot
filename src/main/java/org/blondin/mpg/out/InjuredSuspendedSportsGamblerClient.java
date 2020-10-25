package org.blondin.mpg.out;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * https://www.sportsgambler.com/football/injuries-suspensions/
 */
public class InjuredSuspendedSportsGamblerClient extends AbstractInjuredSuspendedNotL2 {

    private static final Map<String, String> TEAM_NAME_WRAPPER = new HashMap<>();

    static {
        /*
         * Team name "SportsGambler -> MPG" wrapper (by championship)
         */

        // Ligue 1
        TEAM_NAME_WRAPPER.put("Paris Saint Germain", "Paris");
        TEAM_NAME_WRAPPER.put("Saint Etienne", "Saint-Étienne");
        TEAM_NAME_WRAPPER.put("Nimes", "Nîmes");

        // Premiere League
        TEAM_NAME_WRAPPER.put("Manchester City", "Man. City");
        TEAM_NAME_WRAPPER.put("Manchester United", "Man. United");
        TEAM_NAME_WRAPPER.put("Newcastle United", "Newcastle");
        TEAM_NAME_WRAPPER.put("Sheffield United", "Sheffield");
        TEAM_NAME_WRAPPER.put("West Bromwich Albion", "West Bromwich");
        TEAM_NAME_WRAPPER.put("Wolverhampton Wanderers", "Wolverhampton");

        // Serie A
        TEAM_NAME_WRAPPER.put("AC Milan", "Milan");
        TEAM_NAME_WRAPPER.put("SSC Napoli", "Napoli");

        // Ligua
        TEAM_NAME_WRAPPER.put("Celta Vigo", "Celta");
        TEAM_NAME_WRAPPER.put("Athletic Bilbao", "Bilbao");
        TEAM_NAME_WRAPPER.put("Atlético Madrid", "Atlético");
        TEAM_NAME_WRAPPER.put("Cadiz", "Cadix");

    }

    private InjuredSuspendedSportsGamblerClient(Config config) {
        super(config);
    }

    public static InjuredSuspendedSportsGamblerClient build(Config config) {
        return build(config, null);
    }

    public static InjuredSuspendedSportsGamblerClient build(Config config, String urlOverride) {
        InjuredSuspendedSportsGamblerClient client = new InjuredSuspendedSportsGamblerClient(config);
        client.setUrl(StringUtils.defaultString(urlOverride, "https://www.sportsgambler.com/football/injuries-suspensions"));
        return client;
    }

    @Override
    Map<String, String> getMpgTeamNameWrapper() {
        return TEAM_NAME_WRAPPER;
    }

    @Override
    public String getUrlSuffix(ChampionshipOutType championship) {
        switch (championship) {
        case LIGUE_1:
            return "france-ligue-1/";
        case PREMIER_LEAGUE:
            return "england-premier-league/";
        case LIGA:
            return "spain-la-liga/";
        case SERIE_A:
            return "italy-serie-a/";
        default:
            throw new UnsupportedOperationException(String.format("Championship type not supported: %s", championship));
        }
    }

    @Override
    public List<Player> getPlayers(String content) {
        List<Player> players = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        boolean oneTeamHasBeenParsed = false;
        for (Element item : doc.select("h3.injuries-title")) {
            String team = item.selectFirst("a").ownText();
            if (StringUtils.isNoneBlank(team)) {
                oneTeamHasBeenParsed = true;
            }

            for (Element line : item.nextElementSibling().select("tbody").first().select("tr")) {
                if (line.select("td").size() == 1) {
                    // No players injured or suspended for this this
                    if (!line.selectFirst("td").text().contains("No players are currently injured or suspended")) {
                        throw new UnsupportedOperationException(
                                String.format("Only one line, but not with correct message for no injured/suspended players: %s",
                                        line.selectFirst("td").ownText()));
                    }
                    break;
                }
                Player player = new Player();
                player.setTeam(getMpgTeamName(team));
                player.setOutType(parseOutType(line.select("td").get(0)));
                player.setFullNameWithPosition(line.select("td").get(1).text());
                player.setDescription(line.select("td").get(2).text());
                player.setLength(line.select("td").get(3).text());
                players.add(player);
            }
        }
        if (!oneTeamHasBeenParsed) {
            throw new UnsupportedOperationException("No teams have been found, parsing problem");
        }
        return players;
    }

    private static OutType parseOutType(Element e) {
        if (e.selectFirst("img") != null) {
            String image = e.selectFirst("img").attr("src");
            if (StringUtils.isNotBlank(image)) {
                int begin = image.lastIndexOf('/');
                int end = image.indexOf(".png");
                String type = image.substring(begin + 1, end);
                return OutType.getNameByValue(type);
            }
        }
        if ("?".equals(e.text())) {
            return OutType.INJURY_ORANGE;
        }
        throw new UnsupportedOperationException(String.format("Element 'out' not supported: %s", e.toString()));
    }

}

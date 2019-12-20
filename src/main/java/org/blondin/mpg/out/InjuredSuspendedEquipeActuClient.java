package org.blondin.mpg.out;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.blondin.mpg.out.model.Position;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * https://www.equipeactu.fr/blessures-et-suspensions/fodbold/
 */
public class InjuredSuspendedEquipeActuClient extends AbstractClient {

    private static final EnumMap<ChampionshipOutType, Map<String, String>> TEAM_NAME_WRAPPER;
    private static final Map<String, String> LOGO_NAME_WRAPPER = new HashMap<>();

    private EnumMap<ChampionshipOutType, List<Player>> cache = new EnumMap<>(ChampionshipOutType.class);

    static {
        // Team name "EquipeActu -> MPG" wrapper (by championship)

        TEAM_NAME_WRAPPER = new EnumMap<>(ChampionshipOutType.class);
        for (ChampionshipOutType cot : Arrays.asList(ChampionshipOutType.LIGUE_1, ChampionshipOutType.LIGUE_2, ChampionshipOutType.PREMIER_LEAGUE,
                ChampionshipOutType.SERIE_A, ChampionshipOutType.LIGA)) {
            TEAM_NAME_WRAPPER.put(cot, new HashMap<String, String>());
        }

        TEAM_NAME_WRAPPER.get(ChampionshipOutType.LIGUE_1).put("Paris", "PSG");

        TEAM_NAME_WRAPPER.get(ChampionshipOutType.PREMIER_LEAGUE).put("Man. City", "Manchester City");
        TEAM_NAME_WRAPPER.get(ChampionshipOutType.PREMIER_LEAGUE).put("Man. United", "Manchester United");

        TEAM_NAME_WRAPPER.get(ChampionshipOutType.SERIE_A).put("Bologna", "Bologne");
        TEAM_NAME_WRAPPER.get(ChampionshipOutType.SERIE_A).put("Napoli", "Naples");
        TEAM_NAME_WRAPPER.get(ChampionshipOutType.SERIE_A).put("Roma", "Rome");
        TEAM_NAME_WRAPPER.get(ChampionshipOutType.SERIE_A).put("Spal", "SPAL 2013");

        TEAM_NAME_WRAPPER.get(ChampionshipOutType.LIGA).put("Barcelona", "Barcelone");
        TEAM_NAME_WRAPPER.get(ChampionshipOutType.LIGA).put("Granada", "Grenade");
        TEAM_NAME_WRAPPER.get(ChampionshipOutType.LIGA).put("Mallorca", "Majorque");
        TEAM_NAME_WRAPPER.get(ChampionshipOutType.LIGA).put("Sevilla", "Séville");
        TEAM_NAME_WRAPPER.get(ChampionshipOutType.LIGA).put("Valencia", "Valence");

        // Logo name wrapper

        LOGO_NAME_WRAPPER.put("Nimes", "Nîmes");
        LOGO_NAME_WRAPPER.put("Psg", "PSG");
        LOGO_NAME_WRAPPER.put("Saint Etienne", "Saint-Étienne");

        LOGO_NAME_WRAPPER.put("Bologna", "Bologne");
        LOGO_NAME_WRAPPER.put("Napoli", "Naples");
        LOGO_NAME_WRAPPER.put("Roma", "Rome");
        LOGO_NAME_WRAPPER.put("Spal", "SPAL 2013");

        LOGO_NAME_WRAPPER.put("Atletico Madrid", "Atlético Madrid");
        LOGO_NAME_WRAPPER.put("Athletic Club", "Bilbao");
        LOGO_NAME_WRAPPER.put("Barcelona", "Barcelone");
        LOGO_NAME_WRAPPER.put("Granada", "Grenade");
        LOGO_NAME_WRAPPER.put("Leganes", "Leganés");
        LOGO_NAME_WRAPPER.put("Mallorca", "Majorque");
        LOGO_NAME_WRAPPER.put("Sevilla", "Séville");
        LOGO_NAME_WRAPPER.put("Valencia", "Valence");
    }

    public static InjuredSuspendedEquipeActuClient build(Config config) {
        return build(config, null);
    }

    public static InjuredSuspendedEquipeActuClient build(Config config, String urlOverride) {
        InjuredSuspendedEquipeActuClient client = new InjuredSuspendedEquipeActuClient();
        client.setUrl(StringUtils.defaultString(urlOverride, "https://www.equipeactu.fr/blessures-et-suspensions/fodbold/"));
        client.setProxy(config.getProxy());
        return client;
    }

    /**
     * Return team name (equipeActu) from MPG team name
     * 
     * @param championship The championship
     * @param mpgTeamName  The MPG team name
     * @return The EquipeActu team name
     */
    static String getTeamName(ChampionshipOutType championship, String mpgTeamName) {
        if (TEAM_NAME_WRAPPER.get(championship).containsKey(mpgTeamName)) {
            return TEAM_NAME_WRAPPER.get(championship).get(mpgTeamName);
        }
        return mpgTeamName;
    }

    /**
     * Return injured or suspended player
     * 
     * @param championship Championship of player
     * @param playerName   Player Name
     * @param position     Position
     * @param teamName     Team Name
     * @return Player or null if not found
     */
    public Player getPlayer(ChampionshipOutType championship, String playerName, Position position, String teamName) {
        OutType[] excludes = null;
        return getPlayer(championship, playerName, position, teamName, excludes);
    }

    /**
     * Return injured or suspended player
     * 
     * @param championship Championship of player
     * @param playerName   Player Name
     * @param position     Position
     * @param teamName     Team Name
     * @param excludes     {@link OutType} to exclude
     * @return Player or null if not found
     */
    public Player getPlayer(ChampionshipOutType championship, String playerName, Position position, String teamName, OutType... excludes) {
        List<OutType> excluded = Arrays.asList(ObjectUtils.defaultIfNull(excludes, new OutType[] {}));

        for (Player player : getPlayers(championship)) {
            if (!excluded.contains(player.getOutType())
                    && Stream.of(playerName.toLowerCase().split(" ")).allMatch(player.getFullNameWithPosition().toLowerCase()::contains)) {
                Position pos = player.getPosition();
                if (Position.UNDEFINED.equals(pos) || Position.UNDEFINED.equals(position) || position.equals(pos)) {
                    if (StringUtils.isNotBlank(teamName) && !player.getTeam().contains(getTeamName(championship, teamName))) {
                        continue;
                    }
                    return player;
                }
            }
        }
        return null;
    }

    public String getHtmlContent(ChampionshipOutType championship) {
        return get(championship.getValue(), String.class, TIME_HOUR_IN_MILLI_SECOND);
    }

    public List<Player> getPlayers(ChampionshipOutType championship) {
        if (!cache.containsKey(championship)) {
            cache.put(championship, getPlayers(getHtmlContent(championship)));
        }
        return cache.get(championship);
    }

    private List<Player> getPlayers(String content) {
        List<Player> players = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        String team = null;
        for (Element item : doc.select("div.injuries_item")) {
            // Retrieve team from name or logo
            team = parseTeam(item, team);

            if (item.selectFirst("div.injuries_name") == null) {
                // No injured/suspended players in team
                continue;
            }

            Player player = new Player();
            player.setTeam(team);
            player.setOutType(parseOutType(item.selectFirst("div.injuries_type").selectFirst("span").className()));
            player.setFullNameWithPosition(item.selectFirst("div.injuries_playername").text());
            player.setDescription(item.selectFirst("div.injuries_name").text());
            player.setLength(item.selectFirst("div.injuries_length").text());
            players.add(player);
        }
        return players;
    }

    private static String parseTeam(Element item, String currentTeam) {
        String team = currentTeam;
        Element teamItem = item.parent().previousElementSibling();
        if (teamItem.selectFirst("img") != null) {
            String teamName = teamItem.text().trim();
            if (StringUtils.isNotBlank(teamName)) {
                team = teamName;
            } else {
                // No team name, get from png logo name
                String logoUrl = teamItem.selectFirst("img").attr("src");
                if (!logoUrl.contains("blank_team")) {
                    // Some logo can be "blank_team" ... previous team used in this case, no other way
                    team = logoUrl.substring(logoUrl.lastIndexOf('/') + 1, logoUrl.lastIndexOf(".png"));
                    team = WordUtils.capitalizeFully(team.replaceAll("-", " "));
                    if (LOGO_NAME_WRAPPER.containsKey(team)) {
                        team = LOGO_NAME_WRAPPER.get(team);
                    }
                }
            }
        }

        if (StringUtils.isBlank(team)) {
            // Special case of Spain, where 'Alavés' (first team), can be blank :(
            if (item.selectFirst("div.injuries_playerimg").selectFirst("img").attr("src").contains("spain")) {
                team = "Alavés";
            } else {
                throw new UnsupportedOperationException("Team can't be blank here, parsing problem");
            }
        }

        return team;
    }

    private static OutType parseOutType(String htmlContent) {
        final String prefix = "sitesprite icon_";
        if (!StringUtils.startsWith(htmlContent, prefix)) {
            throw new UnsupportedOperationException(String.format("HTML content should start with prefix '%s': %s ", prefix, htmlContent));
        }
        return OutType.getNameByValue(htmlContent.substring(prefix.length()));
    }

}

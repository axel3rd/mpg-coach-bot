package org.blondin.mpg.equipeactu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.equipeactu.model.OutType;
import org.blondin.mpg.equipeactu.model.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * http://www.equipeactu.fr/blessures-et-suspensions/fodbold/
 */
public class InjuredSuspendedClient extends AbstractClient {

    private EnumMap<ChampionshipOutType, List<Player>> cache = new EnumMap<>(ChampionshipOutType.class);

    public static InjuredSuspendedClient build() {
        return new InjuredSuspendedClient();
    }

    @Override
    protected String getUrl() {
        return "http://www.equipeactu.fr/blessures-et-suspensions/fodbold/";
    }

    /**
     * Return injured or suspended player
     * 
     * @param championship Championship of player
     * @param name Name
     * @return Player or null if not found
     */
    public Player getPlayer(ChampionshipOutType championship, String name) {
        OutType[] excludes = null;
        return getPlayer(championship, name, excludes);
    }

    /**
     * Return injured or suspended player
     * 
     * @param championship Championship of player
     * @param name Name
     * @param excludes {@link OutType} to exclude
     * @return Player or null if not found
     */
    public Player getPlayer(ChampionshipOutType championship, String name, OutType... excludes) {
        List<OutType> excluded = Arrays.asList(ObjectUtils.defaultIfNull(excludes, new OutType[] {}));

        for (Player player : getPlayers(championship)) {
            if (!excluded.contains(player.getOutType())
                    && Stream.of(name.toLowerCase().split(" ")).allMatch(player.getFullNameWithPosition().toLowerCase()::contains)) {
                return player;
            }
        }
        return null;
    }

    public String getHtmlContent(ChampionshipOutType championship) {
        return get(championship.getValue(), String.class);
    }

    public List<Player> getPlayers(ChampionshipOutType championship) {
        if (!cache.containsKey(championship)) {
            cache.put(championship, getPlayers(getHtmlContent(championship)));
        }
        return cache.get(championship);
    }

    protected List<Player> getPlayers(String content) {
        List<Player> players = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        for (Element item : doc.select("div.injuries_item")) {
            if (item.selectFirst("div.injuries_name") == null) {
                // No injured/suspended players in team
                continue;
            }
            Player player = new Player();
            player.setOutType(parseOutType(item.selectFirst("div.injuries_type").selectFirst("span").className()));
            player.setFullNameWithPosition(item.selectFirst("div.injuries_playername").text());
            player.setDescription(item.selectFirst("div.injuries_name").text());
            player.setLength(item.selectFirst("div.injuries_length").text());
            players.add(player);
        }
        return players;
    }

    private static OutType parseOutType(String htmlContent) {
        final String prefix = "sitesprite icon_";
        if (!StringUtils.startsWith(htmlContent, prefix)) {
            throw new UnsupportedOperationException(String.format("HTML content should start with prefix '%s': %s ", prefix, htmlContent));
        }
        return OutType.getNameByValue(htmlContent.substring(prefix.length()));
    }
}

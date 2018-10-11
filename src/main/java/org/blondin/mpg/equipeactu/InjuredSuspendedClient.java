package org.blondin.mpg.equipeactu;

import java.util.ArrayList;
import java.util.Arrays;
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

    private List<Player> cache;

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
     * @param name Name
     * @return Player or null if not found
     */
    public Player getPlayer(String name) {
        OutType[] excludes = null;
        return getPlayer(name, excludes);
    }

    /**
     * Return injured or suspended player
     * 
     * @param name Name
     * @param excludes {@link OutType} to exclude
     * @return Player or null if not found
     */
    public Player getPlayer(String name, OutType... excludes) {
        List<OutType> excluded = Arrays.asList(ObjectUtils.defaultIfNull(excludes, new OutType[] {}));

        for (Player player : getPlayers()) {
            if (!excluded.contains(player.getOutType())
                    && Stream.of(name.toLowerCase().split(" ")).allMatch(player.getFullNameWithPosition().toLowerCase()::contains)) {
                return player;
            }
        }
        return null;
    }

    public String getHtmlContent() {
        return get("france/ligue-1", String.class);
    }

    public List<Player> getPlayers() {
        if (cache == null) {
            cache = getPlayers(getHtmlContent());
        }
        return cache;
    }

    protected List<Player> getPlayers(String content) {
        List<Player> players = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        for (Element item : doc.select("div.injuries_item")) {
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

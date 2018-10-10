package org.blondin.mpg.equipeactu;

import java.util.ArrayList;
import java.util.List;

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

    public static InjuredSuspendedClient build() {
        return new InjuredSuspendedClient();
    }

    @Override
    protected String getUrl() {
        return "http://www.equipeactu.fr/blessures-et-suspensions/fodbold/";
    }

    public List<Player> getPlayers() {
        return null;
    }

    protected List<Player> getPlayers(String content) {
        List<Player> players = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        for (Element item : doc.select("div.injuries_item")) {
            Player player = new Player();
            player.setOutType(parseOutType(item.selectFirst("div.injuries_type").selectFirst("span").className()));
            player.setName(item.selectFirst("div.injuries_playername").text());
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

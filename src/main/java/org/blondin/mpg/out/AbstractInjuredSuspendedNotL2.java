package org.blondin.mpg.out;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.out.model.Player;
import org.blondin.mpg.out.model.Position;

public abstract class AbstractInjuredSuspendedNotL2 extends AbstractClient {

    private EnumMap<ChampionshipOutType, List<Player>> cache = new EnumMap<>(ChampionshipOutType.class);

    protected AbstractInjuredSuspendedNotL2(Config config) {
        super(config);
    }

    /**
     * Return injured or suspended player
     * 
     * @param championship Championship of player
     * @param playerName   Player Name
     * @param teamName     Team Name
     * @return Player or null if not found
     */
    public final Player getPlayer(ChampionshipOutType championship, String playerName, String teamName) {
        OutType[] excludes = null;
        return getPlayer(championship, playerName, Position.UNDEFINED, teamName, excludes);
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
    public Player getPlayer(ChampionshipOutType championship, String playerName, String teamName, OutType... excludes) {
        return getPlayer(championship, playerName, Position.UNDEFINED, teamName, excludes);
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
            if (!excluded.contains(player.getOutType()) && Stream.of(StringUtils.stripAccents(playerName.toLowerCase()).split(" "))
                    .allMatch(player.getFullNameWithPosition().toLowerCase()::contains)) {
                Position pos = player.getPosition();
                if (Position.UNDEFINED.equals(pos) || Position.UNDEFINED.equals(position) || position.equals(pos)) {
                    if (StringUtils.isNotBlank(teamName) && StringUtils.isNotBlank(player.getTeam()) && !player.getTeam().equals(teamName)) {
                        continue;
                    }
                    return player;
                }
            }
        }
        return null;
    }

    final List<Player> getPlayers(ChampionshipOutType championship) {
        cache.computeIfAbsent(championship, k -> getPlayers(getHtmlContent(championship)));
        return cache.get(championship);
    }

    /**
     * HTML content for given championship. <b>WARN</b>Public and not final for unit test facilities (mock usage, ...)
     * 
     * @param championship Championship
     * @return HTML content
     */
    public String getHtmlContent(ChampionshipOutType championship) {
        return get(getUrlSuffix(championship), String.class, TIME_HOUR_IN_MILLI_SECOND);
    }

    /**
     * Return MPG team name from sportsgambler team name (because has change during time)
     * 
     * @param sportsgambler The sportsgambler team name
     * @return The MPG team name
     */
    protected final String getMpgTeamName(String sportsgamblerTeamName) {
        if (getMpgTeamNameWrapper().containsKey(sportsgamblerTeamName)) {
            return getMpgTeamNameWrapper().get(sportsgamblerTeamName);
        }
        return sportsgamblerTeamName;
    }

    /**
     * Return MPG team name for given website team name
     * 
     * @return MPG team name
     */
    abstract Map<String, String> getMpgTeamNameWrapper();

    /**
     * URL suffix to add at end of path
     * 
     * @param championship Championship
     * @return The URL suffix
     */
    abstract String getUrlSuffix(ChampionshipOutType championship);

    /**
     * Players injured/suspended parsing, depending the website
     * 
     * @param content HTML website
     * @return List of Injured/Suspended players
     */
    abstract List<Player> getPlayers(String content);
}

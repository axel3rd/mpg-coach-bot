package org.blondin.mpg;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.blondin.mpg.out.ChampionshipOutType;
import org.blondin.mpg.out.InjuredSuspendedWrapperClient;
import org.blondin.mpg.out.model.OutType;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.root.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public abstract class AbstractMercatoMpgProcess extends AbstractMpgProcess {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMercatoMpgProcess.class);

    protected static void processMercato(List<Player> players, InjuredSuspendedWrapperClient outPlayersClient, ChampionshipOutType championship) {
        Collections.sort(players, Comparator.comparing(Player::getPosition).thenComparing(Player::getEfficiency).thenComparing(Player::getQuotation).reversed());
        List<Player> goals = players.stream().filter(p -> p.getPosition().equals(Position.G)).toList().subList(0, 5);
        List<Player> defenders = players.stream().filter(p -> p.getPosition().equals(Position.D)).toList().subList(0, 10);
        List<Player> midfielders = players.stream().filter(p -> p.getPosition().equals(Position.M)).toList().subList(0, 10);
        List<Player> attackers = players.stream().filter(p -> p.getPosition().equals(Position.A)).toList().subList(0, 10);

        AsciiTable at = getTable(TABLE_POSITION, TABLE_PLAYER_NAME, TABLE_EFFICIENCY, TABLE_QUOTE, "Auct.", "Out info");
        for (List<Player> line : Arrays.asList(goals, defenders, midfielders, attackers)) {
            for (Player player : line) {
                org.blondin.mpg.out.model.Player outPlayer = outPlayersClient.getPlayer(championship, player.getName(), PositionWrapper.toOut(player.getPosition()), player.getClubName(),
                        OutType.INJURY_GREEN);
                String outInfos = "";
                if (outPlayer != null) {
                    outInfos = String.format("%s - %s - %s", outPlayer.getOutType(), outPlayer.getDescription(), outPlayer.getLength());
                }
                AT_Row row = at.addRow(player.getPosition(), player.getName(), FORMAT_DECIMAL_DOUBLE.format(player.getEfficiency()), player.getQuotation(), player.getAuction(), outInfos);
                setTableFormatRowPaddingSpace(row);
                row.getCells().get(2).getContext().setTextAlignment(TextAlignment.RIGHT);
                row.getCells().get(3).getContext().setTextAlignment(TextAlignment.RIGHT);
                row.getCells().get(4).getContext().setTextAlignment(TextAlignment.RIGHT);
            }
            at.addRule();
        }

        String render = at.render();
        LOG.info(render);
        LOG.info("");
    }

}

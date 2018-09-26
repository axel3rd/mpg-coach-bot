package org.blondin.mpg;

import org.blondin.mpg.stats.MpgStatsClient;
import org.blondin.mpg.stats.model.Championship;

public class Main {

    public static void main(String[] args) {
        Championship l1 = MpgStatsClient.build().getStats();
        System.out.println(l1.getPlayers().size());
    }
}

package org.blondin.mpg;

import org.blondin.mpg.out.ChampionshipOutType;
import org.blondin.mpg.root.model.ChampionshipType;
import org.blondin.mpg.stats.ChampionshipStatsType;
import org.junit.Assert;
import org.junit.Test;

public class ChampionshipTypeWrapperTest {

    @Test
    public void testConst() {
        new ChampionshipTypeWrapper();
        Assert.assertTrue(true);
    }

    @Test
    public void testToStats() {
        Assert.assertEquals(ChampionshipStatsType.LIGUE_1, ChampionshipTypeWrapper.toStats(ChampionshipType.LIGUE_1));
        Assert.assertEquals(ChampionshipStatsType.PREMIER_LEAGUE, ChampionshipTypeWrapper.toStats(ChampionshipType.PREMIER_LEAGUE));
        Assert.assertEquals(ChampionshipStatsType.LIGA, ChampionshipTypeWrapper.toStats(ChampionshipType.LIGA));
    }

    @Test
    public void testToOut() {
        Assert.assertEquals(ChampionshipOutType.LIGUE_1, ChampionshipTypeWrapper.toOut(ChampionshipType.LIGUE_1));
        Assert.assertEquals(ChampionshipOutType.PREMIER_LEAGUE, ChampionshipTypeWrapper.toOut(ChampionshipType.PREMIER_LEAGUE));
        Assert.assertEquals(ChampionshipOutType.LIGA, ChampionshipTypeWrapper.toOut(ChampionshipType.LIGA));
    }
}

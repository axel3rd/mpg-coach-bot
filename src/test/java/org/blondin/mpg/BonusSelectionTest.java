package org.blondin.mpg;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.blondin.mpg.root.model.SelectedBonus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BonusSelectionTest {

    @Before
    public void setUp() {
        // Force locale as in AbstractMockTestClient, because Main is used in this test class
        Locale.setDefault(Locale.ENGLISH);
    }

    private SelectedBonus getBonusSelected(String name) {
        return getBonusSelected(name, null);
    }

    private SelectedBonus getBonusSelected(String name, String playerId) {
        SelectedBonus bs = mock(SelectedBonus.class);
        doReturn(name).when(bs).getName();
        doReturn(playerId).when(bs).getPlayerId();
        return bs;
    }

    private Map<String, Integer> getBonus(int b1, int b2, int b3, int b4, int b5, int b6, int b7) {
        Map<String, Integer> bonuses = new HashMap<>();
        bonuses.put("removeGoal", b1);
        bonuses.put("boostAllPlayers", b2);
        bonuses.put("nerfGoalkeeper", b3);
        bonuses.put("boostOnePlayer", b4);
        bonuses.put("mirror", b5);
        bonuses.put("blockTacticalSubs", b6);
        bonuses.put("removeRandomPlayer", b7);
        return bonuses;
    }

    @Test
    public void testBonusSelection() {
        Assert.assertEquals("nerfGoalkeeper", Main.selectBonus(null, getBonus(0, 0, 1, 1, 0, 0, 0), 0, true, "fake").getName());
        Assert.assertEquals("boostOnePlayer", Main.selectBonus(null, getBonus(0, 0, 0, 1, 0, 0, 0), 0, true, "fake").getName());
        Assert.assertEquals("fake", Main.selectBonus(null, getBonus(0, 0, 0, 1, 0, 0, 0), 0, true, "fake").getPlayerId());
        Assert.assertNull(Main.selectBonus(null, getBonus(0, 1, 0, 1, 1, 1, 1), 0, true, "fake").getPlayerId());
        Assert.assertNull(Main.selectBonus(null, getBonus(1, 1, 1, 3, 1, 1, 1), 15, true, "fake"));
        Assert.assertEquals("boostAllPlayers", Main.selectBonus(null, getBonus(0, 1, 0, 1, 1, 1, 1), 0, true, "fake").getName());
        Assert.assertEquals("removeGoal", Main.selectBonus(null, getBonus(1, 1, 1, 3, 1, 1, 1), 0, true, "fake").getName());
        Assert.assertEquals("removeRandomPlayer", Main.selectBonus(null, getBonus(1, 1, 1, 3, 1, 1, 1), 9, true, "fake").getName());
    }

    @Test
    public void testBonusAlreadySelected() {
        Assert.assertEquals("nerfGoalkeeper",
                Main.selectBonus(getBonusSelected("nerfGoalkeeper"), getBonus(1, 1, 1, 3, 1, 1, 1), 1, true, "fake").getName());
    }

    @Test
    public void testBonusSelectionNotUsed() {
        // No previous bonus
        Assert.assertNull(Main.selectBonus(null, null, 0, false, "fake"));

        // Previous bonus wallet
        SelectedBonus bs = getBonusSelected("removeGoal");
        Assert.assertNotNull(Main.selectBonus(bs, null, 0, false, "fake"));
        Assert.assertEquals("removeGoal", Main.selectBonus(bs, null, 0, false, "fake").getName());
        Assert.assertNull(Main.selectBonus(bs, null, 0, false, "fake").getPlayerId());

        // Previous bonus redbull
        bs = getBonusSelected("boostOnePlayer", "foobar");
        Assert.assertNotNull(Main.selectBonus(bs, null, 0, false, "fake"));
        Assert.assertEquals("boostOnePlayer", Main.selectBonus(bs, null, 0, false, "fake").getName());
        Assert.assertEquals("foobar", Main.selectBonus(bs, null, 0, false, "fake").getPlayerId());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBonusSelectionBadInputBonus() {
        Main.selectBonus(null, null, -1, true, "fake");
    }

}

package org.blondin.mpg;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.blondin.mpg.root.model.Bonus;
import org.blondin.mpg.root.model.BonusSelected;
import org.junit.Assert;
import org.junit.Test;

public class BonusSelectionTest {

    private BonusSelected getBonusSelected(Integer type) {
        return getBonusSelected(type, null);
    }

    private BonusSelected getBonusSelected(Integer type, String playerId) {
        BonusSelected bs = mock(BonusSelected.class);
        doReturn(type).when(bs).getType();
        doReturn(playerId).when(bs).getPlayerId();
        return bs;
    }

    private Bonus getBonus(int b1, int b2, int b3, int b4, int b5, int b6, int b7) {
        Bonus b = new Bonus();
        try {
            FieldUtils.writeDeclaredField(b, "bonus1", b1, true);
            FieldUtils.writeDeclaredField(b, "bonus2", b2, true);
            FieldUtils.writeDeclaredField(b, "bonus3", b3, true);
            FieldUtils.writeDeclaredField(b, "bonus4", b4, true);
            FieldUtils.writeDeclaredField(b, "bonus5", b5, true);
            FieldUtils.writeDeclaredField(b, "bonus6", b6, true);
            FieldUtils.writeDeclaredField(b, "bonus7", b7, true);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
        return b;
    }

    @Test
    public void testBonusSelection() {
        Assert.assertEquals(4, Main.selectBonus(null, getBonus(0, 0, 0, 1, 0, 0, 0), "mpg_match_XX_1_18_1", 10, true, "fake").getType().intValue());
        Assert.assertEquals("fake", Main.selectBonus(null, getBonus(0, 0, 0, 1, 0, 0, 0), "mpg_match_XX_1_18_1", 10, true, "fake").getPlayerId());
        Assert.assertNull(Main.selectBonus(null, getBonus(0, 1, 0, 1, 1, 1, 1), "mpg_match_XX_1_18_1", 10, true, "fake").getPlayerId());
        Assert.assertNull(Main.selectBonus(null, getBonus(1, 1, 1, 3, 1, 1, 1), "mpg_match_XX_1_3_1", 10, true, "fake").getType());
        Assert.assertEquals(2, Main.selectBonus(null, getBonus(0, 1, 0, 1, 1, 1, 1), "mpg_match_XX_1_18_1", 10, true, "fake").getType().intValue());
        Assert.assertEquals(1, Main.selectBonus(null, getBonus(1, 1, 1, 3, 1, 1, 1), "mpg_match_XX_1_18_1", 10, true, "fake").getType().intValue());
        Assert.assertEquals(7, Main.selectBonus(null, getBonus(1, 1, 1, 3, 1, 1, 1), "mpg_match_XX_1_10_1", 10, true, "fake").getType().intValue());
    }

    @Test
    public void testBonusAlreadySelected() {
        Assert.assertEquals(1,
                Main.selectBonus(getBonusSelected(1), getBonus(1, 1, 1, 3, 1, 1, 1), "mpg_match_XX_1_10_1", 10, true, "fake").getType().intValue());
    }

    @Test
    public void testBonusSelectionNotUsed() {
        // No previous bonus
        Assert.assertNotNull(Main.selectBonus(null, null, "mpg_match_XX_1_18_1", 10, false, "fake"));
        Assert.assertNull(Main.selectBonus(null, null, "mpg_match_XX_1_18_1", 10, false, "fake").getType());
        Assert.assertNull(Main.selectBonus(null, null, "mpg_match_XX_1_18_1", 10, false, "fake").getPlayerId());

        // Previous bonus wallet
        BonusSelected bs = getBonusSelected(1);
        Assert.assertNotNull(Main.selectBonus(bs, null, "mpg_match_XX_1_18_1", 10, false, "fake"));
        Assert.assertEquals(1, Main.selectBonus(bs, null, "mpg_match_XX_1_18_1", 10, false, "fake").getType().intValue());
        Assert.assertNull(Main.selectBonus(bs, null, "mpg_match_XX_1_18_1", 10, false, "fake").getPlayerId());

        // Previous bonus redbull
        bs = getBonusSelected(4, "foobar");
        Assert.assertNotNull(Main.selectBonus(bs, null, "mpg_match_XX_1_18_1", 10, false, "fake"));
        Assert.assertEquals(4, Main.selectBonus(bs, null, "mpg_match_XX_1_18_1", 10, false, "fake").getType().intValue());
        Assert.assertEquals("foobar", Main.selectBonus(bs, null, "mpg_match_XX_1_18_1", 10, false, "fake").getPlayerId());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBonusSelectionBadInputBonus() {
        Main.selectBonus(null, null, "mpg_match_XX_1_42_1", 10, true, "fake");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBonusSelectionBadInputPlayers() {
        Main.selectBonus(null, getBonus(1, 1, 1, 3, 1, 1, 1), "mpg_match_XX_1_42_1", -200, true, "fake");
    }
}

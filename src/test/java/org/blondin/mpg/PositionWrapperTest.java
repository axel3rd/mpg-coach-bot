package org.blondin.mpg;

import org.blondin.mpg.root.model.Position;
import org.junit.Assert;
import org.junit.Test;

public class PositionWrapperTest {

    @Test
    public void testConst() {
        new PositionWrapper();
    }

    @Test
    public void testToOut() {
        Assert.assertEquals(org.blondin.mpg.equipeactu.model.Position.A, PositionWrapper.toOut(Position.A));
        Assert.assertEquals(org.blondin.mpg.equipeactu.model.Position.M, PositionWrapper.toOut(Position.M));
        Assert.assertEquals(org.blondin.mpg.equipeactu.model.Position.D, PositionWrapper.toOut(Position.D));
        Assert.assertEquals(org.blondin.mpg.equipeactu.model.Position.G, PositionWrapper.toOut(Position.G));
    }
}

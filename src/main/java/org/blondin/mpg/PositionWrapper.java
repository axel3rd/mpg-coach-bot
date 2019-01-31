package org.blondin.mpg;

import org.blondin.mpg.root.model.Position;

public class PositionWrapper {

    PositionWrapper() {
        super();
    }

    public static org.blondin.mpg.equipeactu.model.Position toOut(Position position) {
        switch (position) {
        case A:
            return org.blondin.mpg.equipeactu.model.Position.A;
        case M:
            return org.blondin.mpg.equipeactu.model.Position.M;
        case D:
            return org.blondin.mpg.equipeactu.model.Position.D;
        case G:
            return org.blondin.mpg.equipeactu.model.Position.G;
        default:
            throw new UnsupportedOperationException(String.format("Position not supported: %s", position));
        }
    }
}

package org.blondin.mpg;

import org.blondin.mpg.root.model.Position;

public class PositionWrapper {

    PositionWrapper() {
        super();
    }

    public static org.blondin.mpg.out.model.Position toOut(Position position) {
        switch (position) {
        case A:
            return org.blondin.mpg.out.model.Position.A;
        case M:
            return org.blondin.mpg.out.model.Position.M;
        case D:
            return org.blondin.mpg.out.model.Position.D;
        case G:
            return org.blondin.mpg.out.model.Position.G;
        default:
            throw new UnsupportedOperationException(String.format("Position not supported: %s", position));
        }
    }

    public static Position fromStats(org.blondin.mpg.stats.model.Position position) {
        switch (position) {
        case A:
            return Position.A;
        case M:
            return Position.M;
        case D:
            return Position.D;
        case G:
            return Position.G;
        default:
            throw new UnsupportedOperationException(String.format("Position not supported: %s", position));
        }
    }
}

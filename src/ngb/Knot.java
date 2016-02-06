package ngb;

import util.PointF;

/**
 * Created by Florian on 06.02.2016.
 */
public class Knot {


    private final PointF pos;
    private final Obstacle o;


    public Knot(PointF pos, Obstacle o) {
        this.pos = pos;
        this.o = o;
    }

    public PointF getPos() {
        return pos;
    }

    public Obstacle getO() {
        return o;
    }

}

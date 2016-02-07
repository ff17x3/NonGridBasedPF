package ngb;

import util.PointF;

import java.util.ArrayList;

/**
 * Created by Florian on 06.02.2016.
 */
public class Knot {

    private float heuristic;
    private ArrayList<Knot> neighbors = new ArrayList<>();
    public final PointF pos;
    public final Obstacle o;


    public Knot(PointF pos, Obstacle o) {
        this.pos = pos;
        this.o = o;
    }

    public boolean isOn(float x, float y) {
        return x == pos.x && y == pos.y;
    }

    public void addNeighbor(Knot k) {
        neighbors.add(k);
    }

    public void addNeighborBoth(Knot k) {
        neighbors.add(k);
        k.addNeighbor(this);
    }

    public ArrayList<Knot> getNeighbors() {
        return neighbors;
    }

    public void setHeuristic(float heuristic) {
        this.heuristic = heuristic;
    }

    public float getHeuristic() {
        return heuristic;
    }
}

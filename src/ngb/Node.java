package ngb;

import util.PointF;

import java.util.ArrayList;

/**
 * Created by Florian on 06.02.2016.
 */
public class Node {

    private float heuristic, g, f;
    private ArrayList<Node> neighbors = new ArrayList<>();
    private Node parent;
    public final PointF pos;
    public final Obstacle o;


    public Node(PointF pos, Obstacle o) {
        this.pos = pos;
        this.o = o;
    }

    public boolean isOn(float x, float y) {
        return x == pos.x && y == pos.y;
    }

    public void addNeighbor(Node k) {
        neighbors.add(k);
    }

    public void addNeighborBoth(Node k) {
        neighbors.add(k);
        k.addNeighbor(this);
    }

    public ArrayList<Node> getNeighbors() {
        return neighbors;
    }

    public void setHeuristic(float heuristic) {
        this.heuristic = heuristic;
    }

    public float getHeuristic() {
        return heuristic;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public float getG() {
        return g;
    }

    public float getF() {
        return f;
    }

    public Node getParent() {
        return parent;
    }
}

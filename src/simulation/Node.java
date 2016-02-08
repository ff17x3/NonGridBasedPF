package simulation;

import util.PointF;

import java.util.ArrayList;

/**
 * Created by Florian on 06.02.2016.
 */
public class Node {

    private float heuristic;
    private float g;

    private float f;
    private ArrayList<Node> neighbors = new ArrayList<>();
    private Node parent;
    private int matrixIndex;
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

    public int getMatrixIndex() {
        // TODO return index in [Adjazenzmatrix] :P Vorsicht bei startN und endN!!!!
        return matrixIndex;
    }

    public void setMatrixIndex(int index) {
        matrixIndex = index;
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

    public void setG(float g) {
        this.g = g;
    }

    public void setF(float f) {
        this.f = f;
    }

}

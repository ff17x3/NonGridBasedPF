package raw;

import util.PointF;

import java.util.ArrayList;

/**
 * Created by Florian nee Max!!! on 06.02.2016.
 */
public class Node {
    // constants for expanding with a radius in the correct direction
    public static final byte UP_LEFT = 0, UP_RIGHT = 1, DOWN_LEFT = 2, DOWN_RIGHT = 3;

    // a* variables
    private float heuristic;
    private float g;
    private float f;
    private Node parent;

    // values describing the node in the map/in the arrays
    private final ArrayList<Node> neighbors = new ArrayList<>();
    private final int matrixIndex;
    public final byte expandDirection;
    public final PointF pos;

    public Node(PointF pos, byte expandDirection, int matrixIndex) {
        this.pos = pos;
        this.expandDirection = expandDirection;
        this.matrixIndex = matrixIndex;
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

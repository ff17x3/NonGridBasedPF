package raw;


import simulation.Obstacle;//TODO
import util.PointF;

import java.util.ArrayList;

/**
 * Created by Max!!! on 08.02.2016.
 */
public class Algorithm {

    public static Node getWay(MatrixPosBundle map) {

        // generate Node structure from Bundle
        float[][] matrix = map.
    }


    public static final float MIN_PATH_WIDTH = 0.15f;


    public static MatrixPosBundle genMap(Obstacle[] obstacles) {
        return null;
    }

    private void genNodes(Obstacle[] obstacles) {
        ArrayList<PointF> coordsList = new ArrayList<>(obstacles.length * 4);
        Node k1 = null, k2 = null, k3 = null, k4 = null;
        boolean btl = true, btr = true, bbr = true, bbl = true;
        for (Obstacle o : obstacles) {
            btl = true;
            btr = true;
            bbr = true;
            bbl = true;
            PointF tl = new PointF(o.x, o.y),
                    tr = new PointF(o.x + o.width, o.y),
                    br = new PointF(o.x + o.width, o.y + o.height),
                    bl = new PointF(o.x, o.y + o.height);
            for (int j = 0; j < obstacles.length; j++) {
                Obstacle oTest = obstacles[j];
                //dünne Wände
                if (o == oTest)
                    continue;

                if (btl)
                    if (isNearHorz(tl, oTest.y + oTest.height, oTest.x, oTest.width)) {
                        btl = false;
                        oTest.setB(false);
                    } else if (isNearVert(tl, oTest.x + oTest.width, oTest.y, oTest.height)) {
                        btl = false;
                        oTest.setR(false);
                    }
                if (btr)
                    if (isNearHorz(tr, oTest.y + oTest.height, oTest.x, oTest.width)) {
                        btr = false;
                        oTest.setB(false);
                    } else if (isNearVert(tr, oTest.x, oTest.y, oTest.height)) {
                        btr = false;
                        oTest.setL(false);
                    }
                if (bbr)
                    if (isNearHorz(br, oTest.y, oTest.x, oTest.width)) {
                        bbr = false;
                        oTest.setT(false);
                    } else if (isNearVert(br, oTest.x, oTest.y, oTest.height)) {
                        bbr = false;
                        oTest.setL(false);
                    }
                if (bbl)
                    if (isNearHorz(bl, oTest.y, oTest.x, oTest.width)) {
                        bbl = false;
                        oTest.setT(false);
                    } else if (isNearVert(bl, oTest.x + oTest.width, oTest.y, oTest.height)) {
                        bbl = false;
                        oTest.setR(false);
                    }

            }
            o.setTl(btl);
            o.setTr(btr);
            o.setBr(bbr);
            o.setBl(bbl);
        }
        for (Obstacle o : obstacles) {
            btl = o.isTl();
            btr = o.isTr();
            bbr = o.isBr();
            bbl = o.isBl();


            PointF tl = new PointF(o.x, o.y),
                    tr = new PointF(o.x + o.width, o.y),
                    br = new PointF(o.x + o.width, o.y + o.height),
                    bl = new PointF(o.x, o.y + o.height);

            if (btl) {
                k1 = new Node(tl, o);
                coordsList.add(k1);
            }
            if (btr) {
                k2 = new Node(tr, o);
                coordsList.add(k2);
            }
            if (bbr) {
                k3 = new Node(br, o);
                coordsList.add(k3);
            }
            if (bbl) {
                k4 = new Node(bl, o);
                coordsList.add(k4);
            }

            if (btl) {
                if (btr && o.isT())
                    k1.addNeighborBoth(k2);
                if (bbl && o.isL())
                    k1.addNeighborBoth(k4);
            }
            if (bbr) {
                if (btr && o.isR())
                    k3.addNeighborBoth(k2);
                if (bbl && o.isB())
                    k3.addNeighborBoth(k4);
            }
        }
        System.out.println("nodeList.size() = " + coordsList.size());
        nodes = coordsList.toArray(new Node[coordsList.size()]);
    }

    private boolean isNearHorz(PointF p, float yLine, float xStart, float width) {
        return (between(p.x, xStart - MIN_PATH_WIDTH, xStart + width + MIN_PATH_WIDTH)
                && between(p.y, yLine - MIN_PATH_WIDTH, yLine + MIN_PATH_WIDTH));
    }

    private boolean isNearVert(PointF p, float xLine, float yStart, float height) {
        return (between(p.x, xLine - MIN_PATH_WIDTH, xLine + MIN_PATH_WIDTH)
                && between(p.y, yStart - MIN_PATH_WIDTH, yStart + height + MIN_PATH_WIDTH));
    }

    private void finishMatrix() {
        java.util.List<Node> rest = new ArrayList<>(nodes.length);
        for (int i = nodes.length - 1; i >= 0; i--)
            rest.add(nodes[i]);

        //connect nodes
        int i = 0;
        for (Node kStart : nodes) {
            connectToAllInView(kStart, rest);
            rest.remove(nodes.length - 1 - i);
            i++;
        }

        //create Adjazenzmatrix
        movementCosts = new float[nodes.length + 2][nodes.length + 2];
        for (int j = 0; j < movementCosts.length - 2; j++) {
            nodes[j].setMatrixIndex(j);
        }
        float mc;
        for (Node n : nodes)
            for (Node nb : n.getNeighbors())
                if (movementCosts[n.getMatrixIndex()][nb.getMatrixIndex()] == 0) {
                    mc = (float) Math.sqrt(Math.pow(n.pos.x - nb.pos.x, 2) + Math.pow(n.pos.y - nb.pos.y, 2));
                    movementCosts[n.getMatrixIndex()][nb.getMatrixIndex()] = mc;
                    movementCosts[nb.getMatrixIndex()][n.getMatrixIndex()] = mc;
                }
    }

    private void connectToAllInView(Node kStart, java.util.List<Node> rest) {
        Obstacle oStart = kStart.o;
        for (Node kEnd : rest) {
            testInView(kEnd, oStart, kStart);
        }
    }

    private void connectToAllInView(Node kStart, Node[] rest) {
        for (Node kEnd : rest) {
            testInView(kEnd, null, kStart);
        }
    }

    private void testInView(Node kEnd, Obstacle oStart, Node kStart) {
        float dx, dy;
        boolean hits = false;
        Obstacle oEnd = kEnd.o;
        if (oStart == oEnd && oStart != null)
            return;
        for (Obstacle oCol : obstacles) {

            if (kEnd.pos.x == 1 && kEnd.pos.y == 6 && kStart.pos.x == 1 && kStart.pos.y == 1 && oCol.width == 3)
                System.out.println("bgb");

            dx = kEnd.pos.x - kStart.pos.x;
            dy = kEnd.pos.y - kStart.pos.y;
//                    if (rayHitsObstacle(getAngle(dx, dy), oCol, kStart.pos, kEnd.pos)) {
            float mRay = dy / dx;
            float endX = kEnd.pos.x - kStart.pos.x;
            float endY = kEnd.pos.y - kStart.pos.y;
            if (!(kStart.isOn(oCol.x, oCol.y) || kStart.isOn(oCol.x + oCol.width, oCol.y)
                    || kEnd.isOn(oCol.x, oCol.y) || kEnd.isOn(oCol.x + oCol.width, oCol.y)
                    || ((oCol == oStart || oCol == oEnd) && (kEnd.pos.x == kStart.pos.x)))) {
                hits = intsHozLine(mRay, oCol.y - kStart.pos.y, oCol.x - kStart.pos.x, oCol.width, endX, endY);
                if (hits)
                    break;
            }
            if (!(kStart.isOn(oCol.x, oCol.y + oCol.height) || kStart.isOn(oCol.x + oCol.width, oCol.y + oCol.height)
                    || kEnd.isOn(oCol.x, oCol.y + oCol.height) || kEnd.isOn(oCol.x + oCol.width, oCol.y + oCol.height)
                    || ((oCol == oStart || oCol == oEnd) && (kEnd.pos.x == kStart.pos.x)))) {
                hits = intsHozLine(mRay, oCol.y + oCol.height - kStart.pos.y, oCol.x - kStart.pos.x, oCol.width, endX, endY);
                if (hits)
                    break;
            }
            if (!(kStart.isOn(oCol.x, oCol.y) || kStart.isOn(oCol.x, oCol.y + oCol.height)
                    || kEnd.isOn(oCol.x, oCol.y) || kEnd.isOn(oCol.x, oCol.y + oCol.height)
                    || ((oCol == oStart || oCol == oEnd) && (kEnd.pos.y == kStart.pos.y)))) {
                hits = intsVerLine(mRay, oCol.x - kStart.pos.x, oCol.y - kStart.pos.y, oCol.height, endX, endY);
                if (hits)
                    break;
            }
            if (!(kStart.isOn(oCol.x + oCol.width, oCol.y) || kStart.isOn(oCol.x + oCol.width, oCol.y + oCol.height)
                    || kEnd.isOn(oCol.x + oCol.width, oCol.y) || kEnd.isOn(oCol.x + oCol.width, oCol.y + oCol.height)
                    || ((oCol == oStart || oCol == oEnd) && (kEnd.pos.y == kStart.pos.y)))) {
                hits = intsVerLine(mRay, oCol.x + oCol.width - kStart.pos.x, oCol.y - kStart.pos.y, oCol.height, endX, endY);
                if (hits)
                    break;
            }

        }
        if (!hits) {
            kStart.addNeighborBoth(kEnd);
        }
    }


    private boolean intsHozLine(float m, float yB, float xB, float widthB, float posEndXRel, float posEndYRel) {
        /**horizontal col. detection:
         * A: line: y=m*x
         * B: y = a
         * => x = a/m, wenn x auf der Seite liegt (und Schnittpunkt zwischen den beiden Punkten(Start und End)), dann Kollision
         */
        float sx = intersectPointHoz(m, yB);
        return between(sx, xB, xB + widthB) && between(sx, 0, posEndXRel) && between(yB, 0, posEndYRel);
    }

    private boolean intsVerLine(float m, float xB, float yB, float heightB, float posEndXRel, float posEndYRel) {
        /**vertical col. detection:
         * A: line: y=m*x
         * B: x = a
         * => y = m*a, wenn y auf der Seite liegt, dann Kollision
         */
        float sy = intersectPointVer(m, xB);
        return between(sy, yB, yB + heightB) && between(sy, 0, posEndYRel) && between(xB, 0, posEndXRel);
    }

    private boolean between(float a, float x1, float x2) {
        if (x1 < x2)
            return a >= x1 && a <= x2;
        else
            return a >= x2 && a <= x1;
    }

    private float intersectPointVer(float gradientA, float xB) {
        return xB * gradientA;
    }

    private float intersectPointHoz(float gradientA, float yB) {
        return yB / gradientA;
    }

    private float getGradientfromAngle(float angle) {
        return (float) Math.tan(angle);
    }

}

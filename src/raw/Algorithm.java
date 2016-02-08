package raw;


import simulation.Obstacle;
import util.PointF;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import static raw.Node.*;

/**
 * Created by Max!!! on 08.02.2016.
 */
public class Algorithm {

    public static Node getWay(MatrixPosBundle map) {

        // generate Node structure from Bundle
        float[][] matrix = map.matrix;
        PointF[] positions = map.nodesPos;
        Byte[] expandDirs = map.expandDir;

        int startI = positions.length - 2, endI = positions.length - 1;

        // create Node Objects
        Node[] nodes = new Node[positions.length - 2];
        Node startN = new Node(positions[startI], (byte) -1, startI);
        Node endN = new Node(positions[endI], (byte) -1, endI);
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node(positions[i], expandDirs[i], i);
        }
        // connect Node Objects
        for (int i = 0; i < matrix.length; i++) {
            for (int j = i + 1; j < matrix[i].length; j++) {
                if (matrix[i][j] != 0) {
                    if (i < nodes.length && j < nodes.length) {
                        nodes[i].addNeighbor(nodes[j]);
                        nodes[j].addNeighbor(nodes[i]);
                    } else { // entweder startN oder endN
                        if (i == startI) { // es ist startN
                            if (j < nodes.length) { // zu irgendeinem Node
                                startN.addNeighbor(nodes[j]);
                                nodes[j].addNeighbor(startN);
                            } else { // zu endI
                                startN.addNeighbor(endN);
                                endN.addNeighbor(startN);
                            }
                        } else if (i == endI) {
                            if (j < nodes.length) {// zu irgendeinem Node
                                endN.addNeighbor(nodes[j]);
                                nodes[j].addNeighbor(endN);
                            } else { // zu startI
                                endN.addNeighbor(startN);
                                startN.addNeighbor(endN);
                            }
                        }
                    }
                }
            }
        }
        // leg los
        // heuristic, linear
        for (Node k : nodes) {
            float dX = k.pos.x - startN.pos.x, dY = k.pos.y - startN.pos.y;
            k.setHeuristic((float) Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)));
        }
        // a*-Algorithm
        TreeMap<Float, Node> openList = new TreeMap<>();
        HashSet<Node> closedList = new HashSet<>();
        Node currNode;
        openList.put(0f, endN);
        while (!openList.isEmpty()) {
            currNode = openList.remove(openList.firstKey());
            if (currNode == startN) {
                // way found :)
                return startN;
            }
            closedList.add(currNode);

            for (Node neighbor : currNode.getNeighbors()) {
                if (closedList.contains(neighbor))
                    continue;
                // neuen G-Wert von neighbor von currNode bestimmen
                float newG = matrix[currNode.getMatrixIndex()][neighbor.getMatrixIndex()] + currNode.getG();
                // wenn das hier ein besserer Weg ist (oder der erste zu neighbor, wenn man da noch nicht war), dann bei neighbor diesen neuen Weg über currNode speichern
                if (openList.containsValue(neighbor)) {
                    if (neighbor.getG() < newG)
                        // zu neighbor gibt es schon einen besseren Weg, also nichts tun
                        continue;
                    // neighbor ist schon auf der openList, also mit altem key (=f) rauslöschen!
                    openList.remove(neighbor.getF());
                }
                // neuen F-Wert für neighbor und für diesen Weg bestimmen
                float newF = newG + neighbor.getHeuristic();
                openList.put(newF, neighbor);
                neighbor.setF(newF);
                neighbor.setG(newG);
                neighbor.setParent(currNode); // noch restliche Zuweisungen
            }
        }
        // no possible way :(
        return null;
    }


    public static final float MIN_PATH_WIDTH = 0.15f;


    public static MatrixPosBundle genMap(Obstacle[] obstacles) {
        return null;
    }

    private void genNodes(Obstacle[] obstacles) {
        ArrayList<PointF> coordsList = new ArrayList<>(obstacles.length * 4);
        ArrayList<Byte> expDirList = new ArrayList<>(obstacles.length * 4);
        ArrayList<Obstacle> obs = new ArrayList<>(obstacles.length * 4);
        float[][] miniMatrix = new float[obstacles.length * 4][obstacles.length * 4];

        // create Node strucur in Arrays
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
        int iTl = -1, iTr = -1, iBl = -1, iBr = -1;
        float cache = 0;
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
                iTl = coordsList.size();
                coordsList.add(tl);
                expDirList.add(UP_LEFT);
                obs.add(o);
            }
            if (btr) {
                iTr = coordsList.size();
                coordsList.add(tr);
                expDirList.add(UP_RIGHT);
                obs.add(o);
            }
            if (bbr) {
                iBr = coordsList.size();
                coordsList.add(br);
                expDirList.add(DOWN_RIGHT);
                obs.add(o);
            }
            if (bbl) {
                iBl = coordsList.size();
                coordsList.add(bl);
                expDirList.add(DOWN_LEFT);
                obs.add(o);
            }


            if (btl) {
                if (btr && o.isT()) {
                    cache = calcMoveCost(coordsList.get(iTr), coordsList.get(iTl));
                    miniMatrix[iTr][iTl] = cache;
                    miniMatrix[iTl][iTr] = cache;
                }
                if (bbl && o.isL()) {
                    cache = calcMoveCost(coordsList.get(iBl), coordsList.get(iTl));
                    miniMatrix[iBl][iTl] = cache;
                    miniMatrix[iTl][iBl] = cache;
                }
            }
            if (bbr) {
                if (btr && o.isR()) {
                    cache = calcMoveCost(coordsList.get(iBr), coordsList.get(iTr));
                    miniMatrix[iBr][iTr] = cache;
                    miniMatrix[iTr][iBr] = cache;
                }
                if (bbl && o.isB()) {
                    cache = calcMoveCost(coordsList.get(iBr), coordsList.get(iBl));
                    miniMatrix[iBr][iBl] = cache;
                    miniMatrix[iBl][iBr] = cache;
                }
            }

        }

        System.out.println("nodeList.size() = " + coordsList.size());
        PointF[] points = coordsList.toArray(new PointF[coordsList.size() + 2]);
        Byte[] dirs = expDirList.toArray(new Byte[expDirList.size()]);
        float[][] matrix = new float[points.length][points.length];

        for (int j = 0; j < miniMatrix.length; j++) {
            for (int k = 0; k < miniMatrix[0].length; k++) {
                matrix[j][k] = miniMatrix[j][k];
            }
        }


        //finish matrix (add connetion to matrix)---------------------------------------------


        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points.length; j++) {
                testInView(points, obs, j, i, obstacles, matrix);
            }
        }

    }

    private boolean isNearHorz(PointF p, float yLine, float xStart, float width) {
        return (between(p.x, xStart - MIN_PATH_WIDTH, xStart + width + MIN_PATH_WIDTH)
                && between(p.y, yLine - MIN_PATH_WIDTH, yLine + MIN_PATH_WIDTH));
    }

    private boolean isNearVert(PointF p, float xLine, float yStart, float height) {
        return (between(p.x, xLine - MIN_PATH_WIDTH, xLine + MIN_PATH_WIDTH)
                && between(p.y, yStart - MIN_PATH_WIDTH, yStart + height + MIN_PATH_WIDTH));
    }

    private float calcMoveCost(PointF a, PointF b) {
        return (float) Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

//    private void connectToAllInView(Node kStart, Node[] rest) {
//        for (Node kEnd : rest) {
//            testInView(kEnd, null, kStart);
//        }
//    }

    private void testInView(PointF[] points, ArrayList<Obstacle> obs, int indexEnd, int indexStart, Obstacle[] obstacles, float[][] matrix) {
        float dx, dy;
        boolean hits = false;
        PointF pStart = points[indexStart];
        PointF pEnd = points[indexEnd];
        Obstacle oEnd = obs.get(indexEnd);
        Obstacle oStart = obs.get(indexStart);
        if (indexStart == indexEnd && oStart != null)
            return;
        for (Obstacle oCol : obstacles) {

            if (pEnd.x == 1 && pEnd.y == 6 && pEnd.x == 1 && pEnd.y == 1 && oCol.width == 3)
                System.out.println("bgb");

            dx = pEnd.x - pEnd.x;
            dy = pEnd.y - pEnd.y;
//                    if (rayHitsObstacle(getAngle(dx, dy), oCol, pEnd, pEnd)) {
            float mRay = dy / dx;
            float endX = pEnd.x - pEnd.x;
            float endY = pEnd.y - pEnd.y;
            if (!(pStart.equals(oCol) || isOn(pStart, oCol.x + oCol.width, oCol.y)
                    || pEnd.equals(oCol) || isOn(pEnd, oCol.x + oCol.width, oCol.y)
                    || ((oCol == oStart || oCol == oEnd) && (pEnd.x == pEnd.x)))) {
                hits = intsHozLine(mRay, oCol.y - pEnd.y, oCol.x - pEnd.x, oCol.width, endX, endY);
                if (hits)
                    break;
            }
            if (!(isOn(pStart, oCol.x, oCol.y + oCol.height) || isOn(pStart, oCol.x + oCol.width, oCol.y + oCol.height)
                    || isOn(pEnd, oCol.x, oCol.y + oCol.height) || isOn(pEnd, oCol.x + oCol.width, oCol.y + oCol.height)
                    || ((oCol == oStart || oCol == oEnd) && (pEnd.x == pEnd.x)))) {
                hits = intsHozLine(mRay, oCol.y + oCol.height - pEnd.y, oCol.x - pEnd.x, oCol.width, endX, endY);
                if (hits)
                    break;
            }
            if (!(pStart.equals(oCol) || isOn(pStart, oCol.x, oCol.y + oCol.height)
                    || pEnd.equals(oCol) || isOn(pEnd, oCol.x, oCol.y + oCol.height)
                    || ((oCol == oStart || oCol == oEnd) && (pEnd.y == pEnd.y)))) {
                hits = intsVerLine(mRay, oCol.x - pEnd.x, oCol.y - pEnd.y, oCol.height, endX, endY);
                if (hits)
                    break;
            }
            if (!(isOn(pStart, oCol.x + oCol.width, oCol.y) || isOn(pStart, oCol.x + oCol.width, oCol.y + oCol.height)
                    || isOn(pEnd, oCol.x + oCol.width, oCol.y) || isOn(pEnd, oCol.x + oCol.width, oCol.y + oCol.height)
                    || ((oCol == oStart || oCol == oEnd) && (pEnd.y == pEnd.y)))) {
                hits = intsVerLine(mRay, oCol.x + oCol.width - pEnd.x, oCol.y - pEnd.y, oCol.height, endX, endY);
                if (hits)
                    break;
            }

        }
        if (!hits) {
            float cache = calcMoveCost(pStart, pEnd);
            matrix[indexEnd][indexStart] = cache;
            matrix[indexStart][indexEnd] = cache;
        }
    }

    private boolean isOn(PointF p, float x, float y) {
        return p.x == x && p.y == y;
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

package ngb;


import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import static java.lang.Math.round;


/**
 * Created by Florian on 06.02.2016. and max ARRRRRRGH!!!
 */
public class Manager implements DrawInferface, FrameInitInterface, Tickable {

    public static final float MIN_PATH_WIDTH = 0.15f;

    private Map map;
    private DrawFrame frame;
    private util.ClockNano clock;
    private PointF startP = null, endP = null;

    private Node[] nodes;
    private float[][] movementCosts; // TODO indices: startN = length - 2 und endN = length - 1
    private Node startN, endN, wayAnchor = null;

    private int startEndCircSize = 10;

    public static void main(String args[]) {
        try {
            new Manager("map3.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //..
    public Manager(String mapFile) throws Exception {
        map = Map.readMap(mapFile);
        frame = new DrawFrame(new Dimension(700, 700), this, this, new DimensionF(map.mapWidth, map.mapHeight));
        frame.addScaleChangeListener(scale -> {
            for (Obstacle o : map.obstacles) {
                o.updateScale(scale);
            }
        });
//        clock = new ClockNano(60, this);
//        clock.startTicking();
    }

    @Override
    public void draw(Graphics g, float scale) {
        // draw obstacles
        g.setColor(new Color(0xFFFFFF));
        g.fillRect(0, 0, round(map.mapWidth * scale), round(map.mapHeight * scale));
        for (Obstacle o : map.obstacles) {
            o.draw(g, scale);
        }
        // draw nodes
        g.setColor(new Color(0xFF0000));
        if (nodes != null)
            for (Node k : nodes) {
                g.fillOval(round(k.pos.x * scale) - startEndCircSize / 2, round(k.pos.y * scale) - startEndCircSize / 2, startEndCircSize, startEndCircSize);
                ArrayList<Node> nb = k.getNeighbors();
                for (Node kNeighbor : nb)
                    g.drawLine(round(k.pos.x * scale), round(k.pos.y * scale), round(kNeighbor.pos.x * scale), round(kNeighbor.pos.y * scale));
            }
        // draw start/end
        g.setColor(new Color(0xFF00FF));
        if (startP != null) {
            g.fillOval(round(startP.x * scale) - startEndCircSize / 2, round(startP.y * scale) - startEndCircSize / 2, startEndCircSize, startEndCircSize);
            if (endP != null)
                g.fillOval(round(endP.x * scale) - startEndCircSize / 2, round(endP.y * scale) - startEndCircSize / 2, startEndCircSize, startEndCircSize);
        }
        // draw way
        if (wayAnchor != null) {
            g.setColor(new Color(0x0000FF));
            Node l = null;
            for (Node n = wayAnchor; n != null; n = n.getParent()) {
                g.drawOval(round(n.pos.x * scale) - startEndCircSize / 2, round(n.pos.y * scale) - startEndCircSize / 2, startEndCircSize, startEndCircSize);
                if (l != null)
                    g.drawLine(round(n.pos.x * scale), round(n.pos.y * scale), round(l.pos.x * scale), round(l.pos.y * scale));
                l = n;
            }
        }
    }

    @Override
    public void initFrame(JFrame f, DrawFrame.DrawPanel dp) {
        dp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                float scale = frame.getScale();
                System.out.println("clicked " + e.getX() + ", " + e.getY() + " (scale = " + scale + ")");
                if (startP == null || (startP != null && endP != null)) {
                    startP = new PointF(e.getX() / scale, e.getY() / scale);
                    endP = null;
                } else
                    endP = new PointF(e.getX() / scale, e.getY() / scale);
                frame.redraw();
            }
        });
        f.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                System.out.println("KeyChar: '" + e.getKeyChar() + "'" + " keyCode:" + e.getKeyCode());
                switch (e.getKeyChar()) {
                    case ' ':
                        if (startP != null && endP != null) {
                            System.out.println("Starting algorithm..");
                            long time = System.nanoTime();
                            genNodes();
                            finishMatrix();
                            wayAnchor = algorithm();
                            System.out.println("is wayAnchor null? " + (wayAnchor == null));
                            System.out.println("Algorithm finished, required time: " + (System.nanoTime() - time) * 1e-6 + "ms");
                            dp.repaint();
                        }
                        break;
                }
            }
        });
    }

    private void genNodes() {
        ArrayList<Node> nodeList = new ArrayList<>(map.obstacles.length * 4);
        Node k1 = null, k2 = null, k3 = null, k4 = null;
        for (Obstacle o : map.obstacles) {
            boolean btl = true, btr = true, bbr = true, bbl = true;
            PointF tl = new PointF(o.x, o.y),
                    tr = new PointF(o.x + o.width, o.y),
                    br = new PointF(o.x + o.width, o.y + o.height),
                    bl = new PointF(o.x, o.y + o.height);
            for (int j = 0; j < map.obstacles.length; j++) {
                Obstacle oTest = map.obstacles[j];
                if (btl && (isNearHorz(tl, oTest.y + oTest.height, oTest.x, oTest.width) || isNearVert(tl, oTest.x + oTest.width, oTest.y, oTest.height)))
                    btl = false;
                if (btr && (isNearHorz(tr, oTest.y + oTest.height, oTest.x, oTest.width) || isNearVert(tr, oTest.x, oTest.y, oTest.height)))
                    btr = false;
                if (bbr && (isNearHorz(br, oTest.y, oTest.x, oTest.width) || isNearVert(br, oTest.x, oTest.y, oTest.height)))
                    bbr = false;
                if (bbl && (isNearHorz(bl, oTest.y, oTest.x, oTest.width) || isNearVert(bl, oTest.x + oTest.width, oTest.y, oTest.height)))
                    bbl = false;

            }
            if (btl) {
                k1 = new Node(tl, o);
                nodeList.add(k1);
            }
            if (btr) {
                k2 = new Node(tr, o);
                nodeList.add(k2);
            }
            if (bbr) {
                k3 = new Node(br, o);
                nodeList.add(k3);
            }
            if (bbl) {
                k4 = new Node(bl, o);
                nodeList.add(k4);
            }

            if (btl) {
                if (btr)
                    k1.addNeighborBoth(k2);
                if (bbl)
                    k1.addNeighborBoth(k4);
            }
            if (bbr) {
                if (btr)
                    k3.addNeighborBoth(k2);
                if (bbl)
                    k3.addNeighborBoth(k4);
            }
        }
        System.out.println("nodeList.size() = " + nodeList.size());
        nodes = nodeList.toArray(new Node[nodeList.size()]);
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
        for (Obstacle oCol : map.obstacles) {

            dx = kEnd.pos.x - kStart.pos.x;
            dy = kEnd.pos.y - kStart.pos.y;
//                    if (rayHitsObstacle(getAngle(dx, dy), oCol, kStart.pos, kEnd.pos)) {
            float mRay = getAngle(dx, dy);
            float endX = kEnd.pos.x - kStart.pos.x;
            float endY = kEnd.pos.y - kStart.pos.y;
            if (!(kStart.isOn(oCol.x, oCol.y) || kStart.isOn(oCol.x + oCol.width, oCol.y)
                    || kEnd.isOn(oCol.x, oCol.y) || kEnd.isOn(oCol.x + oCol.width, oCol.y))) {
                hits = intsHozLine(mRay, oCol.y - kStart.pos.y, oCol.x - kStart.pos.x, oCol.width, endX);
                if (hits)
                    break;
            }
            if (!(kStart.isOn(oCol.x, oCol.y + oCol.height) || kStart.isOn(oCol.x + oCol.width, oCol.y + oCol.height)
                    || kEnd.isOn(oCol.x, oCol.y + oCol.height) || kEnd.isOn(oCol.x + oCol.width, oCol.y + oCol.height))) {
                hits = intsHozLine(mRay, oCol.y + oCol.height - kStart.pos.y, oCol.x - kStart.pos.x, oCol.width, endX);
                if (hits)
                    break;
            }
            if (!(kStart.isOn(oCol.x, oCol.y) || kStart.isOn(oCol.x, oCol.y + oCol.height)
                    || kEnd.isOn(oCol.x, oCol.y) || kEnd.isOn(oCol.x, oCol.y + oCol.height))) {
                hits = intsVerLine(mRay, oCol.x - kStart.pos.x, oCol.y - kStart.pos.y, oCol.height, endY);
                if (hits)
                    break;
            }
            if (!(kStart.isOn(oCol.x + oCol.width, oCol.y) || kStart.isOn(oCol.x + oCol.width, oCol.y + oCol.height)
                    || kEnd.isOn(oCol.x + oCol.width, oCol.y) || kEnd.isOn(oCol.x + oCol.width, oCol.y + oCol.height))) {
                hits = intsVerLine(mRay, oCol.x + oCol.width - kStart.pos.x, oCol.y - kStart.pos.y, oCol.height, endY);
                if (hits)
                    break;
            }

        }
        if (!hits) {
            kStart.addNeighborBoth(kEnd);
        }
    }

    private float getAngle(float dx, float dy) {
        float m = dy / dx, tempAngle;
        if (dx < 0)
            tempAngle = (float) (Math.atan(m) + Math.PI);
        else if (dy < 0)
            tempAngle = (float) (Math.atan(m) + Math.PI * 2);
        else
            tempAngle = (float) (Math.atan(m));
        return tempAngle;
    }

    private boolean intsHozLine(float angle, float yB, float xB, float widthB, float posEndXRel) {
        /**horizontal col. detection:
         * A: line: y=m*x
         * B: y = a
         * => x = a/m, wenn x auf der Seite liegt (und Schnittpunkt zwischen den beiden Punkten(Start und End)), dann Kollision
         */
        float sx = intersectPointHoz(getGradientfromAngle(angle), yB);
        return between(sx, xB, xB + widthB) && between(sx, 0, posEndXRel);
    }

    private boolean intsVerLine(float angle, float xB, float yB, float heightB, float posEndYRel) {
        /**vertical col. detection:
         * A: line: y=m*x
         * B: x = a
         * => y = m*a, wenn y auf der Seite liegt, dann Kollision
         */
        float sy = intersectPointVer(getGradientfromAngle(angle), xB);
        return between(sy, yB, yB + heightB) && between(sy, 0, posEndYRel);
    }

    private boolean between(float a, float x1, float x2) {
        if (x1 <= x2)
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


    private Node algorithm() {
        // START
        // get connections to new start/end
        startN = new Node(startP, null);
        endN = new Node(endP, null);
        connectToAllInView(startN, nodes);
        connectToAllInView(endN, nodes);
        testInView(startN, null, endN);
        float mc;
        int startIndex = movementCosts.length - 2;
        int endIndex = movementCosts.length - 1;
        startN.setMatrixIndex(startIndex);
        endN.setMatrixIndex(endIndex);
        for (Node nb : startN.getNeighbors()) {
            mc = (float) Math.sqrt(Math.pow(startP.x - nb.pos.x, 2) + Math.pow(startP.y - nb.pos.y, 2));
            movementCosts[startIndex][nb.getMatrixIndex()] = mc;
            movementCosts[nb.getMatrixIndex()][startIndex] = mc;
        }
        for (Node nb : endN.getNeighbors()) {
            mc = (float) Math.sqrt(Math.pow(endP.x - nb.pos.x, 2) + Math.pow(endP.y - nb.pos.y, 2));
            movementCosts[endIndex][nb.getMatrixIndex()] = mc;
            movementCosts[nb.getMatrixIndex()][endIndex] = mc;
        }

        // heuristic, linear
        for (Node k : nodes) {
            float dX = k.pos.x - startP.x, dY = k.pos.y - startP.y;
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
                float newG = movementCosts[currNode.getMatrixIndex()][neighbor.getMatrixIndex()] + currNode.getG();
                // wenn das hier ein besserer Weg ist (oder der erste zu neighbor, wenn man da noch nicht war), dann bei neighbor diesen neuen Weg über currNode speichern
                if (openList.containsValue(neighbor)) {
                    if (neighbor.getG() < newG)
                        // zu neighbor gibt es schon einen besseren Weg, also nichts tun
                        continue;
                    // neighbor ist schon auf der openList, also mit altem key (=f) rauslöschen!
                    openList.remove(neighbor.getF());
                }
                // neuen F-Wert für neighbor und für diesen Weg bestimmen
                float newF = newG + currNode.getHeuristic();
                openList.put(newF, neighbor);
                neighbor.setF(newF);
                neighbor.setG(newG);
                neighbor.setParent(currNode); // noch restliche Zuweisungen
            }
        }
        // no possible way :(
        return null;
    }

    /**
     * UNUSED!!!!
     */
    @Override
    public void tick(int millisDelta) {
        frame.redraw();
    }
}

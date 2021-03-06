package simulation;


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

import static simulation.ColorFrame.*;
import static java.lang.Math.round;


/**
 * Created by Florian on 06.02.2016. and max ARRRRRRGH!!!
 */
public class Manager implements DrawInferface, FrameInitInterface, Tickable {

    public static final float MIN_PATH_WIDTH = 0.15f;

    //Params
    private boolean drawInfo = true, drawString = false, drawNodes = false, stepforstep = false, printMatrix = true;


    private Map map;
    private DrawFrame frame;
    private util.ClockNano clock;
    private PointF startP = null, endP = null;

    private Node[] nodes, allNodes;
    private float[][] movementCosts; // TODO indices: startN = length - 2 und endN = length - 1
    private Node startN, endN, wayAnchor = null;
    private TreeMap<Float, Node> openList = null;
    private HashSet<Node> closedList = null;
    private Node currNode;
    private boolean algFinished = false;
    private String help = "---HELP---\nclick for start-/endpoint\nspace: start Algorithm\ns: toggle draw string\ni: toggle draw info\nt: toggle step-for-step and path only\np: one step in step-for-step\n---\n";

    private int circSize = 10;

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
        System.out.println(help);
//        clock = new ClockNano(60, this);
//        clock.startTicking();
    }

    @Override
    public void draw(Graphics gp, float scale) {
        // draw obstacles
        Graphics2D g = (Graphics2D) gp;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0x4F4F4F));
        g.fillRect(0, 0, round(map.mapWidth * scale), round(map.mapHeight * scale));
        for (Obstacle o : map.obstacles) {
            o.draw(g, scale);
        }
        // draw nodes and paths

        if (drawNodes && allNodes != null) {

            for (Node k : allNodes) {
                if (k == null) {
                    System.out.println("a node is null");
                    continue;
                }
                if (k == startN)
                    g.setColor(C_PATHSTART);
                else if (k == endN)
                    g.setColor(C_PATHEND);
                else
                    g.setColor(C_NODE);
                g.fillOval(round(k.pos.x * scale) - circSize / 2, round(k.pos.y * scale) - circSize / 2, circSize, circSize);
                ArrayList<Node> nb = k.getNeighbors();
                g.setColor(C_NODE);
                for (Node kNeighbor : nb) // draw all paths
                    g.drawLine(round(k.pos.x * scale), round(k.pos.y * scale), round(kNeighbor.pos.x * scale), round(kNeighbor.pos.y * scale));
            }
        } else { // draw startP/endP
            if (startP != null) {
                g.setColor(C_PATHSTART);
                g.fillOval(round(startP.x * scale) - circSize / 2, round(startP.y * scale) - circSize / 2, circSize, circSize);
            }
            if (endP != null) {
                g.setColor(C_PATHEND);
                g.fillOval(round(endP.x * scale) - circSize / 2, round(endP.y * scale) - circSize / 2, circSize, circSize);
            }
        }
        // draw way
        if (algFinished && wayAnchor != null) {
            g.setColor(C_PATH);
            Node l = null;
            for (Node n = wayAnchor; n != null; n = n.getParent()) {
                if (drawNodes)
                    g.drawOval(round(n.pos.x * scale) - circSize / 2, round(n.pos.y * scale) - circSize / 2, circSize, circSize);
                if (l != null)
                    g.drawLine(round(n.pos.x * scale), round(n.pos.y * scale), round(l.pos.x * scale), round(l.pos.y * scale));
                l = n;
            }
        }
        // draw node values, list member markers, etc
        if (allNodes != null && drawInfo && stepforstep) {
            for (Node k : allNodes) {
                if (k == null)
                    continue;
                // draw info String
                if (drawString) {
                    g.setColor(C_FONT);
                    String info1 = k.getMatrixIndex() + ",G: " + to2Digit(k.getG() * 10);
                    String info2 = "H:" + to2Digit(k.getHeuristic() * 10) + ", F:" + to2Digit(k.getF() * 10);
                    g.setFont(new Font("Calibri", Font.PLAIN, 11));
                    g.drawString(info1, round(k.pos.x * scale) + circSize / 2, round(k.pos.y * scale) + circSize / 2);
                    g.drawString(info2, round(k.pos.x * scale) + circSize / 2, round(k.pos.y * scale) + circSize / 2 + 12);
                }
                if (k == currNode) { // is current node
                    g.setColor(C_CURRELEM);
                    g.drawLine(round(k.pos.x * scale), round(k.pos.y * scale) - circSize / 2, round(k.pos.x * scale), round(k.pos.y * scale) + circSize / 2);
                    g.drawLine(round(k.pos.x * scale) - circSize / 2, round(k.pos.y * scale), round(k.pos.x * scale) + circSize / 2, round(k.pos.y * scale));
                }
                if (openList.get(k.getF()) != null) { // is in openList
                    g.setColor(C_OPENLIST);
                    g.drawOval(round(k.pos.x * scale) - circSize / 4, round(k.pos.y * scale) - circSize / 4, circSize / 2, circSize / 2);
                }
                if (closedList.contains(k)) { // is in closedList
                    g.setColor(C_CLOSEDLIST);
                    g.drawOval(round(k.pos.x * scale) - circSize / 4, round(k.pos.y * scale) - circSize / 4, circSize / 2, circSize / 2);
                }
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
                if (algFinished) {
                    algFinished = false;
                    startP = null;
                    endP = null;
                    allNodes = null;
                    frame.redraw();
                }
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
//                System.out.println("KeyChar: '" + e.getKeyChar() + "'" + " keyCode:" + e.getKeyCode());
                switch (e.getKeyChar()) {
                    case ' ':
                        if (startP != null && endP != null) {
                            new Thread() {
                                public void run() {
                                    System.out.println("Starting Algorithm..");
                                    long time = System.nanoTime();
                                    genNodes();
                                    finishMatrix();
                                    wayAnchor = algorithm();
                                    System.out.println("is wayAnchor null? " + (wayAnchor == null));
                                    System.out.println("Algorithm finished, required time: " + (System.nanoTime() - time) * 1e-6 + "ms");
                                    algFinished = true;
                                    dp.repaint();
                                }
                            }.start();
                        }
                        break;
                    case 'p':
                        synchronized (Manager.this) {
                            Manager.this.notify();
                        }
                        break;
                    case 'i':
                        drawInfo = !drawInfo;
                        System.out.println("drawInfo = " + drawInfo);
                        break;
                    case 's':
                        drawString = !drawString;
                        System.out.println("drawString = " + drawString);
                        break;
                    case 't':
                        stepforstep = !stepforstep;
                        System.out.println("stepforstep = " + stepforstep);
                        break;
                    default:
                        System.out.println(help);
                }
            }
        });
    }

    private void genNodes() {
        ArrayList<Node> nodeList = new ArrayList<>(map.obstacles.length * 4);
        Node k1 = null, k2 = null, k3 = null, k4 = null;
        boolean btl = true, btr = true, bbr = true, bbl = true;
        for (Obstacle o : map.obstacles) {
            btl = true;
            btr = true;
            bbr = true;
            bbl = true;
            PointF tl = new PointF(o.x, o.y),
                    tr = new PointF(o.x + o.width, o.y),
                    br = new PointF(o.x + o.width, o.y + o.height),
                    bl = new PointF(o.x, o.y + o.height);
            for (int j = 0; j < map.obstacles.length; j++) {
                Obstacle oTest = map.obstacles[j];
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
        for (Obstacle o : map.obstacles) {
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


    private Node algorithm() {
        // START
        // get connections to new start/end
        startN = new Node(startP, null);
        endN = new Node(endP, null);
        // TODO wenn nicht zeichnen, dann das hier weglassen!
        allNodes = new Node[nodes.length + 2];
        for (int i = 0; i < nodes.length; i++) {
            allNodes[i] = nodes[i];
        }
        allNodes[nodes.length] = startN;
        allNodes[nodes.length + 1] = endN;
        //##############
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
        if (printMatrix)
            printMatrix(movementCosts);
        // heuristic, linear
        for (Node k : nodes) {
            float dX = k.pos.x - startP.x, dY = k.pos.y - startP.y;
            k.setHeuristic((float) Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)));
        }
        if (printMatrix)
            printHeuristic();
        // a*-Algorithm
        openList = new TreeMap<>();
        closedList = new HashSet<>();

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
                float newF = newG + neighbor.getHeuristic();
                openList.put(newF, neighbor);
                neighbor.setF(newF);
                neighbor.setG(newG);
                neighbor.setParent(currNode); // noch restliche Zuweisungen

                // TODO wenn nicht zeichnen, weg damit
                frame.redraw();
                if (stepforstep) {
                    synchronized (this) {
                        try {
//                        System.out.println("wait now");
                            wait();
//                        System.out.println("notified");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        // no possible way :(
        return null;
    }

    private void printHeuristic() {
        System.out.println("Heuristic: ");
        for (int i = 0; i < nodes.length; i++) {
            System.out.print(to2Digit(i) + " | ");
        }
        System.out.println();
        for (Node n : nodes) {
            System.out.print(to2Digit(n.getHeuristic() * 10) + " | ");
        }
        System.out.println();
    }

    public static void printMatrix(float[][] movementCosts) {
        System.out.println("Matrix: ");

        System.out.print("      ");
        for (int i = 0; i < movementCosts.length; i++) {
            System.out.print(to2Digit(i) + " | ");
        }
        System.out.println();
        System.out.print("______");
        for (int i = 0; i < movementCosts.length; i++) {
            System.out.print("_____");
        }
        System.out.println();
        for (int i = 0; i < movementCosts.length; i++) {
            System.out.print(to2Digit(i) + "::  ");
            for (int j = 0; j < movementCosts[i].length; j++) {
                System.out.print(to2Digit(movementCosts[i][j] * 10) + " | ");
            }
            System.out.println();
        }

    }

    private static String to2Digit(float f) {
        int i = round(f);
        if (i < 10)
            return " " + i;
        else
            return "" + i;
    }

    /**
     * UNUSED!!!!
     */
    @Override
    public void tick(int millisDelta) {
        frame.redraw();
    }
}

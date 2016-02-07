package ngb;


import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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
    private Node startK, endK;

    private int startEndCircSize = 10;

    public static void main(String args[]) {
        try {
            new Manager("map2.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //..
    public Manager(String mapFile) throws Exception {
        map = Map.readMap(mapFile);
        frame = new DrawFrame(new Dimension(700, 700), this, this, new DimensionF(map.mapWidth, map.mapHeight));
//        clock = new ClockNano(60, this);
//        clock.startTicking();
    }

    @Override
    public void draw(Graphics g, float scale) {
        for (Obstacle o : map.obstacles) {
            o.draw(g, scale);
        }
        g.setColor(new Color(0xff0000));
        if (startP != null) {
            g.fillOval(round(startP.x * scale) - startEndCircSize / 2, round(startP.y * scale) - startEndCircSize / 2, startEndCircSize, startEndCircSize);
            if (endP != null)
                g.fillOval(round(endP.x * scale) - startEndCircSize / 2, round(endP.y * scale) - startEndCircSize / 2, startEndCircSize, startEndCircSize);
        }
        if (nodes != null)
            for (Node k : nodes) {
                ArrayList<Node> nb = k.getNeighbors();
                for (Node kNeighbor : nb)
                    g.drawLine(round(k.pos.x * scale), round(k.pos.y * scale), round(kNeighbor.pos.x * scale), round(kNeighbor.pos.y * scale));
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
                if (startP == null)
                    startP = new PointF(e.getX() / scale, e.getY() / scale);
                else if (endP == null)
                    endP = new PointF(e.getX() / scale, e.getY() / scale);
                frame.redraw();
            }
        });
        f.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                System.out.println("keyEvent: '" + e.getKeyChar() + "'");
                switch (e.getKeyChar()) {
                    case ' ':
                        if (startP != null && endP != null) {
                            System.out.println("Starting algorithm..");
                            long time = System.nanoTime();
                            genNodes();
                            finishMatrix();
                            algorithm();
                            System.out.println("Algorithm finished, required time: " + (System.nanoTime() - time) * 1e-6 + "ms");

                        }
                        break;
                }
            }
        });
    }

    private void genNodes() {
        ArrayList<Node> nodeList = new ArrayList<>(map.obstacles.length * 4);
        int i = 0;
        Node k1, k2, k3, k4;
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
                if (btr && (isNearHorz(tl, oTest.y + oTest.height, oTest.x, oTest.width) || isNearVert(tl, oTest.x, oTest.y, oTest.height)))
                    btr = false;
                if (bbr && (isNearHorz(tl, oTest.y, oTest.x, oTest.width) || isNearVert(tl, oTest.x, oTest.y, oTest.height)))
                    bbr = false;
                if (bbl && (isNearHorz(tl, oTest.y, oTest.x, oTest.width) || isNearVert(tl, oTest.x + oTest.width, oTest.y, oTest.height)))
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


            //TODO überprüfen, ob Knoten erreichbar sind
            nodes[i].addNeighborBoth(nodes[i + 1]);
            nodes[i].addNeighborBoth(nodes[i + 3]);

            nodes[i + 2].addNeighborBoth(nodes[i + 1]);
            nodes[i + 2].addNeighborBoth(nodes[i + 3]);

            i += 4;
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

    private void finishMatrix() {
        java.util.List<Node> rest = new ArrayList<>(nodes.length);
        for (int i = nodes.length - 1; i >= 0; i--)
            rest.add(nodes[i]);
//        for (int i = 0; i < map.obstacles.length; i++) {
        int i = 0;
        float dx, dy;
        boolean hits = false;
        for (Node kStart : nodes) {
            Obstacle oStart = kStart.o;
            for (Node kEnd : rest) {
                Obstacle oEnd = kEnd.o;
                if (oStart == oEnd)
                    continue;
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
                } else
                    hits = false;
            }
            //(map.obstacles.length - 1 - i) should be the index of kStart
            rest.remove(nodes.length - 1 - i);
            i++;
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


    private void algorithm() {
        System.out.println("Starting algorithm..");
        long time = System.nanoTime();
        // START


        // heuristic, linear
        for (Node k : nodes) {
            float dX = k.pos.x - endP.x, dY = k.pos.y - endP.y;
            k.setHeuristic((float) Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)));
        }
        // END
//        System.out.println("Algorithm finished, required time: " + (System.nanoTime() - time) * 1e-6 + "ms");
    }

    /**
     * UNUSED!!!!
     */
    @Override
    public void tick(int millisDelta) {
        frame.redraw();
    }
}

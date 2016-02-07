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

    private Map map;
    private DrawFrame frame;
    private util.ClockNano clock;
    private PointF startP = null, endP = null;

    private Knot[] knots;
    private Knot startK, endK;

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
        if (knots != null)
            for (Knot k : knots) {
                ArrayList<Knot> nb = k.getNeighbors();
                for (Knot kNeighbor : nb)
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
                            genKnots();
                            finishMatrix();
                            algorithm();
                            System.out.println("Algorithm finished, required time: " + (System.nanoTime() - time) * 1e-6 + "ms");

                        }
                        break;
                }
            }
        });
    }

    private void genKnots() {
        knots = new Knot[map.obstacles.length * 4];
        int i = 0;
        for (Obstacle o : map.obstacles) {
            knots[i] = new Knot(new PointF(o.x, o.y), o);
            knots[i + 1] = new Knot(new PointF(o.x + o.width, o.y), o);
            knots[i + 2] = new Knot(new PointF(o.x + o.width, o.y + o.height), o);
            knots[i + 3] = new Knot(new PointF(o.x, o.y + o.height), o);

            knots[i].addNeighborBoth(knots[i + 1]);
            knots[i].addNeighborBoth(knots[i + 3]);

            knots[i + 2].addNeighborBoth(knots[i + 1]);
            knots[i + 2].addNeighborBoth(knots[i + 3]);

            i += 4;
        }
    }

    private void finishMatrix() {
        java.util.List<Knot> rest = new ArrayList<>(knots.length);
        for (int i = knots.length - 1; i >= 0; i--)
            rest.add(knots[i]);
//        for (int i = 0; i < map.obstacles.length; i++) {
        int i = 0;
        float dx, dy;
        boolean hits = false;
        for (Knot kStart : knots) {
            Obstacle oStart = kStart.o;
            for (Knot kEnd : rest) {
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
            rest.remove(knots.length - 1 - i);
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

    private boolean rayHitsObstacle(float mRay, Obstacle o, PointF posStart, PointF posEnd) {
        //Ursprung ist x,y
        return intsHozLine(mRay, o.y - posStart.y, o.x - posStart.x, o.width, posEnd.x - posStart.x)
                || intsHozLine(mRay, o.y + o.height - posStart.y, o.x - posStart.x, o.width, posEnd.x - posStart.x)
                || intsVerLine(mRay, o.x - posStart.x, o.y - posStart.y, o.height, posEnd.y - posStart.y)
                || intsVerLine(mRay, o.x + o.width - posStart.x, o.y - posStart.y, o.height, posEnd.y - posStart.y);
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
        for (Knot k : knots) {
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

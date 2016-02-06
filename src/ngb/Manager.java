package ngb;


import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.lang.Math.round;


/**
 * Created by Florian on 06.02.2016. and max ARRRRRRGH!!!
 */
public class Manager implements DrawInferface, FrameInitInterface, Tickable {

    private Map map;
    private DrawFrame frame;
    private util.ClockNano clock;
    private PointF startP = null, endP = null;

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
        dp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        algorithm();
                        break;
                }
            }
        });
    }

    private void algorithm() {
        System.out.println("Starting algorithm..");
        long time = System.currentTimeMillis();
        // START

        // END
        System.out.println("Algorithm finished, required time: " + String.valueOf(System.currentTimeMillis() - time) + "ms");
    }

    /**
     * UNUSED!!!!
     */
    @Override
    public void tick(int millisDelta) {
        frame.redraw();
    }
}

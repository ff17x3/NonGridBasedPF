package ngb;


import util.*;

import javax.swing.*;
import java.awt.*;
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

    public static void main(String args[]) {
        try {
            new Manager("map2.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Manager(String mapFile) throws Exception {
        map = Map.readMap(mapFile);
        frame = new DrawFrame(new Dimension(700, 700), this, this, new DimensionF(map.mapWidth, map.mapHeight));
        clock = new ClockNano(60, this);
    }

    @Override
    public void draw(Graphics g, float scale) {
        for (Obstacle o : map.obstacles) {
            o.draw(g, scale);
        }
        g.setColor(new Color(0xff0000));
        g.fillOval(round(startP.x * scale), round(startP.y * scale), 10, 10);
        g.fillOval(round(endP.x * scale), round(endP.y * scale), 10, 10);
    }

    @Override
    public void initFrame(JFrame f, DrawFrame.DrawPanel dp) {
        dp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                float scale = frame.getScale();
                if (startP == null)
                    startP = new PointF(e.getX() / scale, e.getY() / scale);
                else if (endP == null)
                    endP = new PointF(e.getX() / scale, e.getY() / scale);
            }
        });
    }

    @Override
    public void tick(int millisDelta) {
        frame.redraw();
    }
}

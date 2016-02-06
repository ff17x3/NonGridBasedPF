package ngb;


import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Florian on 06.02.2016.
 */
public class Manager implements DrawInferface, FrameInitInterface, Tickable {

    private Map map;
    private DrawFrame frame;
    private util.ClockNano clock;

    public static void main(String args[]) {
        try {
            new Manager("map2.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//.
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
    }

    @Override
    public void initFrame(JFrame f, DrawFrame.DrawPanel dp) {
        dp.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

        });
    }

    @Override
    public void tick(int millisDelta) {
        frame.redraw();
    }
}

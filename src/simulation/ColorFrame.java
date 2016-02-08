package simulation;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Florian on 08.02.2016.
 */
public class ColorFrame extends JFrame {
    public static final Color C_BARRIER = new Color(0x000000),
            C_PATHSTART = new Color(0xff00ff),
            C_PATHEND = new Color(0x990099),
            C_PATH = new Color(0x0000ff),
            C_OPENLIST = new Color(0xffffff),
            C_CLOSEDLIST = new Color(0x000000),
            C_CURRELEM = new Color(0xff7400),
            C_FONT = new Color(0xffff00),
            C_NODE = new Color(0xff0000);

    public ColorFrame() {
        super();
        JPanel pan = new JPanel();
        pan.setLayout(new GridLayout(0, 1));
        int fontsize = 16;

        JLabel node = new JLabel("  Node");
        node.setBackground(C_NODE);
        node.setFont(new Font("Calibri", Font.PLAIN, fontsize));
        node.setForeground(C_FONT);
        node.setOpaque(true);
        pan.add(node);

        JLabel barrier = new JLabel("  Barrier");
        barrier.setBackground(C_BARRIER);
        barrier.setFont(new Font("Calibri", Font.PLAIN, fontsize));
        barrier.setForeground(C_FONT);
        barrier.setOpaque(true);
        pan.add(barrier);


        JLabel pathstart = new JLabel("  Start");
        pathstart.setBackground(C_PATHSTART);
        pathstart.setFont(new Font("Calibri", Font.PLAIN, fontsize));
        pathstart.setForeground(C_FONT);
        pathstart.setOpaque(true);
        pan.add(pathstart);

        JLabel pathend = new JLabel("  End");
        pathend.setBackground(C_PATHEND);
        pathend.setFont(new Font("Calibri", Font.PLAIN, fontsize));
        pathend.setForeground(C_FONT);
        pathend.setOpaque(true);
        pan.add(pathend);

        JLabel path = new JLabel("  Path");
        path.setBackground(C_PATH);
        path.setFont(new Font("Calibri", Font.PLAIN, fontsize));
        path.setForeground(C_FONT);
        path.setOpaque(true);
        pan.add(path);

        JLabel openlist = new JLabel("  On open list");
        openlist.setBackground(C_OPENLIST);
        openlist.setFont(new Font("Calibri", Font.PLAIN, fontsize));
        openlist.setForeground(C_FONT);
        openlist.setOpaque(true);
        pan.add(openlist);

        JLabel closedlist = new JLabel("  On closed list");
        closedlist.setBackground(C_CLOSEDLIST);
        closedlist.setFont(new Font("Calibri", Font.PLAIN, fontsize));
        closedlist.setForeground(C_FONT);
        closedlist.setOpaque(true);
        pan.add(closedlist);

        JLabel currelem = new JLabel("  Current element");
        currelem.setBackground(C_CURRELEM);
        currelem.setFont(new Font("Calibri", Font.PLAIN, fontsize));
        currelem.setForeground(C_FONT);
        currelem.setOpaque(true);
        pan.add(currelem);


        // TODO buttons für dialog, farben bei dunkelblau etc
        add(pan);
        pack();
        setResizable(false);
        setLocation(80, 80);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new ColorFrame();
    }

}
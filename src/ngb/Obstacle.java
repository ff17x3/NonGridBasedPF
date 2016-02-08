package ngb;

import java.awt.*;
import java.util.Scanner;

public class Obstacle {

    public final float x, y, width, height;
    private int xRound, yRound, wRound, hRound;

    private boolean t = true, b = true, r = true, l = true,
            tl = true, tr = true, bl = true, br = true;

    public Obstacle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

    }

    public static Obstacle createFromFile(String line) {
        Scanner sc = new Scanner(line);
        float x, y, width, height;
        x = Float.parseFloat(sc.next());
        y = Float.parseFloat(sc.next());
        width = Float.parseFloat(sc.next());
        height = Float.parseFloat(sc.next());
        System.out.println("Obstacle erstellt: " + x + ", " + y + " | " + width + ", " + height);
        sc.close();
        return new Obstacle(x, y, width, height);
    }

    public void draw(Graphics g, float scale) {
        g.setColor(Color.BLACK);
        g.fillRect(xRound, yRound, wRound, hRound);
    }

    public void updateScale(float scale) {
        xRound = Math.round(x * scale);
        yRound = Math.round(y * scale);
        //                      actSize Right Coor| drawed left Coor
        wRound = Math.round((x + width) * scale) - xRound;
        hRound = Math.round((y + height) * scale) - yRound;
    }

    public boolean isT() {
        return t;
    }

    public void setT(boolean t) {
        this.t = t;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public boolean isR() {
        return r;
    }

    public void setR(boolean r) {
        this.r = r;
    }

    public boolean isL() {
        return l;
    }

    public void setL(boolean l) {
        this.l = l;
    }

    public boolean isTl() {
        return tl;
    }

    public void setTl(boolean tl) {
        this.tl = tl;
    }

    public boolean isTr() {
        return tr;
    }

    public void setTr(boolean tr) {
        this.tr = tr;
    }

    public boolean isBl() {
        return bl;
    }

    public void setBl(boolean bl) {
        this.bl = bl;
    }

    public boolean isBr() {
        return br;
    }

    public void setBr(boolean br) {
        this.br = br;
    }
}

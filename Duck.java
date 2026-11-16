import java.awt.Rectangle;

/** Bebek penjaga kolam. */
public class Duck {
    public static final int W = 24, H = 18;
    public static final double SPEED = 170; // piksel/detik

    public double x, y;
    public boolean facingRight = true;           // arah hadap: true = kanan, false = kiri
    public boolean up, down, left, right;        // status tombol

    public Duck(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, W, H);
    }

    public void update(double dt, GameLogic logic) {
        double dx = 0, dy = 0;
        if (left) dx -= 1;
        if (right) dx += 1;
        if (up) dy -= 1;
        if (down) dy += 1;

        // bebek menoleh sesuai arah gerak
        if (dx < 0) facingRight = false;
        else if (dx > 0) facingRight = true;

        // gerak diagonal tidak boleh lebih cepat
        if (dx != 0 && dy != 0) {
            dx *= 0.7071;
            dy *= 0.7071;
        }
        moveDuck(dx * SPEED * dt, dy * SPEED * dt, logic);
    }

    /** Gerak per sumbu supaya bebek bisa "meluncur" di sepanjang tepi kolam. */
    private void moveDuck(double mx, double my, GameLogic logic) {
        if (logic.canStand(x + mx, y, W, H)) x += mx;
        if (logic.canStand(x, y + my, W, H)) y += my;
    }
}

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/** Menggambar semua layar, menerima input, dan menjalankan game loop (javax.swing.Timer). */
public class GamePanel extends JPanel
        implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    public static final int W = GameLogic.COLS * GameLogic.TILE; // 960
    public static final int H = GameLogic.ROWS * GameLogic.TILE; // 640

    // layar
    private static final int MENU = 0, HOWTO = 1, SDG = 2, PLAYING = 3, LEVEL_DONE = 4, RESULT = 5;
    private int screen = MENU;

    private final GameLogic logic = new GameLogic();
    private final Timer timer = new Timer(16, this);
    private final Random rnd = new Random();
    private long lastNano;
    private double time;          // waktu animasi (detik)

    // menu
    private int menuSel = 0;
    private final String[] menuItems = { "PLAY GAME", "HOW TO PLAY", "SDGs INFORMATION", "EXIT" };
    private final Rectangle[] menuRects = new Rectangle[4];
    private int[][] bgMap, bgDecor;
    private String notice = "";   // pesan untuk fitur yang belum dibuat
    private double noticeTimer;

    // font & warna
    private static final Font F_TINY = new Font(Font.MONOSPACED, Font.BOLD, 11);
    private static final Font F_S = new Font(Font.MONOSPACED, Font.BOLD, 14);
    private static final Font F_M = new Font(Font.MONOSPACED, Font.BOLD, 18);
    private static final Font F_L = new Font(Font.MONOSPACED, Font.BOLD, 30);
    private static final Font F_XL = new Font(Font.MONOSPACED, Font.BOLD, 54);
    private static final Color YELLOW = new Color(255, 217, 59);
    private static final Color CYAN = new Color(120, 220, 255);
    private static final Color GREEN = new Color(120, 255, 140);
    private static final Color RED = new Color(255, 120, 120);
    private static final String HINT = "SPASI/E: buang   ESC: menu";

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        for (int i = 0; i < menuRects.length; i++) {
            menuRects[i] = new Rectangle(W / 2 - 150, 300 + i * 64, 300, 48);
        }
        newBackground();
    }

    public void start() {
        lastNano = System.nanoTime();
        timer.start();
    }

    private void newBackground() {
        bgMap = GameLogic.generateMap(rnd);
        bgDecor = GameLogic.generateDecor(rnd);
    }

    // ===================== Game loop =====================

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = Math.min(0.05, (now - lastNano) / 1e9);
        lastNano = now;
        time += dt;
        noticeTimer -= dt;

        if (screen == PLAYING) logic.update(dt);
        repaint();
    }

    private void activate(int item) {
        switch (item) {
            case 0:
                logic.startNewGame();
                screen = PLAYING;
                break;
            case 1:
            case 2:
                notice = menuItems[item] + " : menyusul di tahap berikutnya";
                noticeTimer = 2.5;
                break;
            default: System.exit(0);
        }
    }

    private void goMenu() {
        screen = MENU;
        newBackground();
    }

    // ===================== Menggambar =====================

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D) gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        switch (screen) {
            case MENU: drawMenu(g); break;
            case PLAYING: drawGame(g); break;
            default: break;
        }
        Toolkit.getDefaultToolkit().sync();
    }

    // ---------- helper ----------

    private void text(Graphics2D g, String s, int x, int y, Font f, Color c) {
        g.setFont(f);
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(s, x + 2, y + 2);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    private void centerText(Graphics2D g, String s, int cx, int y, Font f, Color c) {
        FontMetrics fm = g.getFontMetrics(f);
        text(g, s, cx - fm.stringWidth(s) / 2, y, f, c);
    }

    private void dim(Graphics2D g, int alpha) {
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, W, H);
    }

    private void panel(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(Color.BLACK);
        g.fillRect(x - 4, y - 4, w + 8, h + 8);
        g.setColor(new Color(20, 28, 48));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(70, 100, 150));
        g.drawRect(x + 4, y + 4, w - 9, h - 9);
    }

    private Color mix(Color a, Color b, double k) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * k),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * k));
    }

    private void bar(Graphics2D g, int x, int y, int w, int h, double pct, Color fill, String label, int target) {
        g.setColor(new Color(25, 25, 40));
        g.fillRect(x, y, w, h);
        g.setColor(fill);
        g.fillRect(x + 2, y + 2, (int) ((w - 4) * Math.max(0, Math.min(1, pct / 100.0))), h - 4);
        if (target > 0) {
            g.setColor(Color.WHITE);
            g.fillRect(x + 2 + (int) ((w - 4) * target / 100.0), y, 2, h);
        }
        g.setColor(new Color(255, 255, 255, 140));
        g.drawRect(x, y, w - 1, h - 1);
        centerText(g, label, x + w / 2, y + h - 3, F_TINY, Color.WHITE);
    }

    // ---------- peta ----------

    /** clean (0..100): makin bersih, air makin jernih. */
    private void drawMap(Graphics2D g, int[][] map, int[][] decor, double clean) {
        double k = Math.max(0, Math.min(1, clean / 100.0));
        Color wa = mix(new Color(78, 118, 108), new Color(61, 169, 232), k);
        Color wb = mix(new Color(88, 130, 118), new Color(72, 184, 242), k);
        Color wave = mix(new Color(130, 165, 150), new Color(170, 225, 255), k);
        int T = GameLogic.TILE;
        int phase = (int) (time * 2);

        for (int r = 0; r < GameLogic.ROWS; r++) {
            for (int c = 0; c < GameLogic.COLS; c++) {
                int x = c * T, y = r * T, v = map[r][c];
                if (v == GameLogic.LAND) {
                    drawLand(g, map, decor, r, c, x, y);
                    continue;
                }
                g.setColor(((r + c) & 1) == 0 ? wa : wb);
                g.fillRect(x, y, T, T);
                if (((c * 5 + r * 3 + phase) % 9) == 0) {
                    g.setColor(wave);
                    g.fillRect(x + 4 + (c * 7) % 12, y + 6 + (r * 5) % 14, 10, 2);
                    g.fillRect(x + 16 + (r * 3) % 8, y + 22, 6, 2);
                }
                if (v == GameLogic.LILY) {
                    g.setColor(new Color(52, 168, 83));
                    g.fillRect(x + 6, y + 10, 20, 14);
                    g.fillRect(x + 10, y + 7, 12, 3);
                    g.fillRect(x + 10, y + 24, 12, 3);
                    g.setColor(new Color(36, 130, 62));
                    g.fillRect(x + 6, y + 22, 20, 2);
                    if (decor[r][c] % 3 == 0) {
                        g.setColor(new Color(255, 128, 171));
                        g.fillRect(x + 13, y + 12, 6, 6);
                        g.setColor(YELLOW);
                        g.fillRect(x + 15, y + 14, 2, 2);
                    }
                } else if (v == GameLogic.ROCK) {
                    g.setColor(new Color(0, 0, 0, 50));
                    g.fillRect(x + 3, y + 26, 26, 4);
                    g.setColor(new Color(120, 124, 135));
                    g.fillRect(x + 4, y + 10, 24, 18);
                    g.fillRect(x + 8, y + 5, 14, 6);
                    g.setColor(new Color(155, 160, 172));
                    g.fillRect(x + 4, y + 10, 24, 5);
                    g.fillRect(x + 8, y + 5, 14, 3);
                    g.setColor(new Color(85, 88, 100));
                    g.fillRect(x + 4, y + 24, 24, 4);
                }
            }
        }
    }

    private void drawLand(Graphics2D g, int[][] map, int[][] decor, int r, int c, int x, int y) {
        int T = GameLogic.TILE;
        int d = decor[r][c];
        g.setColor((d & 1) == 0 ? new Color(86, 170, 76) : new Color(78, 160, 68));
        g.fillRect(x, y, T, T);

        boolean up = GameLogic.walk(map, r - 1, c), dn = GameLogic.walk(map, r + 1, c);
        boolean lf = GameLogic.walk(map, r, c - 1), rt = GameLogic.walk(map, r, c + 1);
        if (up || dn || lf || rt) {   // tepi kolam: pasir
            g.setColor(new Color(230, 207, 139));
            if (up) g.fillRect(x, y, T, 7);
            if (dn) g.fillRect(x, y + T - 7, T, 7);
            if (lf) g.fillRect(x, y, 7, T);
            if (rt) g.fillRect(x + T - 7, y, 7, T);
            return;
        }
        if (d <= 2) {                 // rumput
            g.setColor(new Color(52, 128, 52));
            g.fillRect(x + 8 + d * 5, y + 14, 2, 7);
            g.fillRect(x + 11 + d * 5, y + 17, 2, 5);
        } else if (d == 3) {          // bunga
            Color[] flowers = { Color.WHITE, new Color(255, 128, 171), new Color(255, 200, 80) };
            g.setColor(flowers[(r + c) % 3]);
            g.fillRect(x + 12, y + 12, 6, 6);
            g.setColor(new Color(220, 60, 60));
            g.fillRect(x + 14, y + 14, 2, 2);
            g.setColor(new Color(52, 128, 52));
            g.fillRect(x + 14, y + 18, 2, 6);
        } else if (d == 4) {          // semak
            g.setColor(new Color(40, 120, 50));
            g.fillRect(x + 4, y + 10, 24, 17);
            g.fillRect(x + 8, y + 6, 16, 6);
            g.setColor(new Color(62, 150, 62));
            g.fillRect(x + 8, y + 12, 7, 4);
            g.fillRect(x + 18, y + 18, 6, 4);
        }
    }

    // ---------- layar permainan ----------

    private void drawGame(Graphics2D g) {
        drawMap(g, logic.map, logic.decor, 100);

        // ikan (jumlah mengikuti kebersihan kolam mulai tahap 2)
        int fishShown = GameLogic.FISH_COUNT;
        for (int i = 0; i < fishShown; i++) {
            Sprites.draw(g, Sprites.FISH_IMG, (int) logic.fishX[i], (int) logic.fishY[i], 2, logic.fishDir[i] < 0);
        }

        // bebek (menoleh sesuai arah gerak)
        Duck d = logic.duck;
        int bob = (int) Math.round(Math.sin(time * 6) * 1.5);
        int wob = (int) (Math.sin(time * 5) * 2);
        g.setColor(new Color(255, 255, 255, 90));
        g.drawOval((int) d.x - 8 - wob, (int) d.y + 6, 40 + 2 * wob, 14);
        g.drawOval((int) d.x - 4, (int) d.y + 9, 32, 8);
        Sprites.draw(g, Sprites.DUCK_IMG, (int) d.x - 6, (int) d.y - 15 + bob, 3, !d.facingRight);

        drawHud(g);
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(15, 18, 35, 225));
        g.fillRect(0, 0, W, 34);
        g.setColor(new Color(70, 100, 150));
        g.fillRect(0, 34, W, 2);
        // text(g, "TAHAP 1 : peta kolam dan bebek", 12, 23, F_S, YELLOW);
        text(g, "W/A/S/D atau panah: gerak   ESC: menu", 12, 400, F_S, Color.WHITE);
    }

    // ---------- menu & halaman informasi ----------

    private void drawMenu(Graphics2D g) {
        drawMap(g, bgMap, bgDecor, 100);
        dim(g, 60);

        centerText(g, "DUCK POND", W / 2, 95, F_XL, YELLOW);
        centerText(g, "GUARDIAN", W / 2, 155, F_XL, Color.WHITE);
        centerText(g, "Clean the Pond, Save the Ecosystem!", W / 2, 195, F_M, CYAN);

        // bebek besar bergoyang, sesekali menoleh
        boolean faceRight = ((int) (time / 2.5)) % 2 == 0;
        int bob = (int) Math.round(Math.sin(time * 4) * 4);
        Sprites.draw(g, Sprites.DUCK_IMG, 110, 290 + bob, 12, !faceRight);

        for (int i = 0; i < menuRects.length; i++) {
            Rectangle r = menuRects[i];
            boolean sel = i == menuSel;
            g.setColor(Color.BLACK);
            g.fillRect(r.x - 3, r.y - 3, r.width + 6, r.height + 6);
            g.setColor(sel ? YELLOW : new Color(36, 70, 110));
            g.fillRect(r.x, r.y, r.width, r.height);
            g.setColor(sel ? new Color(255, 240, 150) : new Color(60, 100, 150));
            g.fillRect(r.x, r.y, r.width, 6);
            centerText(g, menuItems[i], r.x + r.width / 2, r.y + 32, F_M, sel ? new Color(40, 30, 0) : Color.WHITE);
        }

        if (noticeTimer > 0) centerText(g, notice, W / 2, H - 52, F_S, YELLOW);

        centerText(g, "W/S atau panah + ENTER, atau klik mouse", W / 2, H - 22, F_S, Color.WHITE);
    }

    // ===================== Input =====================

    private void clearDuckKeys() {
        Duck d = logic.duck;
        if (d != null) d.up = d.down = d.left = d.right = false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        switch (screen) {
            case MENU:
                if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) menuSel = (menuSel + 3) % 4;
                else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) menuSel = (menuSel + 1) % 4;
                else if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) activate(menuSel);
                break;
            case PLAYING:
                Duck d = logic.duck;
                if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) d.left = true;
                else if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) d.right = true;
                else if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) d.up = true;
                else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) d.down = true;
                else if (k == KeyEvent.VK_ESCAPE) goMenu();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Duck d = logic.duck;
        if (d == null) return;
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) d.left = false;
        else if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) d.right = false;
        else if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) d.up = false;
        else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) d.down = false;
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (screen != MENU) return;
        for (int i = 0; i < menuRects.length; i++) {
            if (menuRects[i].contains(e.getPoint())) menuSel = i;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (screen == MENU) {
            for (int i = 0; i < menuRects.length; i++) {
                if (menuRects[i].contains(e.getPoint())) activate(i);
            }
        }
    }

    @Override public void mouseDragged(MouseEvent e) { }
    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
}

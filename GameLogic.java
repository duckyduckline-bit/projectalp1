import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Aturan permainan. Isinya bertambah per tahap:
 *   Tahap 1: peta kolam (2D array), penempatan bebek, tabrakan dengan tepi kolam.
 *   Tahap 2: sampah acak, tempat sampah, ambil/buang, skor, persentase kebersihan.
 *   Tahap 3: timer, 5 nyawa, kondisi menang/kalah, tiga level.
 *   Tahap 4: leaderboard.
 */
public class GameLogic {

    // ===== Peta =====
    public static final int TILE = 32, COLS = 30, ROWS = 20;
    public static final int WATER = 0, LAND = 1, LILY = 2, ROCK = 3;

    public static final int FISH_COUNT = 6;

    public final Random rnd = new Random();

    public int[][] map;     // 2D array peta kolam
    public int[][] decor;   // hiasan acak (rumput, bunga, semak)
    public Duck duck;
    public int level;

    public final double[] fishX = new double[FISH_COUNT];
    public final double[] fishY = new double[FISH_COUNT];
    public final double[] fishDir = new double[FISH_COUNT];

    // ================= Alur level =================

    public void startNewGame() {
        loadLevel(0);
    }

    public void loadLevel(int lv) {
        level = lv;
        map = generateMap(rnd);          // kolam BARU setiap level / setiap game
        decor = generateDecor(rnd);
        placeDuck();
        spawnFish();
    }

    public void update(double dt) {
        duck.update(dt, this);
        updateFish(dt);
    }

    // ================= Peta & tabrakan =================

    public static boolean walk(int[][] m, int r, int c) {
        if (r < 0 || r >= ROWS || c < 0 || c >= COLS) return false;
        return m[r][c] == WATER || m[r][c] == LILY;
    }

    private boolean walkPx(double px, double py) {
        return walk(map, (int) Math.floor(py / TILE), (int) Math.floor(px / TILE));
    }

    /** Apakah kotak (x, y, w, h) berada seluruhnya di atas air? (w, h <= TILE) */
    public boolean canStand(double x, double y, int w, int h) {
        return walkPx(x, y) && walkPx(x + w - 1, y)
                && walkPx(x, y + h - 1) && walkPx(x + w - 1, y + h - 1);
    }

    /** Membuat kolam acak: beberapa lingkaran air, dihaluskan, lalu diberi batu dan teratai. */
    public static int[][] generateMap(Random rnd) {
        for (int attempt = 0; ; attempt++) {
            int[][] m = new int[ROWS][COLS];
            for (int[] row : m) Arrays.fill(row, LAND);

            carve(m, COLS / 2 + rnd.nextInt(5) - 2, ROWS / 2 + rnd.nextInt(3), 6 + rnd.nextInt(2), rnd);
            int extra = 4 + rnd.nextInt(3);
            for (int i = 0; i < extra; i++) {
                carve(m, COLS / 2 + rnd.nextInt(17) - 8, ROWS / 2 + rnd.nextInt(9) - 3, 3 + rnd.nextInt(4), rnd);
            }
            smooth(m);
            smooth(m);
            keepLargest(m);

            // batu
            int rocks = 5 + rnd.nextInt(4);
            for (int i = 0, placed = 0; i < 200 && placed < rocks; i++) {
                int r = 4 + rnd.nextInt(ROWS - 8);
                int c = 3 + rnd.nextInt(COLS - 6);
                if (m[r][c] != WATER) continue;
                boolean interior = true;
                for (int dr = -1; dr <= 1; dr++)
                    for (int dc = -1; dc <= 1; dc++)
                        if (m[r + dr][c + dc] == LAND) interior = false;
                if (interior) {
                    m[r][c] = ROCK;
                    placed++;
                }
            }
            keepLargest(m);   // pastikan seluruh air masih tersambung

            // daun teratai (hiasan, tetap bisa dilewati)
            int lilies = 8 + rnd.nextInt(5);
            for (int i = 0, placed = 0; i < 300 && placed < lilies; i++) {
                int r = rnd.nextInt(ROWS), c = rnd.nextInt(COLS);
                if (m[r][c] == WATER) {
                    m[r][c] = LILY;
                    placed++;
                }
            }

            int needed = attempt > 50 ? 80 : 160;
            if (countWalkable(m) >= needed) return m;
        }
    }

    private static void carve(int[][] m, int cx, int cy, int rad, Random rnd) {
        for (int r = cy - rad - 2; r <= cy + rad + 2; r++) {
            for (int c = cx - rad - 2; c <= cx + rad + 2; c++) {
                if (r < 3 || r > ROWS - 2 || c < 2 || c > COLS - 3) continue; // tepi tetap daratan
                double dx = c - cx, dy = (r - cy) * 1.3;
                double limit = rad + (rnd.nextDouble() - 0.5) * 1.8;
                if (Math.sqrt(dx * dx + dy * dy) < limit) m[r][c] = WATER;
            }
        }
    }

    private static void smooth(int[][] m) {
        int[][] n = new int[ROWS][];
        for (int r = 0; r < ROWS; r++) n[r] = m[r].clone();
        for (int r = 3; r <= ROWS - 2; r++) {
            for (int c = 2; c <= COLS - 3; c++) {
                int w = 0;
                if (m[r - 1][c] != LAND) w++;
                if (m[r + 1][c] != LAND) w++;
                if (m[r][c - 1] != LAND) w++;
                if (m[r][c + 1] != LAND) w++;
                if (m[r][c] == LAND && w >= 3) n[r][c] = WATER;
                else if (m[r][c] == WATER && w <= 1) n[r][c] = LAND;
            }
        }
        for (int r = 0; r < ROWS; r++) m[r] = n[r];
    }

    /** Hanya sisakan satu kolam terbesar; genangan terpisah diubah jadi daratan. */
    private static void keepLargest(int[][] m) {
        boolean[][] seen = new boolean[ROWS][COLS];
        ArrayList<int[]> best = null;
        int[] dr = { -1, 1, 0, 0 }, dc = { 0, 0, -1, 1 };
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (!walk(m, r, c) || seen[r][c]) continue;
                ArrayList<int[]> comp = new ArrayList<>();
                ArrayDeque<int[]> q = new ArrayDeque<>();
                q.add(new int[] { r, c });
                seen[r][c] = true;
                while (!q.isEmpty()) {
                    int[] p = q.poll();
                    comp.add(p);
                    for (int d = 0; d < 4; d++) {
                        int nr = p[0] + dr[d], nc = p[1] + dc[d];
                        if (walk(m, nr, nc) && !seen[nr][nc]) {
                            seen[nr][nc] = true;
                            q.add(new int[] { nr, nc });
                        }
                    }
                }
                if (best == null || comp.size() > best.size()) best = comp;
            }
        }
        boolean[][] keep = new boolean[ROWS][COLS];
        if (best != null) for (int[] p : best) keep[p[0]][p[1]] = true;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (walk(m, r, c) && !keep[r][c]) m[r][c] = LAND;
    }

    private static int countWalkable(int[][] m) {
        int n = 0;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (walk(m, r, c)) n++;
        return n;
    }

    public static int[][] generateDecor(Random rnd) {
        int[][] d = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                d[r][c] = rnd.nextInt(16);
        return d;
    }

    // ================= Penempatan objek =================

    private void placeDuck() {
        double sr = 0, sc = 0;
        int n = 0;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (walk(map, r, c)) { sr += r; sc += c; n++; }
        double cr = sr / n, cc = sc / n, best = Double.MAX_VALUE;
        int br = 0, bc = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (!walk(map, r, c)) continue;
                double d = (r - cr) * (r - cr) + (c - cc) * (c - cc);
                if (d < best) { best = d; br = r; bc = c; }
            }
        }
        duck = new Duck(bc * TILE + (TILE - Duck.W) / 2.0, br * TILE + (TILE - Duck.H) / 2.0);
    }

    private void spawnFish() {
        for (int i = 0; i < FISH_COUNT; i++) {
            for (int tries = 0; tries < 500; tries++) {
                int r = rnd.nextInt(ROWS), c = rnd.nextInt(COLS);
                if (!walk(map, r, c)) continue;
                double fx = c * TILE + 8, fy = r * TILE + 11;
                if (canStand(fx, fy, 16, 10)) {
                    fishX[i] = fx;
                    fishY[i] = fy;
                    fishDir[i] = rnd.nextBoolean() ? 1 : -1;
                    break;
                }
            }
        }
    }

    // ================= Pembaruan objek =================

    private void updateFish(double dt) {
        for (int i = 0; i < FISH_COUNT; i++) {
            double nx = fishX[i] + fishDir[i] * 28 * dt;
            if (canStand(nx, fishY[i], 16, 10)) fishX[i] = nx; else fishDir[i] = -fishDir[i];
        }
    }

}

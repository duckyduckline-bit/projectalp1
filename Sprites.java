import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Semua gambar pixel art dibuat dari array String (satu huruf = satu piksel).
 * Tidak butuh file gambar eksternal.
 */
public class Sprites {

    private static final HashMap<Character, Color> PALETTE = new HashMap<>();

    static {
        PALETTE.put('y', new Color(0xFFD93B)); // kuning
        PALETTE.put('Y', new Color(0xE0A800)); // kuning tua
        PALETTE.put('o', new Color(0xFF8C1A)); // oranye
        PALETTE.put('k', new Color(0x1A1A1A)); // hitam
        PALETTE.put('w', new Color(0xFFFFFF)); // putih
        PALETTE.put('r', new Color(0xD63B3B)); // merah
        PALETTE.put('g', new Color(0x4CAF50)); // hijau
        PALETTE.put('G', new Color(0x2E7D32)); // hijau tua
        PALETTE.put('b', new Color(0x4FA3E0)); // biru
        PALETTE.put('B', new Color(0x1F5FA8)); // biru tua
        PALETTE.put('c', new Color(0xA5EBFF)); // cyan muda
        PALETTE.put('s', new Color(0xC0C7D1)); // perak
        PALETTE.put('S', new Color(0x6B7280)); // abu-abu
        PALETTE.put('n', new Color(0x8B5A2B)); // coklat
        PALETTE.put('l', new Color(0xFFF59D)); // kuning muda
    }

    // Bebek menghadap KANAN (12 x 11). Kalau menghadap kiri, gambar dicerminkan.
    public static final String[] DUCK = {
        ".....yyyy...",
        "....yyyyyy..",
        "....yyykyyoo",
        "....yyyyyyoo",
        ".....yyyyy..",
        "yy.yyyyyyy..",
        "yyyYyyyyYy..",
        ".yYYyyyyYY..",
        "..YyyyyyyY..",
        "...yyyyyy...",
        "....o..o...."
    };

    public static final String[] FISH = {
        "..oooo..",
        "o.oooko.",
        "oooooooo",
        "o.oooo..",
        "...oo..."
    };

    public static final String[] HEART = {
        ".rr.rr.",
        "rrrrrrr",
        "rrrrrrr",
        ".rrrrr.",
        "..rrr..",
        "...r..."
    };

    // [kategori][variasi][baris]  kategori: 0 organik, 1 anorganik, 2 B3
    public static final String[][][] TRASH = {
        { // ORGANIK
            { ".....nn.", "....yyY.", "...yyyY.", "..yyyY..", ".yyyY...", "nyyY....", "nYY.....", "........" }, // kulit pisang
            { "....n...", "...gn...", "..rrrr..", ".rrwwrr.", ".rwwwwr.", "..wkkw..", "..rwwr..", "...rr..." }, // sisa apel
            { "........", "..w.w.w.", "..w.w.w.", "wwwwwwww", "..w.w.w.", "..w.w.w.", "........", "........" }  // tulang ikan
        },
        { // ANORGANIK
            { "...ww...", "...cc...", "..cccc..", "..cBBc..", "..cBBc..", "..cBBc..", "..cccc..", "...cc..." }, // botol plastik
            { "........", "..SSSS..", "..ssss..", "..srrs..", "..srrs..", "..ssss..", "..SSSS..", "........" }, // kaleng
            { "..w..w..", "..wwww..", ".wwwwww.", "wwwwwwww", "wSwwwwSw", ".wwSSww.", "..wwww..", "........" }  // kantong plastik
        },
        { // B3
            { "...ss...", "..gggg..", "..gGGg..", "..gyyg..", "..gyyg..", "..gGGg..", "..gggg..", "..SSSS.." }, // baterai
            { "..llll..", ".llwwll.", ".lwwwwl.", ".lwwwwl.", "..lwwl..", "...SS...", "...ss...", "...SS..." }, // lampu
            { "...kk...", "..kkkk..", "..rrrr..", "..rwwr..", "..rkkr..", "..rrrr..", "..rrrr..", "..SSSS.." }  // pestisida
        }
    };

    public static final BufferedImage DUCK_IMG = make(DUCK);
    public static final BufferedImage FISH_IMG = make(FISH);
    public static final BufferedImage HEART_IMG = make(HEART);
    public static final BufferedImage HEART_EMPTY_IMG = make(replaceAll(HEART, 'r', 'S'));
    public static final BufferedImage[][] TRASH_IMG = new BufferedImage[3][3];

    static {
        for (int c = 0; c < 3; c++) {
            for (int v = 0; v < 3; v++) {
                TRASH_IMG[c][v] = make(TRASH[c][v]);
            }
        }
    }

    private static String[] replaceAll(String[] rows, char from, char to) {
        String[] out = new String[rows.length];
        for (int i = 0; i < rows.length; i++) {
            out[i] = rows[i].replace(from, to);
        }
        return out;
    }

    public static BufferedImage make(String[] rows) {
        int h = rows.length;
        int w = rows[0].length();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = PALETTE.get(rows[y].charAt(x));
                if (c != null) {
                    img.setRGB(x, y, c.getRGB());
                }
            }
        }
        return img;
    }

    /** Menggambar sprite diperbesar (scale). flip = true untuk dicerminkan secara horizontal. */
    public static void draw(Graphics2D g, BufferedImage img, int x, int y, int scale, boolean flip) {
        int w = img.getWidth() * scale;
        int h = img.getHeight() * scale;
        if (flip) {
            g.drawImage(img, x, y, x + w, y + h, img.getWidth(), 0, 0, img.getHeight(), null);
        } else {
            g.drawImage(img, x, y, w, h, null);
        }
    }
}

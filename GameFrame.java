import javax.swing.JFrame;

/** Jendela utama permainan. */
public class GameFrame extends JFrame {
    public GameFrame() {
        super("Duck Pond Guardian");
        GamePanel panel = new GamePanel();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
        panel.start();
    }
}

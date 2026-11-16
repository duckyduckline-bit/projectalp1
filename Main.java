import javax.swing.SwingUtilities;

/** Titik masuk program. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}

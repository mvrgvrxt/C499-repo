import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Morph {
    public static void main(String[] args) {
        MorphWindow M = new MorphWindow();
        M.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}

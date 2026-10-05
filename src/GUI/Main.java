package GUI;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            UIManager.put("OptionPane.messageFont", Theme.font(Font.PLAIN, 14));
            UIManager.put("OptionPane.buttonFont", Theme.font(Font.PLAIN, 13));
            new LoginFrame().setVisible(true);
        });
    }
}

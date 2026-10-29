package GUI;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** Khung bo góc bao quanh một ô nhập: viền xanh khi đang nhập, viền đỏ khi có lỗi. */
final class FieldBox extends JPanel {
    private final JTextField field;
    private final Color bg;
    private boolean error;

    FieldBox(JTextField field, Color bg, int height) {
        super(new BorderLayout());
        this.field = field;
        this.bg = bg;
        setOpaque(false);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, 12, 0, 12));
        field.setFont(Theme.font(Font.PLAIN, 13));
        field.setForeground(Theme.TEXT);
        add(field, BorderLayout.CENTER);
        setPreferredSize(new Dimension(0, height));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
    }

    void setError(boolean error) {
        if (this.error != error) {
            this.error = error;
            repaint();
        }
    }

    JTextField getField() { return field; }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.setColor(error ? Theme.DANGER : field.hasFocus() ? Theme.PRIMARY : Theme.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        g2.dispose();
    }
}

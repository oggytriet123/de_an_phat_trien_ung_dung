package GUI;

import java.awt.*;
import javax.swing.JToggleButton;

/** Nút lọc / chọn dạng viên thuốc (bo tròn hoàn toàn) hoặc chip bo góc nhẹ. */
final class Pill extends JToggleButton {
    private final Color mauChon;
    private final Color nen;
    private final int bo;   // < 0: bo tròn hoàn toàn

    Pill(String text) {
        this(text, Theme.PRIMARY, Theme.FIELD_BG, -1);
    }

    Pill(String text, Color mauChon, Color nen, int bo) {
        super(text);
        this.mauChon = mauChon;
        this.nen = nen;
        this.bo = bo;
        setFont(Theme.font(Font.PLAIN, 12));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        FontMetrics fm = getFontMetrics(getFont());
        setPreferredSize(new Dimension(fm.stringWidth(text) + 30, 32));
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        int arc = bo < 0 ? h : bo;
        if (isSelected()) {
            g2.setColor(mauChon);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
        } else {
            g2.setColor(nen);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        }
        g2.setFont(getFont());
        g2.setColor(isSelected() ? Color.WHITE : Theme.TEXT);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}

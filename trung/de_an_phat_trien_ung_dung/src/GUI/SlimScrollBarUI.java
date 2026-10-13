package GUI;

import java.awt.*;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Thanh cuộn mảnh, bo tròn, không có nút mũi tên. */
public class SlimScrollBarUI extends BasicScrollBarUI {
    private static final int SIZE = 8;

    /** Áp dụng cho thanh cuộn dọc của một JScrollPane. */
    public static void apply(javax.swing.JScrollPane sp) {
        JScrollBar bar = sp.getVerticalScrollBar();
        bar.setUI(new SlimScrollBarUI());
        bar.setPreferredSize(new Dimension(SIZE + 6, 0));
        bar.setOpaque(false);
        bar.setUnitIncrement(24);
    }

    @Override protected void configureScrollBarColors() {
        thumbColor = new Color(0xC3CDD4);
        trackColor = new Color(0, 0, 0, 0);
    }

    @Override protected JButton createDecreaseButton(int o) { return zero(); }
    @Override protected JButton createIncreaseButton(int o) { return zero(); }

    private static JButton zero() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }

    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isThumbRollover() || isDragging ? new Color(0x9FAEB9) : thumbColor);
        int x = r.x + (r.width - SIZE) / 2;
        g2.fillRoundRect(x, r.y + 2, SIZE, r.height - 4, SIZE, SIZE);
        g2.dispose();
    }
}

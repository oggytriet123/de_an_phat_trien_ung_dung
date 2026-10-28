package GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Popup phủ lên cả cửa sổ: nền tối mờ, thẻ trắng ở giữa, trang phía sau vẫn hiển thị.
 * Dùng glass pane của cửa sổ nên chặn mọi thao tác chuột vào trang bên dưới.
 */
final class Overlay extends JComponent {
    private static final String KEY_OLD_GLASS = "overlay.oldGlass";

    private Overlay(JComponent card, Runnable onEsc) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        add(card, new GridBagConstraints());

        MouseAdapter chan = new MouseAdapter() { };
        addMouseListener(chan);
        addMouseMotionListener(chan);
        addMouseWheelListener(chan);

        setFocusCycleRoot(true);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "dong");
        getActionMap().put("dong", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (onEsc != null) onEsc.run();
            }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        g.setColor(new Color(18, 32, 42, 130));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /** Hiện thẻ popup. onEsc chạy khi nhấn phím Esc (thường là nút đóng / bỏ qua / không). */
    static void hien(Component host, JComponent card, Runnable onEsc) {
        JRootPane rp = SwingUtilities.getRootPane(host);
        if (rp == null) return;
        if (!(rp.getGlassPane() instanceof Overlay)) rp.putClientProperty(KEY_OLD_GLASS, rp.getGlassPane());
        Overlay ov = new Overlay(card, onEsc);
        rp.setGlassPane(ov);
        ov.setVisible(true);
        ov.revalidate();
        ov.repaint();
    }

    static void dong(Component host) {
        JRootPane rp = SwingUtilities.getRootPane(host);
        if (rp == null || !(rp.getGlassPane() instanceof Overlay)) return;
        rp.getGlassPane().setVisible(false);
        Object old = rp.getClientProperty(KEY_OLD_GLASS);
        if (old instanceof Component c) {
            c.setVisible(false);
            rp.setGlassPane(c);
        }
        rp.repaint();
    }
}

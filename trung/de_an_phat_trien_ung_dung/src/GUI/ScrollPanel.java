package GUI;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/** Panel đặt trong JScrollPane: luôn co giãn theo chiều rộng khung nhìn, chỉ cuộn dọc. */
public class ScrollPanel extends JPanel implements Scrollable {
    public ScrollPanel(LayoutManager lm) {
        super(lm);
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 24; }
    @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
        return o == SwingConstants.VERTICAL ? r.height : r.width;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}

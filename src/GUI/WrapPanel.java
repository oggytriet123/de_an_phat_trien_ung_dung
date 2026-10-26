package GUI;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;

/** Xếp các thành phần từ trái sang phải và tự xuống dòng khi hết chỗ (chiều cao tăng theo số dòng). */
final class WrapPanel extends JPanel {
    private final int hgap, vgap, rongGoiY;
    private int rongCu = -1;

    /** @param rongGoiY bề rộng tạm dùng để ước lượng chiều cao khi panel chưa được xếp vị trí */
    WrapPanel(int hgap, int vgap, int rongGoiY) {
        super(null);
        this.hgap = hgap;
        this.vgap = vgap;
        this.rongGoiY = rongGoiY;
        setOpaque(false);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (getWidth() != rongCu) {   // đổi bề rộng thì số dòng có thể đổi: tính lại chiều cao
                    rongCu = getWidth();
                    revalidate();
                }
            }
        });
    }

    /** Xếp vị trí (nếu dat = true) và trả về tổng chiều cao cần có với bề rộng cho trước. */
    private int xep(int rong, boolean dat) {
        int x = 0, y = 0, caoHang = 0;
        for (Component c : getComponents()) {
            if (!c.isVisible()) continue;
            Dimension d = c.getPreferredSize();
            if (x > 0 && x + d.width > rong) {
                x = 0;
                y += caoHang + vgap;
                caoHang = 0;
            }
            if (dat) c.setBounds(x, y, d.width, d.height);
            x += d.width + hgap;
            caoHang = Math.max(caoHang, d.height);
        }
        return y + caoHang;
    }

    @Override public Dimension getPreferredSize() {
        int w = getWidth() > 0 ? getWidth() : rongGoiY;
        return new Dimension(w, xep(w, false));
    }

    @Override public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override public void doLayout() {
        xep(Math.max(getWidth(), 1), true);
    }
}

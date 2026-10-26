package GUI;

import java.awt.Font;
import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Menu xổ xuống của một mục trên thanh điều hướng (ví dụ "Xử Lý"). */
final class NavMenu {
    private NavMenu() {}

    static JPopupMenu tao(String[] muc, Consumer<String> onChon) {
        JPopupMenu m = new JPopupMenu();
        m.setBorder(new LineBorder(Theme.BORDER));
        for (String s : muc) {
            JMenuItem it = new JMenuItem(s);
            it.setFont(Theme.font(Font.PLAIN, 13));
            it.setBorder(new EmptyBorder(9, 18, 9, 32));
            it.addActionListener(e -> onChon.accept(s));
            m.add(it);
        }
        return m;
    }
}

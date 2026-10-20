package GUI;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * Biểu đồ tròn dạng Donut (Donut / Pie Chart) vẽ thuần Java2D vector.
 * Vòng Donut hiện đại, hiển thị tổng ở tâm và bảng chú giải % chi tiết bên cạnh.
 */
public class DonutChartComponent extends JComponent {
    private String chartTitle = "";
    private final List<Slice> slices = new ArrayList<>();
    private String centerText = "";
    private String centerSubtext = "";

    public static class Slice {
        public final String name;
        public final double value;
        public final Color color;

        public Slice(String name, double value, Color color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }

    public DonutChartComponent(String title) {
        this.chartTitle = title;
        setBackground(Color.WHITE);
        setOpaque(true);
        setPreferredSize(new Dimension(360, 240));
        setMinimumSize(new Dimension(260, 180));
    }

    public void clearData() {
        slices.clear();
        centerText = "";
        centerSubtext = "";
        repaint();
    }

    public void addSlice(String name, double value, Color color) {
        if (value > 0) {
            slices.add(new Slice(name, value, color));
        }
        repaint();
    }

    public void setCenterText(String text, String subtext) {
        this.centerText = text;
        this.centerSubtext = subtext;
        repaint();
    }

    public void setTitle(String t) {
        this.chartTitle = t;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g.setColor(getBackground());
        g.fillRect(0, 0, w, h);

        // Tiêu đề biểu đồ
        int titleHeight = 28;
        if (chartTitle != null && !chartTitle.isEmpty()) {
            g.setColor(Theme.TEXT);
            g.setFont(Theme.font(Font.BOLD, 14));
            g.drawString(chartTitle, 16, 22);
        }

        if (slices.isEmpty()) {
            g.setColor(Theme.MUTED);
            g.setFont(Theme.font(Font.PLAIN, 13));
            String msg = "Không có dữ liệu";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g.dispose();
            return;
        }

        double total = 0;
        for (Slice s : slices) total += s.value;
        if (total <= 0) total = 1;

        // Vùng vẽ donut bên trái và chú giải bên phải
        int padTop = titleHeight + 10;
        int availH = h - padTop - 16;
        int donutSize = Math.min(availH, Math.min(180, (int) (w * 0.44)));
        if (donutSize < 60) donutSize = 60;

        int donutX = 20;
        int donutY = padTop + (availH - donutSize) / 2;

        // Vẽ các lát cắt Donut
        double curAngle = 90.0; // Bắt đầu từ 12h
        for (Slice s : slices) {
            double extent = (s.value / total) * 360.0;
            g.setColor(s.color);
            Arc2D.Double outer = new Arc2D.Double(donutX, donutY, donutSize, donutSize, curAngle, -extent, Arc2D.PIE);
            g.fill(outer);
            curAngle -= extent;
        }

        // Khoét lỗ tròn ở tâm để tạo hình Donut
        int innerHole = (int) (donutSize * 0.58);
        int holeX = donutX + (donutSize - innerHole) / 2;
        int holeY = donutY + (donutSize - innerHole) / 2;
        g.setColor(Color.WHITE);
        g.fillOval(holeX, holeY, innerHole, innerHole);

        // Chữ ở tâm Donut (nếu có)
        if (centerText != null && !centerText.isEmpty()) {
            g.setColor(Theme.TEXT);
            g.setFont(Theme.font(Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            int cx = holeX + (innerHole - fm.stringWidth(centerText)) / 2;
            int cy = holeY + innerHole / 2 + (centerSubtext.isEmpty() ? 5 : 0);
            g.drawString(centerText, cx, cy);

            if (centerSubtext != null && !centerSubtext.isEmpty()) {
                g.setColor(Theme.MUTED);
                g.setFont(Theme.font(Font.PLAIN, 10));
                FontMetrics fsm = g.getFontMetrics();
                int scx = holeX + (innerHole - fsm.stringWidth(centerSubtext)) / 2;
                g.drawString(centerSubtext, scx, cy + 14);
            }
        }

        // Danh sách Chú giải (Legend) bên phải
        int legendLeft = donutX + donutSize + 20;
        int legendW = w - legendLeft - 16;
        int itemH = Math.max(22, availH / Math.max(1, slices.size()));
        int ly = padTop + Math.max(0, (availH - (slices.size() * itemH)) / 2);

        for (Slice s : slices) {
            // Chấm màu
            g.setColor(s.color);
            g.fillRoundRect(legendLeft, ly + (itemH - 10) / 2, 10, 10, 3, 3);

            // Tên danh mục
            g.setColor(Theme.TEXT);
            g.setFont(Theme.font(Font.PLAIN, 12));
            g.drawString(s.name, legendLeft + 16, ly + itemH / 2 + 4);

            // Số lượng & % bên phải
            double pct = (s.value / total) * 100.0;
            String valStr = String.format("%s (%.1f%%)", formatVal(s.value), pct);
            g.setColor(Theme.MUTED);
            g.setFont(Theme.font(Font.PLAIN, 11));
            FontMetrics fmVal = g.getFontMetrics();
            int vx = legendLeft + legendW - fmVal.stringWidth(valStr);
            if (vx > legendLeft + 16) {
                g.drawString(valStr, vx, ly + itemH / 2 + 4);
            }

            ly += itemH;
        }

        g.dispose();
    }

    private static String formatVal(double v) {
        if (v == (long) v) return String.format("%d", (long) v);
        return String.format("%.1f", v);
    }
}

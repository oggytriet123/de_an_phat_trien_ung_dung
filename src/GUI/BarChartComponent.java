package GUI;

import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import javax.swing.JComponent;

/**
 * Biểu đồ cột (Bar Chart) vẽ thuần bằng Java2D vector, hỗ trợ cột đơn hoặc cột đôi (Grouped Bars).
 * Tự động co giãn theo kích thước, vẽ lưới toạ độ, số liệu trục Y, nhãn trục X và chú giải.
 */
public class BarChartComponent extends JComponent {
    private String chartTitle = "";
    private final List<String> categories = new ArrayList<>();
    private final List<Series> seriesList = new ArrayList<>();

    public static class Series {
        public final String name;
        public final Color color;
        public final List<Double> values = new ArrayList<>();

        public Series(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    }

    public BarChartComponent(String title) {
        this.chartTitle = title;
        setBackground(Color.WHITE);
        setOpaque(true);
        setPreferredSize(new Dimension(500, 240));
        setMinimumSize(new Dimension(300, 180));
    }

    public void clearData() {
        categories.clear();
        seriesList.clear();
        repaint();
    }

    public void setCategories(List<String> cats) {
        this.categories.clear();
        this.categories.addAll(cats);
    }

    public Series addSeries(String name, Color color, List<Double> data) {
        Series s = new Series(name, color);
        if (data != null) s.values.addAll(data);
        seriesList.add(s);
        repaint();
        return s;
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

        // Nền thẻ biểu đồ bo góc nhẹ nếu cần
        g.setColor(getBackground());
        g.fillRect(0, 0, w, h);

        // Tiêu đề biểu đồ
        int titleHeight = 28;
        if (chartTitle != null && !chartTitle.isEmpty()) {
            g.setColor(Theme.TEXT);
            g.setFont(Theme.font(Font.BOLD, 14));
            g.drawString(chartTitle, 16, 22);
        }

        // Vẽ chú giải (Legend) ở góc trên bên phải
        int legendX = w - 16;
        g.setFont(Theme.font(Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        for (int i = seriesList.size() - 1; i >= 0; i--) {
            Series s = seriesList.get(i);
            int textW = fm.stringWidth(s.name);
            int itemW = textW + 24;
            legendX -= itemW;

            // Chấm màu
            g.setColor(s.color);
            g.fillRoundRect(legendX, 10, 12, 12, 4, 4);

            // Chữ
            g.setColor(Theme.MUTED);
            g.drawString(s.name, legendX + 18, 20);
        }

        if (categories.isEmpty() || seriesList.isEmpty()) {
            g.setColor(Theme.MUTED);
            g.setFont(Theme.font(Font.PLAIN, 13));
            String msg = "Không có dữ liệu trong khoảng thời gian này";
            int mw = fm.stringWidth(msg);
            g.drawString(msg, (w - mw) / 2, h / 2);
            g.dispose();
            return;
        }

        // Xác định giá trị cực đại để chia tỉ lệ trục Y
        double maxVal = 0;
        for (Series s : seriesList) {
            for (Double v : s.values) {
                if (v != null && v > maxVal) maxVal = v;
            }
        }
        if (maxVal <= 0) maxVal = 10;
        // Làm tròn mốc trục Y cho đẹp
        maxVal = roundNice(maxVal);

        // Vùng vẽ đồ thị
        int padLeft = 65;
        int padRight = 20;
        int padTop = titleHeight + 14;
        int padBottom = 34;

        int plotW = Math.max(10, w - padLeft - padRight);
        int plotH = Math.max(10, h - padTop - padBottom);

        // Vẽ 4 vạch lưới ngang trục Y (0%, 25%, 50%, 75%, 100%)
        int steps = 4;
        g.setFont(Theme.font(Font.PLAIN, 10));
        FontMetrics fsm = g.getFontMetrics();

        for (int i = 0; i <= steps; i++) {
            double v = maxVal * i / steps;
            int y = padTop + plotH - (int) (plotH * i / (double) steps);

            // Đường lưới ngang
            g.setColor(Theme.ROW_LINE);
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 3}, 0));
            g.drawLine(padLeft, y, padLeft + plotW, y);

            // Nhãn số trục Y
            g.setColor(Theme.MUTED);
            String label = formatShortVal(v);
            g.drawString(label, padLeft - fsm.stringWidth(label) - 8, y + 4);
        }

        // Trục hoành X
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(Theme.BORDER);
        g.drawLine(padLeft, padTop + plotH, padLeft + plotW, padTop + plotH);

        // Vẽ các cột
        int numCats = categories.size();
        int numSeries = seriesList.size();
        double catWidth = plotW / (double) numCats;

        for (int c = 0; c < numCats; c++) {
            double cx = padLeft + c * catWidth;
            double availWidth = catWidth * 0.72; // Khoảng trống cho các cột trong nhóm
            double barWidth = Math.min(28, Math.max(6, availWidth / numSeries));
            double groupWidth = barWidth * numSeries;
            double startX = cx + (catWidth - groupWidth) / 2.0;

            for (int s = 0; s < numSeries; s++) {
                Series series = seriesList.get(s);
                Double val = c < series.values.size() ? series.values.get(c) : 0.0;
                if (val == null) val = 0.0;

                double barH = (val / maxVal) * plotH;
                double bx = startX + s * barWidth;
                double by = padTop + plotH - barH;

                if (barH > 0) {
                    g.setColor(series.color);
                    // Bo góc trên đầu cột
                    RoundRectangle2D bar = new RoundRectangle2D.Double(bx + 1, by, barWidth - 2, barH, 5, 5);
                    g.fill(bar);
                    // Nếu cột cao, che phần bo góc dưới cùng để đáy phẳng sát trục
                    if (barH > 4) {
                        g.fillRect((int) (bx + 1), (int) (padTop + plotH - 3), (int) (barWidth - 2), 3);
                    }
                }
            }

            // Nhãn danh mục / ngày trên trục X
            g.setColor(Theme.TEXT);
            g.setFont(Theme.font(Font.PLAIN, 11));
            String catName = categories.get(c);
            int catW = g.getFontMetrics().stringWidth(catName);
            int catX = (int) (cx + (catWidth - catW) / 2);
            g.drawString(catName, catX, padTop + plotH + 18);
        }

        g.dispose();
    }

    private static double roundNice(double val) {
        if (val <= 0) return 10;
        double exp = Math.floor(Math.log10(val));
        double frac = val / Math.pow(10, exp);
        double nice;
        if (frac <= 1.0) nice = 1.0;
        else if (frac <= 2.0) nice = 2.0;
        else if (frac <= 5.0) nice = 5.0;
        else nice = 10.0;
        return nice * Math.pow(10, exp);
    }

    private static String formatShortVal(double val) {
        if (val >= 1_000_000_000) {
            return String.format("%.1f tỷ", val / 1_000_000_000.0);
        }
        if (val >= 1_000_000) {
            if (val % 1_000_000 == 0) return String.format("%.0f tr", val / 1_000_000.0);
            return String.format("%.1f tr", val / 1_000_000.0);
        }
        if (val >= 1_000) {
            if (val % 1_000 == 0) return String.format("%.0f k", val / 1_000.0);
            return String.format("%.1f k", val / 1_000.0);
        }
        return String.format("%.0f", val);
    }
}

package GUI;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

/** Icon vector vẽ bằng Java2D (hệ toạ độ 24x24) nên không cần file ảnh. */
public final class Icons {
    private Icons() {}

    public static Icon of(String name, int size, Color color) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                draw((Graphics2D) g, name, x, y, size, color, false);
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    /** Màu mặc định của từng icon khi dùng trong menu (đa sắc như thiết kế mẫu). */
    public static Color colorOf(String name) {
        if (name == null) return new Color(0x546E7A);
        switch (name) {
            case "settings": return new Color(0x546E7A);
            case "list": return new Color(0x2196F3);
            case "sync": return new Color(0x9C27B0);
            case "search": return new Color(0x1E88E5);
            case "bar-chart": return new Color(0x00ACC1);
            case "home": return new Color(0xE53935);
            case "person": return new Color(0x1F3A5F);
            case "help": return new Color(0x1E88E5);
            case "logout": return new Color(0x3F6FB5);
            case "close": return new Color(0xE53935);
            case "invoice": return new Color(0x37474F);
            case "customers": return new Color(0x2E86C1);
            case "service": return new Color(0xE57373);
            case "tag": return new Color(0xE53935);
            case "paw":
            case "logo": return new Color(0x8D6E63);
            case "cage":
            case "calendar": return new Color(0xF5A623);
            case "pie": return new Color(0x26A69A);
            case "check": return new Color(0x2E9E4F);
            case "x": return new Color(0xE53935);
            default: return new Color(0x546E7A);
        }
    }

    /** Icon màu mặc định. */
    public static Icon colored(String name, int size) {
        return of(name, size, colorOf(name));
    }

    public static void draw(Graphics2D g0, String name, double x, double y, double size, Color color, boolean filled) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        g.scale(size / 24.0, size / 24.0);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (name) {
            case "dashboard" -> {
                g.draw(new RoundRectangle2D.Double(3.5, 3.5, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(13.5, 3.5, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(3.5, 13.5, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(13.5, 13.5, 7, 7, 2, 2));
            }
            case "calendar" -> {
                g.draw(new RoundRectangle2D.Double(4, 5, 16, 15, 3, 3));
                g.draw(new Line2D.Double(4, 10, 20, 10));
                g.draw(new Line2D.Double(8.5, 3, 8.5, 7));
                g.draw(new Line2D.Double(15.5, 3, 15.5, 7));
            }
            case "paw" -> {
                Shape[] s = {
                    new Ellipse2D.Double(6.5, 12.5, 11, 8),
                    new Ellipse2D.Double(3, 8.7, 3.6, 4.6),
                    new Ellipse2D.Double(7.2, 4.2, 3.6, 4.6),
                    new Ellipse2D.Double(13.2, 4.2, 3.6, 4.6),
                    new Ellipse2D.Double(17.4, 8.7, 3.6, 4.6)
                };
                for (Shape sh : s) { if (filled) g.fill(sh); else g.draw(sh); }
            }
            case "customers" -> {
                g.draw(new Ellipse2D.Double(5.5, 4.5, 7, 7));
                Path2D p = new Path2D.Double();
                p.moveTo(3, 20); p.curveTo(3, 15, 6, 14, 9, 14); p.curveTo(12, 14, 15, 15, 15, 20);
                g.draw(p);
                g.draw(new Arc2D.Double(14.5, 5, 5, 6, 60, 240, Arc2D.OPEN));
                Path2D q = new Path2D.Double();
                q.moveTo(17.5, 14); q.curveTo(20, 14, 21, 16, 21, 19);
                g.draw(q);
            }
            case "person" -> {
                g.draw(new Ellipse2D.Double(8, 4, 8, 8));
                Path2D p = new Path2D.Double();
                p.moveTo(4, 21); p.curveTo(4, 15.5, 8, 14.5, 12, 14.5); p.curveTo(16, 14.5, 20, 15.5, 20, 21);
                g.draw(p);
            }
            case "box" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 3); p.lineTo(20, 7.5); p.lineTo(20, 16.5); p.lineTo(12, 21);
                p.lineTo(4, 16.5); p.lineTo(4, 7.5); p.closePath();
                g.draw(p);
                Path2D q = new Path2D.Double();
                q.moveTo(4, 7.5); q.lineTo(12, 12); q.lineTo(20, 7.5);
                g.draw(q);
                g.draw(new Line2D.Double(12, 12, 12, 21));
            }
            case "card" -> {
                g.draw(new RoundRectangle2D.Double(3, 5.5, 18, 13, 3, 3));
                g.draw(new Line2D.Double(3, 10, 21, 10));
                g.draw(new Line2D.Double(6.5, 14.5, 10.5, 14.5));
            }
            case "search" -> {
                g.draw(new Ellipse2D.Double(4.5, 4.5, 11, 11));
                g.draw(new Line2D.Double(14, 14, 19.5, 19.5));
            }
            case "wrench" -> {
                g.draw(new Line2D.Double(6, 18, 14, 10));
                g.draw(new Ellipse2D.Double(11, 3.5, 7, 7));
            }
            case "exit" -> {
                g.draw(new RoundRectangle2D.Double(9, 5, 10, 14, 3, 3));
                g.draw(new Line2D.Double(12, 12, 4, 12));
                g.draw(new Line2D.Double(4, 12, 7, 9));
                g.draw(new Line2D.Double(4, 12, 7, 15));
            }
            case "lock" -> {
                g.draw(new RoundRectangle2D.Double(6, 11, 12, 9, 2, 2));
                g.draw(new Arc2D.Double(8, 5, 8, 10, 0, 180, Arc2D.OPEN));
                g.draw(new Line2D.Double(12, 15, 12, 17));
            }
            case "sync" -> {
                g.draw(new Arc2D.Double(5, 5, 14, 14, 45, 270, Arc2D.OPEN));
                g.draw(new Line2D.Double(18, 12, 21, 9));
                g.draw(new Line2D.Double(18, 12, 15, 9));
            }
            case "bar-chart" -> {
                g.draw(new Line2D.Double(4, 20, 20, 20));
                g.draw(new RoundRectangle2D.Double(6, 12, 3, 8, 1, 1));
                g.draw(new RoundRectangle2D.Double(11, 6, 3, 14, 1, 1));
                g.draw(new RoundRectangle2D.Double(16, 10, 3, 10, 1, 1));
            }
            case "logo" -> {
                Shape[] s = {
                    new Ellipse2D.Double(8, 11, 8, 8),
                    new Ellipse2D.Double(3.5, 7.5, 4.5, 4.5),
                    new Ellipse2D.Double(9.75, 4, 4.5, 4.5),
                    new Ellipse2D.Double(16, 7.5, 4.5, 4.5)
                };
                for (Shape sh : s) { if (filled) g.fill(sh); else g.draw(sh); }
            }
            case "settings" -> {
                g.draw(new Ellipse2D.Double(7, 7, 10, 10));
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4.0;
                    double c = Math.cos(a), s = Math.sin(a);
                    g.draw(new Line2D.Double(12 + 5 * c, 12 + 5 * s, 12 + 7 * c, 12 + 7 * s));
                }
            }
            case "list" -> {
                for (int i = 0; i < 4; i++) {
                    double yy = 5 + i * 5;
                    g.draw(new Line2D.Double(3.5, yy, 5, yy));
                    g.draw(new Line2D.Double(8, yy, 20.5, yy));
                }
            }
            case "home" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 11.5); p.lineTo(12, 3.5); p.lineTo(21, 11.5);
                g.draw(p);
                Path2D q = new Path2D.Double();
                q.moveTo(5.5, 10); q.lineTo(5.5, 20.5); q.lineTo(18.5, 20.5); q.lineTo(18.5, 10);
                g.draw(q);
                g.draw(new Rectangle2D.Double(10, 14, 4, 6.5));
            }
            case "help" -> {
                g.fill(new Ellipse2D.Double(2.5, 2.5, 19, 19));
                g.setColor(Color.WHITE);
                g.setFont(new Font(Theme.FONT, Font.BOLD, 16));
                g.drawString("?", 8.3f, 17.5f);
            }
            case "logout" -> {
                g.fill(new Ellipse2D.Double(2.5, 2.5, 19, 19));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(17, 12, 7, 12));
                g.draw(new Line2D.Double(7, 12, 11, 8));
                g.draw(new Line2D.Double(7, 12, 11, 16));
            }
            case "close" -> {
                g.fill(new RoundRectangle2D.Double(2.5, 2.5, 19, 19, 5, 5));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(8, 8, 16, 16));
                g.draw(new Line2D.Double(16, 8, 8, 16));
            }
            case "invoice" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(5, 3); p.lineTo(19, 3); p.lineTo(19, 21); p.lineTo(16.5, 19); p.lineTo(14, 21);
                p.lineTo(11.5, 19); p.lineTo(9, 21); p.lineTo(7, 19); p.lineTo(5, 21); p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(8.5, 8, 15.5, 8));
                g.draw(new Line2D.Double(8.5, 12, 15.5, 12));
            }
            case "service" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 20.5); p.curveTo(4, 15, 2.5, 11, 2.5, 8.5);
                p.curveTo(2.5, 5.5, 5, 4, 7.2, 4); p.curveTo(9.5, 4, 11, 5.5, 12, 7);
                p.curveTo(13, 5.5, 14.5, 4, 16.8, 4); p.curveTo(19, 4, 21.5, 5.5, 21.5, 8.5);
                p.curveTo(21.5, 11, 20, 15, 12, 20.5);
                g.draw(p);
            }
            case "tag" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 12); p.lineTo(12, 3); p.lineTo(21, 3); p.lineTo(21, 12); p.lineTo(12, 21); p.closePath();
                g.draw(p);
                g.fill(new Ellipse2D.Double(15, 6, 3, 3));
            }
            case "cage" -> {
                g.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15, 3, 3));
                for (int i = 1; i <= 3; i++) g.draw(new Line2D.Double(3.5 + i * 4.25, 5, 3.5 + i * 4.25, 20));
                g.draw(new Line2D.Double(7, 5, 12, 2.5));
                g.draw(new Line2D.Double(17, 5, 12, 2.5));
            }
            case "pie" -> {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
                g.draw(new Line2D.Double(12, 12, 12, 3.5));
                g.draw(new Line2D.Double(12, 12, 19, 16));
            }
            case "eye" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(2, 12);
                p.curveTo(6, 6.5, 18, 6.5, 22, 12);
                p.curveTo(18, 17.5, 6, 17.5, 2, 12);
                p.closePath();
                g.draw(p);
                g.draw(new Ellipse2D.Double(9.5, 9.5, 5, 5));
                g.fill(new Ellipse2D.Double(11, 11, 2, 2));
            }
            case "edit" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(4, 20);
                p.lineTo(8.5, 19.5);
                p.lineTo(19, 9);
                p.lineTo(15, 5);
                p.lineTo(4.5, 15.5);
                p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(13.5, 6.5, 17.5, 10.5));
            }
            case "chevron-down" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(6.5, 9.5);
                p.lineTo(12, 15);
                p.lineTo(17.5, 9.5);
                g.draw(p);
            }
            case "plus" -> {
                g.draw(new Line2D.Double(12, 6, 12, 18));
                g.draw(new Line2D.Double(6, 12, 18, 12));
            }
            case "excel" -> {
                g.draw(new RoundRectangle2D.Double(4, 3.5, 16, 17, 2.5, 2.5));
                g.draw(new Line2D.Double(4, 9, 20, 9));
                g.draw(new Line2D.Double(10, 9, 10, 20.5));
                g.draw(new Line2D.Double(4, 15, 20, 15));
            }
            case "check" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(5, 12.5);
                p.lineTo(10, 17.5);
                p.lineTo(19, 7);
                g.draw(p);
            }
            case "trash" -> {
                g.draw(new Line2D.Double(4, 7, 20, 7));
                g.draw(new Line2D.Double(9, 7, 9, 4.5));
                g.draw(new Line2D.Double(9, 4.5, 15, 4.5));
                g.draw(new Line2D.Double(15, 4.5, 15, 7));
                Path2D p = new Path2D.Double();
                p.moveTo(6, 7);
                p.lineTo(7, 20);
                p.lineTo(17, 20);
                p.lineTo(18, 7);
                g.draw(p);
                g.draw(new Line2D.Double(10, 11, 10, 16));
                g.draw(new Line2D.Double(14, 11, 14, 16));
            }
            case "x" -> {
                g.draw(new Line2D.Double(6.5, 6.5, 17.5, 17.5));
                g.draw(new Line2D.Double(17.5, 6.5, 6.5, 17.5));
            }
            default -> { }


        }
        g.dispose();
    }

    /** Icon cửa sổ (ô vuông bo góc màu accent + chân thú trắng). */
    public static Image appIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Theme.SIDEBAR);
        g.fillRoundRect(0, 0, 64, 64, 16, 16);
        g.dispose();
        Graphics2D g2 = img.createGraphics();
        draw(g2, "paw", 12, 12, 40, Color.WHITE, true);
        g2.dispose();
        return img;
    }
}

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

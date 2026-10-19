package GUI;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Màu sắc, font và định dạng dùng chung cho toàn bộ giao diện. */
public final class Theme {
    private Theme() {}

    public static final Color SIDEBAR        = new Color(0x5B7B8C);
    public static final Color SIDEBAR_HOVER  = new Color(255, 255, 255, 30);
    public static final Color SIDEBAR_ACTIVE = new Color(255, 255, 255, 60);
    public static final Color ACCENT         = new Color(0x56788A);
    public static final Color ACCENT_HOVER   = new Color(0x466778);
    public static final Color DARK           = new Color(0x1E3442);
    public static final Color BG             = new Color(0xF3F6F8);
    public static final Color CARD           = Color.WHITE;
    public static final Color BORDER         = new Color(0xDDE3E8);
    public static final Color ROW_LINE       = new Color(0xE9EDF0);
    public static final Color TEXT           = new Color(0x1F2A33);
    public static final Color MUTED          = new Color(0x6B7A86);
    public static final Color GREEN          = new Color(0x2E9E4F);
    public static final Color RED            = new Color(0xD64545);

    public static final String FONT = "Segoe UI";

    public static Font font(int style, int size) {
        return new Font(FONT, style, size);
    }

    private static final DecimalFormat NUM;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.ROOT);
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        NUM = new DecimalFormat("#,##0", s);
    }

    public static synchronized String number(long v) { return NUM.format(v); }
    public static String money(long v) { return number(v) + "₫"; }
}

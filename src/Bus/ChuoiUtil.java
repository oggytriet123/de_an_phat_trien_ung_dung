package Bus;

import java.text.Normalizer;
import java.util.Locale;

/** Tiện ích xử lý chuỗi dùng chung cho tìm kiếm. */
public final class ChuoiUtil {
    private ChuoiUtil() {}

    /** Bỏ dấu tiếng Việt + chữ thường để tìm kiếm không phân biệt dấu. */
    public static String chuanHoa(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    public static String chiSo(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}

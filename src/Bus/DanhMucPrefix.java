package Bus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Enum định nghĩa các tiền tố mã phân loại danh mục chuẩn cho Pet Shop.
 * Hỗ trợ tra cứu nhanh theo tên danh mục (có dấu, không dấu, viết hoa/thường).
 */
public enum DanhMucPrefix {
    // Danh mục sản phẩm phổ biến
    THUC_AN("Thức ăn", "FOOD", false),
    DO_CHOI("Đồ chơi", "TOY", false),
    THUOC("Thuốc", "MED", false),
    CHAM_SOC_VE_SINH("Chăm sóc và vệ sinh", "CARE", false),
    PHU_KIEN("Phụ kiện", "ACC", false),
    DO_DUNG_SINH_HOAT("Đồ dùng và sinh hoạt", "LIV", false),
    MY_PHAM("Mỹ phẩm", "MP", false),
    CHUONG_LONG("Chuồng lồng", "CL", false),
    VE_SINH("Vệ sinh", "VS", false),
    CAT_VE_SINH("Cát vệ sinh", "CAT", false),
    QUAN_AO("Quần áo", "QA", false),
    DINH_DUONG("Dinh dưỡng", "DD", false),

    // Tiền tố gốc cho dịch vụ
    DICH_VU("Dịch vụ", "DV", true),

    // Các phân nhóm dịch vụ chuyên biệt trong Pet Shop (tiền tố DV-...)
    SPA("Spa", "DV-SPA", true),
    KHAM_BENH("Khám bệnh", "DV-KB", true),
    TIEM_PHONG("Tiêm phòng", "DV-TP", true),
    PHAU_THUAT("Phẫu thuật", "DV-PT", true),
    KHACH_SAN("Khách sạn thú cưng", "DV-KS", true),
    LUU_CHUONG("Lưu chuồng", "DV-LC", true),
    LUU_TRU("Lưu trú", "DV-LT", true),
    NHA_KHOA("Nha khoa", "DV-NK", true),
    XET_NGHIEM("Xét nghiệm", "DV-XN", true),
    CHAN_DOAN_HINH_ANH("Chẩn đoán hình ảnh", "DV-HA", true);

    private final String ten;
    private final String ma;
    private final boolean dichVu;

    private static final Map<String, DanhMucPrefix> LOOKUP_MAP;

    static {
        Map<String, DanhMucPrefix> map = new HashMap<>();
        for (DanhMucPrefix item : values()) {
            map.put(normalizeKey(item.ten), item);
            map.put(normalizeKey(item.name()), item);
            map.put(normalizeKey(item.ma), item);
        }
        // Thêm các biến thể gọi phổ biến
        map.put("THUCAN", THUC_AN);
        map.put("FOOD", THUC_AN);
        map.put("TA", THUC_AN);
        map.put("DOCHOI", DO_CHOI);
        map.put("TOY", DO_CHOI);
        map.put("DC", DO_CHOI);
        map.put("THUOC", THUOC);
        map.put("MED", THUOC);
        map.put("TH", THUOC);
        map.put("CHAMSOCVESINH", CHAM_SOC_VE_SINH);
        map.put("CHAMSOCVAVESINH", CHAM_SOC_VE_SINH);
        map.put("CARE", CHAM_SOC_VE_SINH);
        map.put("DODUNGSINHHOAT", DO_DUNG_SINH_HOAT);
        map.put("DODUNGVASINHHOAT", DO_DUNG_SINH_HOAT);
        map.put("LIV", DO_DUNG_SINH_HOAT);
        map.put("MYPHAM", MY_PHAM);
        map.put("COSMETIC", MY_PHAM);
        map.put("PHUKIEN", PHU_KIEN);
        map.put("ACCESSORY", PHU_KIEN);
        map.put("ACC", PHU_KIEN);
        map.put("PK", PHU_KIEN);
        map.put("DICHVU", DICH_VU);
        map.put("SERVICE", DICH_VU);
        map.put("DICHVUSPA", SPA);
        map.put("GROOMING", SPA);
        map.put("SPAGROOMING", SPA);
        map.put("KHAMDIEUTRI", KHAM_BENH);
        map.put("DICHVUKHAM", KHAM_BENH);
        map.put("PHONGBENH", TIEM_PHONG);
        map.put("LUUTRU", LUU_TRU);
        LOOKUP_MAP = Collections.unmodifiableMap(map);
    }

    DanhMucPrefix(String ten, String ma, boolean dichVu) {
        this.ten = ten;
        this.ma = ma;
        this.dichVu = dichVu;
    }

    public String getTen() {
        return ten;
    }

    public String getMa() {
        return ma;
    }

    public boolean isDichVu() {
        return dichVu;
    }

    /**
     * Tra cứu tiền tố danh mục theo tên hoặc bí danh.
     *
     * @param input Tên danh mục (vd: "Thức ăn", "Thuc an", "Spa", "Nhóm Mỹ phẩm")
     * @return DanhMucPrefix tương ứng, hoặc null nếu chưa có trong Enum
     */
    public static DanhMucPrefix find(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String cleaned = cleanCategoryInput(input);
        return LOOKUP_MAP.get(normalizeKey(cleaned));
    }

    /**
     * Loại bỏ các tiền tố mô tả như "Nhóm ", "Danh mục ", "Loại "...
     */
    public static String cleanCategoryInput(String input) {
        if (input == null) return "";
        String s = input.trim();
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.startsWith("nhóm ") || lower.startsWith("nhom ")) {
            s = s.substring(5).trim();
        } else if (lower.startsWith("danh mục ") || lower.startsWith("danh muc ")) {
            s = s.substring(9).trim();
        } else if (lower.startsWith("dịch vụ ") || lower.startsWith("dich vu ")) {
            // Nếu là "Dịch vụ Spa" -> có thể giữ "Spa" hoặc giữ nguyên
            String rest = s.substring(8).trim();
            if (!rest.isEmpty()) {
                s = rest;
            }
        }
        return s;
    }

    private static String normalizeKey(String key) {
        if (key == null) return "";
        return ChuoiUtil.chuanHoa(key).replaceAll("[^a-z0-9]", "").toUpperCase(Locale.ROOT);
    }
}

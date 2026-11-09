package Bus;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Từ điển (Dictionary) lưu trữ các mã quy ước viết tắt cho Thương hiệu,
 * Loại sản phẩm và Dịch vụ trong cửa hàng thú cưng (Pet Shop).
 * Cho phép đăng ký thêm từ điển động tại thời điểm chạy (Runtime Extensible).
 */
public final class SkuDictionary {

    private static final Map<String, String> BRAND_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> TYPE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> SERVICE_MAP = new ConcurrentHashMap<>();

    static {
        // --- 1. MÃ THƯƠNG HIỆU PHỔ BIẾN ---
        registerBrand("Royal Canin", "RC");
        registerBrand("Bio-Care", "BIO");
        registerBrand("Bio Care", "BIO");
        registerBrand("Biocare", "BIO");
        registerBrand("Pedigree", "PEDI");
        registerBrand("Whiskas", "WHIS");
        registerBrand("Me-O", "MEO");
        registerBrand("Meo", "MEO");
        registerBrand("Catsrang", "CR");
        registerBrand("SmartHeart", "SH");
        registerBrand("Nutrience", "NUT");
        registerBrand("Inaba", "INA");
        registerBrand("Ciao", "CIAO");
        registerBrand("Cat's Best", "CB");
        registerBrand("Sanicat", "SANI");
        registerBrand("Kit Cat", "KC");
        registerBrand("Fidog", "FID");
        registerBrand("Tropiclean", "TROP");

        // --- 2. MÃ LOẠI PHỤ KIỆN / SẢN PHẨM PHỔ BIẾN ---
        registerType("Vòng cổ", "VC");
        registerType("Dây dắt", "DD");
        registerType("Dây xích", "DX");
        registerType("Rọ mõm", "RM");
        registerType("Bát ăn", "BA");
        registerType("Bình nước", "BN");
        registerType("Khay cát", "KC");
        registerType("Nhà cây", "NC");
        registerType("Ổ nệm", "ON");
        registerType("Lược chải", "LC");
        registerType("Bàn cào móng", "BCM");
        registerType("Túi vận chuyển", "TVC");
        registerType("Lồng sắt", "LS");

        // --- 3. MÃ TÊN DỊCH VỤ PHỔ BIẾN ---
        registerService("Tắm sấy", "TAM");
        registerService("Tam say", "TAM");
        registerService("Cắt tỉa", "TIA");
        registerService("Cat tia", "TIA");
        registerService("Cạo lông", "CAO");
        registerService("Vệ sinh tai", "VST");
        registerService("Cắt móng", "CM");
        registerService("Nhuộm lông", "NHUOM");
        registerService("Khám tổng quát", "KTQ");
        registerService("Tiêm phòng", "TP");
        registerService("Tiêm phòng vaccine", "VAC");
        registerService("Tắm và cắt tỉa lông", "TAM-TIA");
        registerService("Lấy cao răng siêu âm", "LCR");
        registerService("Lưu trú thú cưng (theo đêm)", "DEM");
        registerService("Cắt móng & vệ sinh tai", "CM-VST");
        registerService("Xét nghiệm máu tổng quát", "XN-MAU");
        registerService("Siêu âm ổ bụng", "SA-OB");
        registerService("Tiêm ngừa", "TN");
        registerService("Tẩy giun", "TG");
        registerService("Lấy cao răng", "LCR");
    }

    private SkuDictionary() {
        // Utility class
    }

    /** Chuẩn hóa key tra cứu từ điển (bỏ dấu, bỏ khoảng trắng, viết thường). */
    public static String normalizeKey(String input) {
        if (input == null) return "";
        return ChuoiUtil.chuanHoa(input).replaceAll("[^a-z0-9]", "");
    }

    // ==================== ĐĂNG KÝ VÀ TRA CỨU THƯƠNG HIỆU ====================

    public static void registerBrand(String brandName, String code) {
        if (brandName != null && code != null && !brandName.isBlank() && !code.isBlank()) {
            BRAND_MAP.put(normalizeKey(brandName), code.trim().toUpperCase(Locale.ROOT));
        }
    }

    public static String findBrandCode(String brandName) {
        if (brandName == null) return null;
        String cleaned = cleanBrandInput(brandName);
        return BRAND_MAP.get(normalizeKey(cleaned));
    }

    // ==================== ĐĂNG KÝ VÀ TRA CỨU LOẠI SẢN PHẨM ====================

    public static void registerType(String typeName, String code) {
        if (typeName != null && code != null && !typeName.isBlank() && !code.isBlank()) {
            TYPE_MAP.put(normalizeKey(typeName), code.trim().toUpperCase(Locale.ROOT));
        }
    }

    public static String findTypeCode(String typeName) {
        if (typeName == null) return null;
        String cleaned = cleanTypeInput(typeName);
        return TYPE_MAP.get(normalizeKey(cleaned));
    }

    // ==================== ĐĂNG KÝ VÀ TRA CỨU DỊCH VỤ ====================

    public static void registerService(String serviceName, String code) {
        if (serviceName != null && code != null && !serviceName.isBlank() && !code.isBlank()) {
            SERVICE_MAP.put(normalizeKey(serviceName), code.trim().toUpperCase(Locale.ROOT));
        }
    }

    public static String findServiceCode(String serviceName) {
        if (serviceName == null) return null;
        String cleaned = cleanServiceInput(serviceName);
        return SERVICE_MAP.get(normalizeKey(cleaned));
    }

    // ==================== HÀM TIỆN ÍCH LÀM SẠCH NHÃN ĐẦU VÀO ====================

    public static String cleanBrandInput(String input) {
        if (input == null) return "";
        String s = input.trim();
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.startsWith("hãng ") || lower.startsWith("hang ")) {
            s = s.substring(5).trim();
        } else if (lower.startsWith("thương hiệu ") || lower.startsWith("thuong hieu ")) {
            s = s.substring(12).trim();
        }
        return s;
    }

    public static String cleanTypeInput(String input) {
        if (input == null) return "";
        String s = input.trim();
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.startsWith("loại ") || lower.startsWith("loai ")) {
            s = s.substring(5).trim();
        }
        return s;
    }

    public static String cleanServiceInput(String input) {
        if (input == null) return "";
        String s = input.trim();
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.startsWith("dịch vụ ") || lower.startsWith("dich vu ")) {
            s = s.substring(8).trim();
        }
        return s;
    }

    public static String cleanVariantInput(String input) {
        if (input == null) return "";
        String s = input.trim();
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.startsWith("phân loại ") || lower.startsWith("phan loai ")) {
            s = s.substring(10).trim();
        } else if (lower.startsWith("biến thể ") || lower.startsWith("bien the ")) {
            s = s.substring(9).trim();
        }
        return s;
    }
}

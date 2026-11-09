package Bus;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Lớp tiện ích chuẩn Clean Code tự động sinh mã SKU cho Sản phẩm và Dịch vụ trong Pet Shop.
 * <p>
 * Cấu trúc SKU phân cấp:
 * <code>[NHOM_DANH_MUC]-[THUONG_HIEU_HOAC_LOAI]-[BIEN_THE_HOAC_SIZE]</code>
 * </p>
 * <p>
 * Ví dụ thực tế:
 * <ul>
 *   <li>Sản phẩm: Nhóm Thức ăn, Hãng Royal Canin, Phân loại Kitten 2kg &rarr; <b>TA-RC-KITTEN-2KG</b></li>
 *   <li>Sản phẩm: Nhóm Mỹ phẩm, Hãng Bio-Care, Phân loại Ve 200ml &rarr; <b>MP-BIO-VE-200ML</b></li>
 *   <li>Sản phẩm: Nhóm Phụ kiện, Loại Vòng cổ, Phân loại Đỏ size M &rarr; <b>PK-VC-DO-M</b></li>
 *   <li>Dịch vụ: Nhóm Spa, Dịch vụ Tắm sấy, Phân loại Chó size M &rarr; <b>DV-SPA-TAM-M</b></li>
 * </ul>
 */
public final class SkuGenerator {

    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]+");
    private static final Pattern MULTI_HYPHEN = Pattern.compile("-+");
    private static final Pattern LEADING_TRAILING_HYPHEN = Pattern.compile("^-+|-+$");
    private static final Pattern SIZE_STOPWORD = Pattern.compile("(?i)\\bsize\\b\\s*");
    private static final Pattern PET_SPECIES_STOPWORDS = Pattern.compile("(?i)\\b(cho|meo|cun|miu|thu cưng|thu cung|be|pet)\\b\\s*");

    private SkuGenerator() {
        // Utility class, ngăn ngừa khởi tạo đối tượng
    }

    // =========================================================================
    // 1. CÁC PHƯƠNG THỨC SINH SKU CHÍNH (PUBLIC API)
    // =========================================================================

    /**
     * Sinh mã SKU chuyên biệt cho Sản phẩm.
     *
     * @param danhMuc          Tên nhóm danh mục (vd: "Thức ăn", "Mỹ phẩm", "Phụ kiện")
     * @param thuongHieuHoacLoai Tên hãng hoặc loại (vd: "Royal Canin", "Bio-Care", "Vòng cổ")
     * @param bienTheHoacSize   Tên phân loại biến thể/kích cỡ (vd: "Kitten 2kg", "Ve 200ml", "Đỏ size M")
     * @return Chuỗi mã SKU chuẩn hóa (vd: "TA-RC-KITTEN-2KG")
     */
    public static String generateProduct(String danhMuc, String thuongHieuHoacLoai, String bienTheHoacSize) {
        return generate(danhMuc, thuongHieuHoacLoai, bienTheHoacSize, false);
    }

    /**
     * Sinh mã SKU chuyên biệt cho Dịch vụ.
     *
     * @param nhomDichVu      Nhóm dịch vụ (vd: "Spa", "Khám bệnh", "Tiêm phòng")
     * @param tenDichVu       Tên dịch vụ cụ thể (vd: "Tắm sấy", "Cắt tỉa", "Vệ sinh tai")
     * @param bienTheHoacSize Phân loại đối tượng/kích cỡ (vd: "Chó size M", "Size S", "Mèo")
     * @return Chuỗi mã SKU chuẩn hóa (vd: "DV-SPA-TAM-M")
     */
    public static String generateService(String nhomDichVu, String tenDichVu, String bienTheHoacSize) {
        return generate(nhomDichVu, tenDichVu, bienTheHoacSize, true);
    }

    /**
     * Phương thức sinh SKU tổng quát 3 tham số.
     * Tự động phát hiện xem nhóm danh mục có phải là dịch vụ hay không.
     *
     * @param nhomDanhMuc        Tên danh mục (vd: "Thức ăn", "Spa", "Nhóm Mỹ phẩm")
     * @param thuongHieuHoacLoai Tên thương hiệu, loại hoặc tên dịch vụ
     * @param bienTheHoacSize    Biến thể, màu sắc hoặc size
     * @return Chuỗi mã SKU chuẩn hóa
     */
    public static String generate(String nhomDanhMuc, String thuongHieuHoacLoai, String bienTheHoacSize) {
        boolean isDichVu = isServiceContext(nhomDanhMuc, thuongHieuHoacLoai);
        return generate(nhomDanhMuc, thuongHieuHoacLoai, bienTheHoacSize, isDichVu);
    }

    /**
     * Phương thức sinh SKU đầy đủ với cờ xác định Dịch vụ / Sản phẩm.
     *
     * @param nhomDanhMuc        Tên danh mục
     * @param thuongHieuHoacLoai Tên thương hiệu/loại/dịch vụ
     * @param bienTheHoacSize    Biến thể/size
     * @param isDichVu           true nếu là dịch vụ, false nếu là sản phẩm
     * @return Mã SKU hoàn chỉnh
     */
    public static String generate(String nhomDanhMuc, String thuongHieuHoacLoai, String bienTheHoacSize, boolean isDichVu) {
        // 1. Phân giải Nhóm danh mục -> [NHOM_DANH_MUC]
        String categoryCode = resolveCategoryCode(nhomDanhMuc, isDichVu);

        // 2. Phân giải Thương hiệu hoặc Loại hoặc Tên DV -> [THUONG_HIEU_HOAC_LOAI]
        String brandOrTypeCode = resolveBrandOrTypeCode(thuongHieuHoacLoai, isDichVu);

        // 3. Phân giải Biến thể hoặc Size -> [BIEN_THE_HOAC_SIZE]
        String variantCode = normalizeVariant(bienTheHoacSize, isDichVu);

        // 4. Ghép các thành phần phân cấp bằng dấu gạch ngang (-)
        List<String> parts = new ArrayList<>();
        if (!categoryCode.isEmpty()) parts.add(categoryCode);
        if (!brandOrTypeCode.isEmpty()) parts.add(brandOrTypeCode);
        if (!variantCode.isEmpty()) parts.add(variantCode);

        String sku = String.join("-", parts);
        return cleanHyphens(sku);
    }

    // =========================================================================
    // 2. PHÂN GIẢI TỪNG THÀNH PHẦN THEO TỪ ĐIỂN / QUY TẮC PHÂN CẤP
    // =========================================================================

    /**
     * Phân giải tiền tố nhóm danh mục dựa trên Enum DanhMucPrefix hoặc từ điển.
     */
    public static String resolveCategoryCode(String danhMuc, boolean isDichVu) {
        if (danhMuc == null || danhMuc.isBlank()) {
            return isDichVu ? "DV" : "SP";
        }

        String cleaned = DanhMucPrefix.cleanCategoryInput(danhMuc);

        // Kiểm tra trong Enum DanhMucPrefix
        DanhMucPrefix prefix = DanhMucPrefix.find(cleaned);
        if (prefix != null) {
            String code = prefix.getMa();
            if (isDichVu && !code.startsWith("DV")) {
                return "DV-" + code;
            }
            return code;
        }

        // Nếu người dùng truyền vào chuỗi có chứa tiền tố Dịch vụ
        String upperClean = removeAccents(cleaned).toUpperCase(Locale.ROOT);
        if (isDichVu) {
            String slug = toSkuSlug(cleaned);
            if (slug.startsWith("DV-") || slug.equals("DV")) {
                return slug;
            }
            return "DV-" + slug;
        }

        // Fallback cho sản phẩm chưa có trong từ điển: lấy chữ cái đầu hoặc slug ngắn
        return generateAcronymOrShortSlug(cleaned);
    }

    /**
     * Phân giải thương hiệu hoặc phân loại sản phẩm / tên dịch vụ.
     */
    public static String resolveBrandOrTypeCode(String brandOrType, boolean isDichVu) {
        if (brandOrType == null || brandOrType.isBlank()) {
            return "";
        }

        if (isDichVu) {
            // Tra cứu tên dịch vụ (ví dụ: "Tắm sấy" -> "TAM")
            String serviceCode = SkuDictionary.findServiceCode(brandOrType);
            if (serviceCode != null) {
                return serviceCode;
            }
            // Fallback dịch vụ: lấy từ đầu tiên nếu là chuỗi (ví dụ: "Tắm sạch" -> "TAM")
            String cleaned = SkuDictionary.cleanServiceInput(brandOrType);
            String[] words = removeAccents(cleaned).trim().split("\\s+");
            if (words.length > 0 && !words[0].isEmpty()) {
                return toSkuSlug(words[0]);
            }
            return toSkuSlug(brandOrType);
        }

        // Với sản phẩm: kiểm tra thương hiệu trước, sau đó tới loại sản phẩm
        String brandCode = SkuDictionary.findBrandCode(brandOrType);
        if (brandCode != null) {
            return brandCode;
        }

        String typeCode = SkuDictionary.findTypeCode(brandOrType);
        if (typeCode != null) {
            return typeCode;
        }

        // Fallback: Nếu là cụm từ nhiều từ (như "Royal Canin" -> "RC", "Vòng Cổ" -> "VC")
        String cleaned = SkuDictionary.cleanBrandInput(SkuDictionary.cleanTypeInput(brandOrType));
        return generateAcronymOrShortSlug(cleaned);
    }

    /**
     * Chuẩn hóa biến thể hoặc size: loại bỏ từ dừng "size", "loại", ký tự thừa.
     */
    public static String normalizeVariant(String variant, boolean isDichVu) {
        if (variant == null || variant.isBlank()) {
            return "";
        }

        String s = SkuDictionary.cleanVariantInput(variant);
        s = removeAccents(s);

        if (isDichVu) {
            // Trong dịch vụ Pet Shop, phân loại thường là cỡ thú cưng (vd: "Chó size M" -> "M")
            // Loại bỏ các từ chỉ loài thú cưng và từ "size"
            s = PET_SPECIES_STOPWORDS.matcher(s).replaceAll(" ");
            s = SIZE_STOPWORD.matcher(s).replaceAll(" ");
            s = s.trim();
            if (s.isEmpty()) {
                // Nếu sau khi lọc bị rỗng (ví dụ người dùng chỉ nhập "Chó"), giữ lại slug của chuỗi gốc
                return toSkuSlug(variant);
            }
            return toSkuSlug(s);
        }

        // Trong sản phẩm: loại bỏ từ "size" để biến thể gọn gàng (vd: "Đỏ size M" -> "DO-M")
        s = SIZE_STOPWORD.matcher(s).replaceAll(" ");
        return toSkuSlug(s);
    }

    // =========================================================================
    // 3. XỬ LÝ CHUỖI TIẾNG VIỆT VÀ CHUẨN HÓA SLUG SKU
    // =========================================================================

    /**
     * Chuyển toàn bộ ký tự tiếng Việt có dấu sang không dấu (loại bỏ Unicode diacritics).
     * Xử lý triệt để ký tự đặc thù 'đ', 'Đ' mà Normalizer NFD không tự phân rã.
     *
     * @param input Chuỗi văn bản tiếng Việt
     * @return Chuỗi không dấu, giữ nguyên chữ hoa/thường
     */
    public static String removeAccents(String input) {
        if (input == null) return "";
        // 1. Phân rã chuẩn Unicode NFD
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // 2. Xóa các dấu kết hợp (combining marks)
        String withoutMarks = DIACRITICAL_MARKS.matcher(normalized).replaceAll("");
        // 3. Chuyển đổi 'đ'/'Đ' thành 'd'/'D'
        return withoutMarks.replace('đ', 'd').replace('Đ', 'D');
    }

    /**
     * Chuẩn hóa chuỗi thành Slug SKU:
     * - Bỏ dấu tiếng Việt
     * - Thay ký tự đặc biệt và khoảng trắng bằng gạch ngang (-)
     * - Rút gọn các dấu gạch ngang liền kề
     * - Cắt bỏ gạch ngang ở đầu/cuối
     * - Chuyển sang VIẾT HOA (toUpperCase)
     *
     * @param input Chuỗi đầu vào
     * @return Chuỗi chuẩn hóa cho SKU (vd: "Kitten 2kg" -> "KITTEN-2KG")
     */
    public static String toSkuSlug(String input) {
        if (input == null) return "";
        String noAccents = removeAccents(input.trim());
        String withHyphens = NON_ALPHANUMERIC.matcher(noAccents).replaceAll("-");
        return cleanHyphens(withHyphens).toUpperCase(Locale.ROOT);
    }

    /**
     * Tạo mã viết tắt Acronym từ các chữ cái đầu tiên của từng từ.
     * Ví dụ: "Royal Canin" -> "RC", "Vòng cổ" -> "VC", "Cát vệ sinh" -> "CVS"
     */
    public static String toAcronym(String input) {
        if (input == null || input.isBlank()) return "";
        String noAccents = removeAccents(input.trim());
        String[] words = noAccents.split("[^A-Za-z0-9]+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
            }
        }
        return sb.toString();
    }

    /**
     * Tự động sinh mã viết tắt (Acronym) hoặc rút gọn slug cho các từ chưa có trong từ điển.
     */
    private static String generateAcronymOrShortSlug(String input) {
        if (input == null || input.isBlank()) return "";
        String[] words = removeAccents(input.trim()).split("[^A-Za-z0-9]+");
        if (words.length >= 2) {
            String acronym = toAcronym(input);
            if (acronym.length() >= 2) {
                return acronym;
            }
        }
        String slug = toSkuSlug(input);
        if (slug.length() > 6) {
            return slug.substring(0, 4);
        }
        return slug;
    }

    /**
     * Làm sạch các dấu gạch ngang trùng lặp và ở 2 đầu chuỗi.
     */
    private static String cleanHyphens(String text) {
        if (text == null) return "";
        String s = MULTI_HYPHEN.matcher(text).replaceAll("-");
        return LEADING_TRAILING_HYPHEN.matcher(s).replaceAll("");
    }

    /**
     * Kiểm tra nhanh xem ngữ cảnh có phải là Dịch vụ hay không.
     */
    private static boolean isServiceContext(String category, String brandOrType) {
        String catClean = DanhMucPrefix.cleanCategoryInput(category);
        DanhMucPrefix prefix = DanhMucPrefix.find(catClean);
        if (prefix != null && prefix.isDichVu()) {
            return true;
        }

        String lowerCat = (category == null ? "" : category.toLowerCase(Locale.ROOT));
        if (lowerCat.contains("dịch vụ") || lowerCat.contains("dich vu") || lowerCat.contains("spa")) {
            return true;
        }

        return SkuDictionary.findServiceCode(brandOrType) != null;
    }

    // =========================================================================
    // 4. HÀM MAIN KIỂM THỬ VÀ MINH HỌA
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("       HỆ THỐNG TỰ ĐỘNG SINH MÃ SKU PET SHOP (CLEAN CODE)             ");
        System.out.println("======================================================================");

        // 1. Kiểm thử 4 ca kiểm thử mẫu từ đề bài
        runCase("Sản phẩm 1", "Nhóm Thức ăn", "Hãng Royal Canin", "Phân loại Kitten 2kg", "TA-RC-KITTEN-2KG", false);
        runCase("Sản phẩm 2", "Nhóm Mỹ phẩm", "Hãng Bio-Care", "Phân loại Ve 200ml", "MP-BIO-VE-200ML", false);
        runCase("Sản phẩm 3", "Nhóm Phụ kiện", "Loại Vòng cổ", "Phân loại Đỏ size M", "PK-VC-DO-M", false);
        runCase("Dịch vụ 4", "Nhóm Spa", "Dịch vụ Tắm sấy", "Phân loại Chó size M", "DV-SPA-TAM-M", true);

        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("       KIỂM THỬ THÊM CÁC TRƯỜNG HỢP THỰC TẾ & KÝ TỰ ĐẶC BIỆT          ");
        System.out.println("----------------------------------------------------------------------");

        // Các ca thực tế mở rộng
        runCase("Sản phẩm 4", "Thức ăn", "Whiskas", "Cá biển 1.2kg", "TA-WHIS-CA-BIEN-1-2KG", false);
        runCase("Sản phẩm 5", "Phụ kiện", "Dây dắt", "Xanh Size L", "PK-DD-XANH-L", false);
        runCase("Dịch vụ 5", "Dịch vụ", "Cắt tỉa", "Mèo size S", "DV-TIA-S", true);
        runCase("Dịch vụ 6", "Khám bệnh", "Khám tổng quát", "Chó", "DV-KB-KTQ-CHO", true);
        runCase("Dịch vụ 7", "Khám bệnh", "Khám tổng quát", null, "DV-KB-KTQ", true);

        System.out.println("\nHoàn tất kiểm thử SkuGenerator!");
    }

    private static void runCase(String testName, String cat, String brandOrType, String variant, String expected, boolean isService) {
        String actual = isService
                ? generateService(cat, brandOrType, variant)
                : generateProduct(cat, brandOrType, variant);

        boolean pass = Objects.equals(actual, expected);
        String status = pass ? "[PASS]" : "[FAIL]";

        System.out.printf("%s %-12s: [%s] | [%s] | [%s]\n", status, testName, cat, brandOrType, variant);
        System.out.printf("       -> Kết quả : %s\n", actual);
        if (!pass) {
            System.out.printf("       -> Kỳ vọng : %s\n", expected);
        }
    }
}

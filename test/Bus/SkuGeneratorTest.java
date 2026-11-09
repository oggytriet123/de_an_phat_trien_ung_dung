package Bus;

import java.util.Objects;

/**
 * Bộ kiểm thử toàn diện (Unit Test) cho SkuGenerator, DanhMucPrefix và SkuDictionary.
 * Chạy độc lập qua hàm main, không phụ thuộc thư viện ngoài.
 */
public class SkuGeneratorTest {

    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                 TEST SUITE: SKU GENERATOR (PET SHOP SYSTEM)                    ");
        System.out.println("================================================================================");

        testCacViDuChinhYeuCau();
        testXuLyTiengVietVaKyTuDacBiet();
        testXuLyKhoangTrangThuaVaChuHoaChuThuong();
        testTienToDanhMucVaEnumMapping();
        testThuongHieuVaLoaiHangTuDien();
        testDichVuVaBienTheThuCung();
        testDangKyTuDienDong();
        testTuDongSinhVietTatFallback();
        testNullSafetyVaBienTheRong();

        System.out.println("================================================================================");
        System.out.printf("TỔNG KẾT KIỂM THỬ: %d THÀNH CÔNG, %d THẤT BẠI\n", passCount, failCount);
        System.out.println("================================================================================");

        if (failCount > 0) {
            System.err.println("CẢNH BÁO: Có ca kiểm thử không đạt yêu cầu!");
            System.exit(1);
        } else {
            System.out.println("TUYỆT VỜI! Tất cả các ca kiểm thử đều PASS 100%.");
        }
    }

    // -------------------------------------------------------------------------
    // 1. KIỂM THỬ 4 VÍ DỤ CHÍNH TRONG ĐỀ BÀI
    // -------------------------------------------------------------------------
    private static void testCacViDuChinhYeuCau() {
        printHeader("1. KIỂM THỬ 4 VÍ DỤ ĐẶC TẢ TỪ ĐỀ BÀI");

        // Ví dụ 1: Sản phẩm: Nhóm Thức ăn, Hãng Royal Canin, Phân loại Kitten 2kg -> TA-RC-KITTEN-2KG
        assertSku("TC-REQ-01", "Nhóm Thức ăn + Hãng Royal Canin + Phân loại Kitten 2kg",
                SkuGenerator.generateProduct("Nhóm Thức ăn", "Hãng Royal Canin", "Phân loại Kitten 2kg"),
                "TA-RC-KITTEN-2KG");

        // Ví dụ 1 (dạng tên thuần không có tiền tố "Nhóm/Hãng/Phân loại")
        assertSku("TC-REQ-01b", "Thức ăn + Royal Canin + Kitten 2kg",
                SkuGenerator.generateProduct("Thức ăn", "Royal Canin", "Kitten 2kg"),
                "TA-RC-KITTEN-2KG");

        // Ví dụ 2: Sản phẩm: Nhóm Mỹ phẩm, Hãng Bio-Care, Phân loại Ve 200ml -> MP-BIO-VE-200ML
        assertSku("TC-REQ-02", "Nhóm Mỹ phẩm + Hãng Bio-Care + Phân loại Ve 200ml",
                SkuGenerator.generateProduct("Nhóm Mỹ phẩm", "Hãng Bio-Care", "Phân loại Ve 200ml"),
                "MP-BIO-VE-200ML");

        assertSku("TC-REQ-02b", "Mỹ phẩm + Bio-Care + Ve 200ml",
                SkuGenerator.generateProduct("Mỹ phẩm", "Bio-Care", "Ve 200ml"),
                "MP-BIO-VE-200ML");

        // Ví dụ 3: Sản phẩm: Nhóm Phụ kiện, Loại Vòng cổ, Phân loại Đỏ size M -> PK-VC-DO-M
        assertSku("TC-REQ-03", "Nhóm Phụ kiện + Loại Vòng cổ + Phân loại Đỏ size M",
                SkuGenerator.generateProduct("Nhóm Phụ kiện", "Loại Vòng cổ", "Phân loại Đỏ size M"),
                "PK-VC-DO-M");

        assertSku("TC-REQ-03b", "Phụ kiện + Vòng cổ + Đỏ size M",
                SkuGenerator.generateProduct("Phụ kiện", "Vòng cổ", "Đỏ size M"),
                "PK-VC-DO-M");

        // Ví dụ 4: Dịch vụ: Nhóm Spa, Dịch vụ Tắm sấy, Phân loại Chó size M -> DV-SPA-TAM-M
        assertSku("TC-REQ-04", "Nhóm Spa + Dịch vụ Tắm sấy + Phân loại Chó size M",
                SkuGenerator.generateService("Nhóm Spa", "Dịch vụ Tắm sấy", "Phân loại Chó size M"),
                "DV-SPA-TAM-M");

        assertSku("TC-REQ-04b", "Spa + Tắm sấy + Chó size M (tự động nhận diện Service)",
                SkuGenerator.generate("Spa", "Tắm sấy", "Chó size M"),
                "DV-SPA-TAM-M");
    }

    // -------------------------------------------------------------------------
    // 2. KIỂM THỬ XỬ LÝ TIẾNG VIỆT VÀ KÝ TỰ ĐẶC BIỆT
    // -------------------------------------------------------------------------
    private static void testXuLyTiengVietVaKyTuDacBiet() {
        printHeader("2. KIỂM THỬ XỬ LÝ TIẾNG VIỆT CÓ DẤU (Đ/đ, nguyên âm) VÀ KÝ TỰ ĐẶC BIỆT");

        // Kiểm tra xử lý chữ đ và Đ
        assertString("TC-VN-01", "Bỏ dấu tiếng Việt chữ đ/Đ",
                SkuGenerator.removeAccents("Đồ chơi cho chó đực"),
                "Do choi cho cho duc");

        // Kiểm tra toSkuSlug
        assertString("TC-VN-02", "Slug hóa chuỗi có dấu và ký tự lạ",
                SkuGenerator.toSkuSlug("Vòng cổ da bò (Cao cấp) #1!"),
                "VONG-CO-DA-BO-CAO-CAP-1");

        // Kiểm tra thay thế ký tự đặc biệt bằng gạch ngang và không bị lặp gạch ngang
        assertString("TC-VN-03", "Ký tự đặc biệt liên tiếp",
                SkuGenerator.toSkuSlug("Kitten @@@ 2kg *** [Túi zip]"),
                "KITTEN-2KG-TUI-ZIP");
    }

    // -------------------------------------------------------------------------
    // 3. KIỂM THỬ KHOẢNG TRẮNG THỪA VÀ HOA/THƯỜNG
    // -------------------------------------------------------------------------
    private static void testXuLyKhoangTrangThuaVaChuHoaChuThuong() {
        printHeader("3. KIỂM THỬ KHOẢNG TRẮNG THỪA VÀ CHỮ HOA/THƯỜNG");

        assertSku("TC-SPACE-01", "Dư thừa khoảng trắng ở đầu, giữa và cuối",
                SkuGenerator.generateProduct("   Thức   ăn   ", "  Royal   Canin  ", "  Kitten   2kg  "),
                "TA-RC-KITTEN-2KG");

        assertSku("TC-CASE-01", "Nhập toàn bộ chữ thường",
                SkuGenerator.generateProduct("thức ăn", "royal canin", "kitten 2kg"),
                "TA-RC-KITTEN-2KG");

        assertSku("TC-CASE-02", "Nhập chữ hoa lộn xộn",
                SkuGenerator.generateProduct("mỸ pHẩM", "bIo-CaRe", "vE 200mL"),
                "MP-BIO-VE-200ML");
    }

    // -------------------------------------------------------------------------
    // 4. KIỂM THỬ ENUM VÀ MAPPING DANH MỤC
    // -------------------------------------------------------------------------
    private static void testTienToDanhMucVaEnumMapping() {
        printHeader("4. KIỂM THỬ ENUM VÀ TIỀN TỐ DANH MỤC PHỔ BIẾN");

        assertString("TC-CAT-01", "Thức ăn -> TA", DanhMucPrefix.find("Thức ăn").getMa(), "TA");
        assertString("TC-CAT-02", "Mỹ phẩm -> MP", DanhMucPrefix.find("Mỹ phẩm").getMa(), "MP");
        assertString("TC-CAT-03", "Phụ kiện -> PK", DanhMucPrefix.find("Phụ kiện").getMa(), "PK");
        assertString("TC-CAT-04", "Dịch vụ -> DV", DanhMucPrefix.find("Dịch vụ").getMa(), "DV");
        assertString("TC-CAT-05", "Spa -> DV-SPA", DanhMucPrefix.find("Spa").getMa(), "DV-SPA");
        assertString("TC-CAT-06", "Cát vệ sinh -> CAT", DanhMucPrefix.find("Cát vệ sinh").getMa(), "CAT");
        assertString("TC-CAT-07", "Tiêm phòng -> DV-TP", DanhMucPrefix.find("Tiêm phòng").getMa(), "DV-TP");
    }

    // -------------------------------------------------------------------------
    // 5. KIỂM THỬ THƯƠNG HIỆU VÀ LOẠI HÀNG TỪ ĐIỂN
    // -------------------------------------------------------------------------
    private static void testThuongHieuVaLoaiHangTuDien() {
        printHeader("5. KIỂM THỬ TỪ ĐIỂN THƯƠNG HIỆU VÀ LOẠI SẢN PHẨM");

        assertSku("TC-BRAND-01", "Thương hiệu Pedigree",
                SkuGenerator.generateProduct("Thức ăn", "Pedigree", "Bò nướng 500g"),
                "TA-PEDI-BO-NUONG-500G");

        assertSku("TC-BRAND-02", "Thương hiệu Whiskas",
                SkuGenerator.generateProduct("Thức ăn", "Whiskas", "Cá hồi 1.2kg"),
                "TA-WHIS-CA-HOI-1-2KG");

        assertSku("TC-TYPE-01", "Loại phụ kiện Dây dắt",
                SkuGenerator.generateProduct("Phụ kiện", "Dây dắt", "Nâu 1.5m"),
                "PK-DD-NAU-1-5M");

        assertSku("TC-TYPE-02", "Loại phụ kiện Bát ăn",
                SkuGenerator.generateProduct("Phụ kiện", "Bát ăn", "Inox size L"),
                "PK-BA-INOX-L");
    }

    // -------------------------------------------------------------------------
    // 6. KIỂM THỬ DỊCH VỤ VÀ BIẾN THỂ THÚ CƯNG
    // -------------------------------------------------------------------------
    private static void testDichVuVaBienTheThuCung() {
        printHeader("6. KIỂM THỬ DỊCH VỤ VÀ BIẾN THỂ THEO THÚ CƯNG");

        assertSku("TC-SRV-01", "Dịch vụ Cắt tỉa lông mèo size S",
                SkuGenerator.generateService("Spa", "Cắt tỉa", "Mèo size S"),
                "DV-SPA-TIA-S");

        assertSku("TC-SRV-02", "Dịch vụ Vệ sinh tai cho cún size L",
                SkuGenerator.generateService("Spa", "Vệ sinh tai", "Cún size L"),
                "DV-SPA-VST-L");

        assertSku("TC-SRV-03", "Dịch vụ Tiêm phòng vaccine dại",
                SkuGenerator.generateService("Tiêm phòng", "Tiêm phòng", "Vaccine dại"),
                "DV-TP-TP-VACCINE-DAI");
    }

    // -------------------------------------------------------------------------
    // 7. KIỂM THỬ ĐĂNG KÝ TỪ ĐIỂN ĐỘNG (RUNTIME EXTENSIBLE)
    // -------------------------------------------------------------------------
    private static void testDangKyTuDienDong() {
        printHeader("7. KIỂM THỬ ĐĂNG KÝ TỪ ĐIỂN MỚI TẠI THỜI ĐIỂM CHẠY");

        // Đăng ký một thương hiệu mới chưa có trong hệ thống
        SkuDictionary.registerBrand("Natural Bridge", "NB");
        assertSku("TC-EXT-01", "Đăng ký thương hiệu mới Natural Bridge -> NB",
                SkuGenerator.generateProduct("Thức ăn", "Natural Bridge", "Chó con 2kg"),
                "TA-NB-CHO-CON-2KG");

        // Đăng ký một dịch vụ mới
        SkuDictionary.registerService("Massage bấm huyệt", "MASSAGE");
        assertSku("TC-EXT-02", "Đăng ký dịch vụ mới Massage -> MASSAGE",
                SkuGenerator.generateService("Spa", "Massage bấm huyệt", "Chó size M"),
                "DV-SPA-MASSAGE-M");
    }

    // -------------------------------------------------------------------------
    // 8. KIỂM THỬ TỰ ĐỘNG SINH VIẾT TẮT FALLBACK (KHI CHƯA CÓ TRONG TỪ ĐIỂN)
    // -------------------------------------------------------------------------
    private static void testTuDongSinhVietTatFallback() {
        printHeader("8. KIỂM THỬ FALLBACK CHO DANH MỤC / HÃNG CHƯA ĐĂNG KÝ");

        // Hãng có 3 từ chưa đăng ký: "Pet Care Pro" -> Tự sinh Acronym "PCP"
        assertSku("TC-FALLBACK-01", "Tự sinh viết tắt từ 'Pet Care Pro' -> PCP",
                SkuGenerator.generateProduct("Thức ăn", "Pet Care Pro", "Hạt 1kg"),
                "TA-PCP-HAT-1KG");

        // Nhóm mới 3 từ: "Thời trang thú cưng" -> "TTTC"
        assertSku("TC-FALLBACK-02", "Tự sinh viết tắt từ 'Thời trang thú cưng' -> TTTC",
                SkuGenerator.generateProduct("Thời trang thú cưng", "Doggy Style", "Áo len vàng size S"),
                "TTTC-DS-AO-LEN-VANG-S");
    }

    // -------------------------------------------------------------------------
    // 9. KIỂM THỬ NULL-SAFETY VÀ BIẾN THỂ RỖNG
    // -------------------------------------------------------------------------
    private static void testNullSafetyVaBienTheRong() {
        printHeader("9. KIỂM THỬ TÍNH AN TOÀN KHI THAM SỐ NULL HOẶC RỖNG");

        // Biến thể null
        assertSku("TC-NULL-01", "Biến thể là null",
                SkuGenerator.generateProduct("Thức ăn", "Royal Canin", null),
                "TA-RC");

        // Biến thể chuỗi rỗng
        assertSku("TC-NULL-02", "Biến thể là chuỗi rỗng",
                SkuGenerator.generateProduct("Thức ăn", "Royal Canin", "   "),
                "TA-RC");

        // Hãng là null
        assertSku("TC-NULL-03", "Hãng là null",
                SkuGenerator.generateProduct("Thức ăn", null, "Kitten 2kg"),
                "TA-KITTEN-2KG");

        // Danh mục là null (fallback sang SP)
        assertSku("TC-NULL-04", "Danh mục là null",
                SkuGenerator.generateProduct(null, "Royal Canin", "Kitten 2kg"),
                "SP-RC-KITTEN-2KG");
    }

    // -------------------------------------------------------------------------
    // CÁC HÀM TIỆN ÍCH CHO ASSERTION
    // -------------------------------------------------------------------------
    private static void assertSku(String testId, String description, String actual, String expected) {
        boolean pass = Objects.equals(actual, expected);
        if (pass) {
            passCount++;
            System.out.printf("  [PASS] %-12s: %s\n", testId, description);
            System.out.printf("         SKU: %s\n", actual);
        } else {
            failCount++;
            System.err.printf("  [FAIL] %-12s: %s\n", testId, description);
            System.err.printf("         Thực tế : %s\n", actual);
            System.err.printf("         Kỳ vọng : %s\n", expected);
        }
    }

    private static void assertString(String testId, String description, String actual, String expected) {
        boolean pass = Objects.equals(actual, expected);
        if (pass) {
            passCount++;
            System.out.printf("  [PASS] %-12s: %s (%s)\n", testId, description, actual);
        } else {
            failCount++;
            System.err.printf("  [FAIL] %-12s: %s\n", testId, description);
            System.err.printf("         Thực tế : %s\n", actual);
            System.err.printf("         Kỳ vọng : %s\n", expected);
        }
    }

    private static void printHeader(String title) {
        System.out.println("\n--- " + title + " ---");
    }
}

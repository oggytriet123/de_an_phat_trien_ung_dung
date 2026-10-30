# NHẬT KÝ SỬ DỤNG TRÍ TUỆ NHÂN TẠO (AI PROMPT & RESPONSE LOGBOOK)
## PHÂN HỆ: QUẢN LÝ THÚ CƯNG, KHO HÀNG, ĐƠN THUỐC & PHÁC ĐỒ ĐIỀU TRỊ
### ĐỀ ÁN PHÁT TRIỂN ỨNG DỤNG: HỆ THỐNG QUẢN LÝ PHÒNG KHÁM THÚ Y (PETCARE POS)

---

> **Học phần:** Đề án Phát triển Ứng dụng  
> **Dự án:** Hệ thống Quản lý Phòng khám Thú y (PetCare POS)  
> **Phân hệ phụ trách:** 1. Quản lý Thú cưng • 2. Quản lý Kho hàng • 3. Quản lý Đơn thuốc • 4. Lập & Theo dõi Phác đồ Điều trị  
> **Sinh viên thực hiện:** Nguyễn Ngọc Toàn  
> **Nền tảng & Công nghệ:** Java 21 • Java Swing Custom UI (Graphics2D) • SQL Server (JDBC)  
> **Mô hình AI hỗ trợ:** Antigravity AI (Google DeepMind) • ChatGPT (GPT-4o) / Claude  
> **Kho lưu trữ GitHub:** [oggytriet123/de_an_phat_trien_ung_dung](https://github.com/oggytriet123/de_an_phat_trien_ung_dung) (Nhánh: `ngoc-toan`)  
> **Thời gian thực hiện:** Tháng 10/2026  

---

## MỤC LỤC
1. [TỔNG QUAN PHƯƠNG PHÁP GHI NHẬT KÝ AI](#1-tổng-quan-phương-pháp-ghi-nhật-ký-ai)
2. [BẢNG TỔNG HỢP CÁC PHIÊN LÀM VIỆC VỚI AI (PROMPT SUMMARY TABLE)](#2-bảng-tổng-hợp-các-phiên-làm-việc-với-ai-prompt-summary-table)
3. [CHI TIẾT TỪNG PHIÊN LÀM VIỆC (PROMPT – KẾT QUẢ AI – THẨM ĐỊNH)](#3-chi-tiết-từng-phiên-làm-việc-prompt--kết-quả-ai--thẩm-định)
   - [PHẦN I: PHÂN HỆ QUẢN LÝ THÚ CƯNG (PET MANAGEMENT)](#phần-i-phân-hệ-quản-lý-thú-cưng-pet-management)
     - [Phiên 1: Thiết kế giao diện danh sách thú cưng, Avatar bo tròn & Huy hiệu sức khỏe](#phiên-1-thiết-kế-giao-diện-danh-sách-thú-cưng-avatar-bo-tròn--huy-hiệu-sức-khỏe)
     - [Phiên 2: Tích hợp menu ngữ cảnh chuột phải mở nhanh Kê đơn & Phác đồ](#phiên-2-tích-hợp-menu-ngữ-cảnh-chuột-phải-mở-nhanh-kê-đơn--phác-đồ)
   - [PHẦN II: PHÂN HỆ QUẢN LÝ KHO HÀNG (INVENTORY MANAGEMENT)](#phần-ii-phân-hệ-quản-lý-kho-hàng-inventory-management)
     - [Phiên 3: Xây dựng giao diện Kho hàng & Cảnh báo tồn kho thấp](#phiên-3-xây-dựng-giao-diện-kho-hàng--cảnh-báo-tồn-kho-thấp)
     - [Phiên 4: Xây dựng hàm trừ tồn kho tự động `xuatKho()` & Cơ chế Observer Pattern](#phiên-4-xây-dựng-hàm-trừ-tồn-kho-tự-động-xuatkho--cơ-chế-observer-pattern)
     - [Phiên 5: Xây dựng thành phần cuộn lịch động `CalendarPicker` bằng Swing thuần](#phiên-5-xây-dựng-thành-phần-cuộn-lịch-động-calendarpicker-bằng-swing-thuần)
   - [PHẦN III: PHÂN HỆ QUẢN LÝ ĐƠN THUỐC (PRESCRIPTION MANAGEMENT)](#phần-iii-phân-hệ-quản-lý-đơn-thuốc-prescription-management)
     - [Phiên 6: Khắc phục lỗi form kê đơn tự động điền sẵn dữ liệu thú cưng & thuốc index 0](#phiên-6-khắc-phục-lỗi-form-kê-đơn-tự-động-điền-sẵn-dữ-liệu-thú-cưng--thuốc-index-0)
     - [Phiên 7: Tạo Modal 'Chọn thuốc từ kho hàng' & Kiểm tra tồn kho thời gian thực](#phiên-7-tạo-modal-chọn-thuốc-từ-kho-hàng--kiểm-tra-tồn-kho-thời-gian-thực)
     - [Phiên 8: Tự động trừ kho thuốc khi hoàn tất đơn & Xuất phiếu in đơn thuốc](#phiên-8-tự-động-trừ-kho-thuốc-khi-hoàn-tất-đơn--xuất-phiếu-in-đơn-thuốc)
   - [PHẦN IV: PHÂN HỆ PHÁC ĐỒ ĐIỀU TRỊ (TREATMENT PROTOCOL & CLINICAL DIARY)](#phần-iv-phân-hệ-phác-đồ-điều-trị-treatment-protocol--clinical-diary)
     - [Phiên 9: Khắc phục lỗi tạo phác đồ mới bị điền sẵn dữ liệu của phác đồ cũ](#phiên-9-khắc-phục-lỗi-tạo-phác-đồ-mới-bị-điền-sẵn-dữ-liệu-của-phác-đồ-cũ)
     - [Phiên 10: Phân tích ảnh chụp màn hình lỗi xung đột giao diện (UI Overlap / Collision)](#phiên-10-phân-tích-ảnh-chụp-màn-hình-lỗi-xung-đột-giao-diện-ui-overlap--collision)
     - [Phiên 11: Tái thiết kế Modal theo dõi diễn tiến thành 3 Card độc lập & JTable nhật ký 6 cột](#phiên-11-tái-thiết-kế-modal-theo-dõi-diễn-tiến-thành-3-card-độc-lập--jtable-nhật-ký-6-cột)
4. [TỔNG KẾT HIỆU QUẢ VÀ KINH NGHIỆM PROMPT ENGINEERING](#4-tổng-kết-hiệu-quả-và-kinh-nghiệm-prompt-engineering)

---

## 1. TỔNG QUAN PHƯƠNG PHÁP GHI NHẬT KÝ AI

Nhật ký này ghi lại đầy đủ và trung thực toàn bộ quá trình sử dụng Trí tuệ Nhân tạo (AI) trong quá trình nghiên cứu, thiết kế kiến trúc và viết mã nguồn cho **4 phân hệ cốt lõi**:
* **Quản lý Thú cưng** ([`ThuCungPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/ThuCungPanel.java))
* **Quản lý Kho hàng & Lịch sử kho** ([`KhoHangPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/KhoHangPanel.java), [`KhoHangBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/KhoHangBUS.java))
* **Quản lý Đơn thuốc** ([`DonThuocPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/DonThuocPanel.java), [`DonThuocBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/DonThuocBUS.java))
* **Tạo & Theo dõi Phác đồ Điều trị** ([`PhacDoPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/PhacDoPanel.java), [`PhacDoBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/PhacDoBUS.java))

Mỗi phiên làm việc được ghi nhận theo đúng cấu trúc tiêu chuẩn:
$$\text{Mục tiêu bài toán} \longrightarrow \text{Câu lệnh Prompt (nguyên văn)} \longrightarrow \text{Mã nguồn / Giải pháp AI trả về} \longrightarrow \text{Thẩm định & Nghiệm thu của Sinh viên}$$

---

## 2. BẢNG TỔNG HỢP CÁC PHIÊN LÀM VIỆC VỚI AI (PROMPT SUMMARY TABLE)

| Mã phiên | Ngày thực hiện | Phân hệ | Nhiệm vụ / Bài toán cần giải quyết | Tóm tắt Prompt của Sinh viên | Kết quả nghiệm thu |
| :---: | :---: | :--- | :--- | :--- | :--- |
| **SS-01** | 11/10/2026 | Thú cưng | Dựng UI danh sách thú cưng & custom cell | Dựng bảng thú cưng có avatar tròn, huy hiệu loài và tình trạng sức khỏe bằng Java Swing thuần. | Đạt 100%, render mượt mà không dùng thư viện ngoài. |
| **SS-02** | 12/10/2026 | Thú cưng | Menu ngữ cảnh chuột phải mở Kê đơn & Phác đồ | Thêm menu chuột phải vào dòng thú cưng để gọi `moKeDonChoThuCung()` và `moPhacDoChoThuCung()`. | Thao tác 1-click mở form điền sẵn thông tin thú cưng. |
| **SS-03** | 11/10/2026 | Kho hàng | Quản lý kho dược phẩm, cảnh báo tồn kho | Xây dựng màn hình kho hàng có thẻ KPI, bộ lọc danh mục và huy hiệu cảnh báo sắp hết hàng. | Phân loại rõ ràng, giao diện phẳng chuẩn SaaS. |
| **SS-04** | 20/10/2026 | Kho hàng | Tự động trừ tồn kho & Observer Pattern | Viết hàm `xuatKho()` trong `KhoHangBUS` và thêm `addListener` để `KhoHangPanel` tự cập nhật khi kê đơn. | Kho tự động trừ và cập nhật bảng kho tức thì. |
| **SS-05** | 13/10/2026 | Kho hàng | Bộ cuộn lịch pop-up chọn ngày trực quan | Xây dựng `CalendarPicker.java` bằng Swing thuần để chọn ngày cho bộ lọc kho và phiếu nhập. | Hoạt động độc lập, không phụ thuộc thư viện ngày tháng ngoài. |
| **SS-06** | 26/10/2026 | Đơn thuốc | Sửa lỗi form kê đơn tự điền dữ liệu index 0 | Sửa lỗi form kê đơn mở lên bị điền sẵn thuốc Clavamox và thú cưng đầu tiên, form mới phải rỗng. | Tách bạch `isEdit` và `isCreate`, form mới sạch 100%. |
| **SS-07** | 26/10/2026 | Đơn thuốc | Modal chọn thuốc trực tiếp từ kho hàng | Tạo nút `+ Chọn thuốc từ kho` lấy danh sách từ `KhoHangBUS`, điền giá bán, số lượng tồn thực tế. | Chọn thuốc nhanh chóng, ngăn chặn kê quá số lượng tồn. |
| **SS-08** | 26/10/2026 | Đơn thuốc | Liên kết trừ kho thuốc & in đơn thuốc | Khi bấm 'Lưu & Xuất thuốc', duyệt `ChiTietDonThuoc` gọi `KhoHangBUS.xuatKho()` và xuất phiếu in. | Tự động trừ kho thuốc hoàn toàn khép kín. |
| **SS-09** | 26/10/2026 | Phác đồ | Sửa lỗi tạo phác đồ mới bị điền dữ liệu cũ | Kiểm tra logic tạo phác đồ, khi tạo mới không được nạp dữ liệu của phác đồ đầu tiên. | Đã xóa hardcode index 0, form tạo mới hiển thị placeholder. |
| **SS-10** | 26/10/2026 | Phác đồ | Phân tích ảnh chụp màn hình lỗi xung đột UI | Gửi ảnh chụp modal theo dõi diễn tiến: "UI đang bị xung đột quá, chỉnh lại model này rõ ràng ra". | AI chỉ ra 5 nguyên nhân gốc rễ gây xung đột layout. |
| **SS-11** | 26/10/2026 | Phác đồ | Tái thiết kế Modal theo dõi diễn tiến | Tái cấu trúc dialog thành 3 Card độc lập, Slider % tiến độ động, thay JList bằng JTable 6 cột. | Khắc phục hoàn toàn lỗi đè chữ, giao diện chuyên nghiệp. |

---

## 3. CHI TIẾT TỪNG PHIÊN LÀM VIỆC (PROMPT – KẾT QUẢ AI – THẨM ĐỊNH)

---

### PHẦN I: PHÂN HỆ QUẢN LÝ THÚ CƯNG (PET MANAGEMENT)

#### Phiên 1: Thiết kế giao diện danh sách thú cưng, Avatar bo tròn & Huy hiệu sức khỏe
* **Mã phiên:** `SS-01` | **Ngày:** 11/10/2026
* **Mục tiêu:** Tạo bảng danh sách thú cưng có ảnh đại diện bo tròn, huy hiệu loài (Chó/Mèo) và trạng thái sức khỏe bằng Java Swing thuần không phụ thuộc thư viện ngoài.

> **Prompt của Sinh viên:**
> ```text
> Hãy viết giao diện ThuCungPanel.java hiển thị bảng danh sách bệnh nhân thú cưng. 
> Yêu cầu:
> 1. Sử dụng Java 21, Swing thuần, màu sắc lấy từ Theme.java.
> 2. Cột Thú cưng hiển thị Avatar bo tròn (nếu không có ảnh thì vẽ avatar mặc định kèm tên và mã TC).
> 3. Cột Giống loài và Tình trạng sức khỏe sử dụng các Badge bo góc có màu nền nhẹ và chữ đậm (Khỏe mạnh: xanh lá, Đang điều trị: cam, Theo dõi: xanh dương).
> 4. Có thanh tìm kiếm theo tên thú cưng hoặc tên chủ nuôi.
> ```

* **Kết quả AI trả về (Trích đoạn mã nguồn tiêu biểu):**
```java
// AI thiết kế Avatar bo tròn tự vẽ bằng Java 2D Graphics
private static class PetAvatar extends JComponent {
    private final Image img;
    PetAvatar(Image img) { this.img = img; setPreferredSize(new Dimension(36, 36)); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.AVATAR_BG);
        g2.fillOval(0, 0, 36, 36);
        if (img != null) {
            g2.setClip(new Ellipse2D.Float(0, 0, 36, 36));
            g2.drawImage(img, 0, 0, 36, 36, null);
        }
        g2.dispose();
    }
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Biên dịch chạy thử trên Eclipse, bảng danh sách render mượt mà, avatar hiển thị chuẩn hình tròn không bị méo.
  * **Đánh giá:** Tốt 100%, đáp ứng đầy đủ yêu cầu thẩm mỹ phẳng hiện đại.

---

#### Phiên 2: Tích hợp menu ngữ cảnh chuột phải mở nhanh Kê đơn & Phác đồ
* **Mã phiên:** `SS-02` | **Ngày:** 12/10/2026
* **Mục tiêu:** Bổ sung menu chuột phải (JPopupMenu) trên mỗi dòng thú cưng để bác sĩ click mở thẳng giao diện Kê đơn hoặc Lập phác đồ điều trị cho thú cưng đó.

> **Prompt của Sinh viên:**
> ```text
> Trong ThuCungPanel.java, hãy bổ sung menu chuột phải (JPopupMenu) khi click vào một hàng thú cưng.
> Menu gồm các chức năng: 'Xem chi tiết', 'Chỉnh sửa', 'Kê đơn thuốc' và 'Lập phác đồ điều trị'.
> Khi chọn 'Kê đơn thuốc' hoặc 'Lập phác đồ điều trị', hãy gọi hàm tương ứng trên MainFrame để chuyển tab và truyền thẳng đối tượng ThuCung đã chọn vào form.
> ```

* **Kết quả AI trả về:**
```java
// AI bổ sung JMenuItem và kết nối ngữ cảnh thông qua MainFrame
JMenuItem itKeDon = new JMenuItem("Kê đơn thuốc");
itKeDon.setFont(Theme.font(Font.PLAIN, 13));
itKeDon.setIcon(Icons.of("prescription", 16, Theme.ACCENT));
itKeDon.addActionListener(e -> {
    Window w = SwingUtilities.getWindowAncestor(this);
    if (w instanceof MainFrame mf) {
        mf.moKeDonChoThuCung(tc); // Chuyển sang DonThuocPanel và điền sẵn thú cưng tc
    }
});

JMenuItem itPhacDo = new JMenuItem("Lập phác đồ điều trị");
itPhacDo.setFont(Theme.font(Font.PLAIN, 13));
itPhacDo.setIcon(Icons.of("protocol", 16, Theme.ACCENT));
itPhacDo.addActionListener(e -> {
    Window w = SwingUtilities.getWindowAncestor(this);
    if (w instanceof MainFrame mf) {
        mf.moPhacDoChoThuCung(tc); // Chuyển sang PhacDoPanel và điền sẵn thú cưng tc
    }
});
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Thử nhấp chuột phải vào thú cưng "Milo (Chó Poodle)", chọn "Kê đơn thuốc", màn hình lập tức chuyển sang giao diện Kê đơn với thông tin bệnh nhân Milo được điền sẵn.
  * **Đánh giá:** Luồng người dùng (UX flow) vô cùng tiện lợi, giảm thiểu thao tác nhập liệu lặp lại cho bác sĩ.

---

### PHẦN II: PHÂN HỆ QUẢN LÝ KHO HÀNG (INVENTORY MANAGEMENT)

#### Phiên 3: Xây dựng giao diện Kho hàng & Cảnh báo tồn kho thấp
* **Mã phiên:** `SS-03` | **Ngày:** 11/10/2026
* **Mục tiêu:** Tạo giao diện theo dõi kho dược phẩm, phân loại danh mục, tự động gắn nhãn cảnh báo đỏ khi số lượng tồn < 10.

> **Prompt của Sinh viên:**
> ```text
> Thiết kế giao diện KhoHangPanel.java gồm 3 thẻ thống kê trên đầu (Tổng mặt hàng, Tồn kho an toàn, Cảnh báo sắp hết).
> Bảng danh sách hiển thị SKU, Tên thuốc/vật tư, Danh mục, Quy cách, Tồn kho, Đơn vị tính, Giá bán, Trạng thái.
> Nếu tồn kho <= 10, hiển thị huy hiệu Warning màu cam/đỏ 'Sắp hết hàng'.
> ```

* **Kết quả AI trả về:**
  * Sinh mã nguồn [`KhoHangPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/KhoHangPanel.java) với bố cục `BorderLayout`: `top` chứa các thẻ KPI thống kê, `center` chứa `JTable` phân trang, bộ lọc danh mục và ô tìm kiếm nhanh.
  * Tùy biến `StockCell` tự động kiểm tra số lượng và hiển thị icon cảnh báo tam giác nếu tồn thấp.

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Thử nghiệm lọc các mặt hàng thuốc kháng sinh, danh sách hiển thị đúng giá bán và tồn kho.
  * **Đánh giá:** Giao diện trực quan, đạt chuẩn thiết kế tham chiếu.

---

#### Phiên 4: Xây dựng hàm trừ tồn kho tự động `xuatKho()` & Cơ chế Observer Pattern
* **Mã phiên:** `SS-04` | **Ngày:** 20/10/2026
* **Mục tiêu:** Cho phép các phân hệ khác (Kê đơn thuốc, Hóa đơn) gọi xuất kho làm giảm số lượng tồn kho tự động và thông báo cho giao diện kho làm mới số liệu ngay lập tức.

> **Prompt của Sinh viên:**
> ```text
> Trong KhoHangBUS.java, hãy viết hàm xuatKho(String maSKU, int soLuongXuat):
> - Kiểm tra mã SKU và số lượng xuất hợp lệ.
> - Trừ số lượng tồn kho: tonMoi = tonCu - soLuongXuat. Nếu về 0 thì tự chuyển trạng thái sang 'Tạm hết hàng'.
> - Xây dựng cơ chế listener (Observer Pattern) có hàm addListener(Runnable) và notifyListeners() để khi xuất kho thì giao diện KhoHangPanel tự động refresh lại mà không cần load lại ứng dụng.
> ```

* **Kết quả AI trả về:**
```java
// Trong KhoHangBUS.java:
private final List<Runnable> listeners = new ArrayList<>();

public void addListener(Runnable r) {
    if (r != null && !listeners.contains(r)) listeners.add(r);
}

public void notifyListeners() {
    for (Runnable r : listeners) {
        try { r.run(); } catch (Exception ignored) {}
    }
}

public boolean xuatKho(String maSKU, int soLuongXuat) {
    if (maSKU == null || maSKU.trim().isEmpty() || soLuongXuat <= 0) return false;
    for (SanPham sp : ds) {
        if (sp.getMaSKU().equalsIgnoreCase(maSKU.trim())) {
            int tonMoi = Math.max(0, sp.getSoLuongTon() - soLuongXuat);
            sp.setSoLuongTon(tonMoi);
            if (tonMoi == 0) sp.setTrangThai("Tạm hết hàng");
            notifyListeners(); // Kích hoạt sự kiện cập nhật giao diện
            return true;
        }
    }
    return false;
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Đăng ký `bus.addListener(this::refresh)` trong `KhoHangPanel.java`. Khi kê đơn thuốc xuất 5 lọ thuốc, quay lại tab Kho hàng thấy số lượng tồn đã giảm chính xác từ 20 xuống 15.
  * **Đánh giá:** Cơ chế hoạt động hoàn hảo, đảm bảo tính nhất quán dữ liệu giữa Kho và Đơn thuốc.

---

#### Phiên 5: Xây dựng thành phần cuộn lịch động `CalendarPicker` bằng Swing thuần
* **Mã phiên:** `SS-05` | **Ngày:** 13/10/2026
* **Mục tiêu:** Tạo hộp thoại chọn ngày tháng trực quan cho bộ lọc ngày xuất nhập kho mà không cần nạp thêm thư viện bên ngoài như JCalendar.

> **Prompt của Sinh viên:**
> ```text
> Hãy viết 1 class CalendarPicker.java bằng Swing thuần:
> - Hiển thị dạng popup cuộn lịch tháng.
> - Có 2 nút bấm chuyển tháng trước / tháng sau (<, >) và combobox chọn năm.
> - Lưới hiển thị các ngày trong tuần (T2 đến CN), ngày hiện tại được highlight viền accent.
> - Khi người dùng click vào một ngày, gọi callback trả về LocalDate và tự đóng popup.
> ```

* **Kết quả AI trả về:**
  * Sinh toàn bộ file [`CalendarPicker.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/CalendarPicker.java) với `JDialog` không viền hoặc `JPopupMenu`, sử dụng `java.time.LocalDate` để tính toán ngày đầu tháng và thứ trong tuần.
  * Hỗ trợ gán trực tiếp vào các ô `JTextField` ngày tháng chỉ với 1 dòng lệnh.

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Đã tích hợp vào `KhoHangPanel` và `LichSuNhapXuatPanel`. Người dùng bấm vào icon lịch là bảng lịch hiện ra chọn ngày rất nhạy.
  * **Đánh giá:** Tuyệt vời, giải quyết dứt điểm nhu cầu chọn ngày mà không cần thêm file jar cồng kềnh.

---

### PHẦN III: PHÂN HỆ QUẢN LÝ ĐƠN THUỐC (PRESCRIPTION MANAGEMENT)

#### Phiên 6: Khắc phục lỗi form kê đơn tự động điền sẵn dữ liệu thú cưng & thuốc index 0
* **Mã phiên:** `SS-06` | **Ngày:** 26/10/2026
* **Mục tiêu:** Sửa lỗi logic khi bấm nút "Thêm đơn thuốc mới": form tự động lấy thú cưng đầu tiên và dòng thuốc Clavamox 125mg điền sẵn vào bảng thay vì để trống.

> **Prompt của Sinh viên:**
> ```text
> "điều chỉnh lại chỗ thêm phác đồ thì lại là thành phần chỉnh sửa và có sẵn dữ liệu của cái đầu, kiểm tra lại toàn bộ logic quản lý đơn thuốc với tạo phác đồ điều trị rồi chỉnh sửa cho chuẩn"
> ```

* **Kết quả AI trả về:**
  * **Phân tích nguyên nhân:** Trong `DonThuocPanel.java`, hàm mở dialog dùng chung cho cả tạo mới và chỉnh sửa, biến khởi tạo bị hardcode lấy `dsThuCung.get(0)` và nạp sẵn 1 dòng thuốc mẫu.
  * **Giải pháp khắc phục:**
    ```java
    // AI bổ sung cờ phân định và reset trạng thái form sạch (Clean State)
    boolean isEdit = (editTarget != null && editTarget.getMaDonThuoc() != null);
    if (!isEdit && preSelectedTC == null) {
        cbThuCung.addItem("-- Chọn thú cưng điều trị --");
        cbThuCung.setSelectedIndex(0);
        txtChanDoan.setText("");
        txtTrieuChung.setText("");
        // Bảng chi tiết thuốc khởi tạo rỗng 0 dòng
        dsChiTiet.clear();
        tableModelChiTiet.fireTableDataChanged();
    }
    ```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Mở form kê đơn mới, form hoàn toàn trống sạch sẽ, hiển thị placeholder rõ ràng.
  * **Đánh giá:** Lỗi đã được khắc phục triệt để.

---

#### Phiên 7: Tạo Modal 'Chọn thuốc từ kho hàng' & Kiểm tra tồn kho thời gian thực
* **Mã phiên:** `SS-07` | **Ngày:** 26/10/2026
* **Mục tiêu:** Thay vì bắt bác sĩ phải tự gõ tên thuốc và giá tiền bằng tay, cho phép bấm nút `+ Chọn thuốc từ kho` để chọn trực tiếp thuốc từ `KhoHangBUS`.

> **Prompt của Sinh viên:**
> ```text
> Trong dialog kê đơn thuốc của DonThuocPanel.java, hãy thêm nút '+ Chọn thuốc từ kho'.
> Khi bấm nút này:
> 1. Mở một dialog con hiển thị bảng danh sách các thuốc đang có số lượng tồn > 0 từ kho hàng.
> 2. Có ô tìm kiếm theo tên thuốc hoặc mã SKU.
> 3. Khi chọn 1 thuốc và bấm 'Thêm vào đơn', tự động điền Mã thuốc, Tên thuốc, Đơn vị tính và Đơn giá vào bảng chi tiết đơn thuốc, đồng thời giới hạn số lượng kê không được vượt quá số lượng tồn kho.
> ```

* **Kết quả AI trả về:**
  * Sinh phương thức `moDialogChonThuocTuKho()` với bảng danh sách thuốc trực quan.
  * Tự động kiểm tra: Nếu thuốc đã có trong đơn thì tăng số lượng lên 1; nếu chưa có thì thêm dòng mới.
  * Thêm kiểm tra hợp lệ: `if (soLuongKe > tonKho) { JOptionPane.showMessageDialog(..., "Số lượng kê vượt quá tồn kho hiện có!"); }`.

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Thao tác chọn thuốc Clavamox, thuốc tự động điền vào bảng với đơn giá 15.000₫ và ĐVT "Viên", tổng tiền đơn thuốc tự tính lại tức thì.
  * **Đánh giá:** Tính năng hoạt động cực kỳ chính xác và tiện lợi.

---

#### Phiên 8: Tự động trừ kho thuốc khi hoàn tất đơn & Xuất phiếu in đơn thuốc
* **Mã phiên:** `SS-08` | **Ngày:** 26/10/2026
* **Mục tiêu:** Khi bác sĩ hoàn tất kê đơn và lưu đơn thuốc, hệ thống tự động trừ kho hàng và cung cấp chức năng in đơn thuốc cho chủ nuôi.

> **Prompt của Sinh viên:**
> ```text
> Hãy liên kết DonThuocBUS với KhoHangBUS: khi lưu đơn thuốc ở trạng thái 'Đã xuất thuốc', tự động duyệt danh sách ChiTietDonThuoc để trừ số lượng tồn trong kho hàng.
> Bổ sung chức năng in đơn thuốc (sử dụng PrinterJob của Java AWT hoặc xuất dialog xem trước bản in).
> ```

* **Kết quả AI trả về:**
```java
// Trong logic xử lý lưu đơn thuốc:
for (ChiTietDonThuoc ct : donThuocMoi.getDsThuoc()) {
    boolean ok = khoHangBus.xuatKho(ct.getMaThuoc(), ct.getSoLuong());
    if (!ok) {
        System.err.println("Cảnh báo: Không thể trừ kho cho thuốc: " + ct.getTenThuoc());
    }
}
// Chức năng in đơn thuốc:
private void inDonThuoc(DonThuoc dt) {
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setJobName("In Đơn Thuốc - " + dt.getMaDonThuoc());
    // Hiển thị dialog in chuẩn của hệ điều hành
    if (job.printDialog()) {
        try { job.print(); } catch (Exception ex) { ... }
    }
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Kê đơn 2 mặt hàng thuốc, sau khi lưu, kiểm tra tab Kho hàng thấy cả 2 mặt hàng đều bị trừ đúng số lượng. Nút in đơn thuốc mở được hộp thoại máy in của Windows.
  * **Đánh giá:** Hệ thống hoạt động trơn tru, khép kín.

---

### PHẦN IV: PHÂN HỆ PHÁC ĐỒ ĐIỀU TRỊ (TREATMENT PROTOCOL & CLINICAL DIARY)

#### Phiên 9: Khắc phục lỗi tạo phác đồ mới bị điền sẵn dữ liệu của phác đồ cũ
* **Mã phiên:** `SS-09` | **Ngày:** 26/10/2026
* **Mục tiêu:** Tương tự như đơn thuốc, sửa lỗi khi bấm "Tạo phác đồ điều trị mới" thì modal lại hiển thị sẵn dữ liệu của phác đồ đầu tiên.

> **Prompt của Sinh viên:**
> ```text
> "điều chỉnh lại chỗ thêm phác đồ thì lại là thành phần chỉnh sửa và có sẵn dữ liệu của cái đầu"
> ```

* **Kết quả AI trả về:**
  * Rà soát `moDialogLapPhacDo(PhacDoDieuTri editTarget, ThuCung preSelectedTC)` trong `PhacDoPanel.java`.
  * Sửa biến kiểm tra `boolean isEdit = (editTarget != null && editTarget.getMaPhacDo() != null && !editTarget.getMaPhacDo().isEmpty());`.
  * Nếu không phải `isEdit`, đặt tất cả trường Chẩn đoán, Mục tiêu, Dinh dưỡng về rỗng, tạo mã phác đồ mới tự động tăng (`PD-xxx`) và danh sách giai đoạn điều trị ở trạng thái mặc định chưa hoàn thành.

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Bấm "Thêm phác đồ mới", form hoàn toàn trắng sạch, sẵn sàng cho bác sĩ nhập liệu ca bệnh mới.
  * **Đánh giá:** Đạt yêu cầu.

---

#### Phiên 10: Phân tích ảnh chụp màn hình lỗi xung đột giao diện (UI Overlap / Collision)
* **Mã phiên:** `SS-10` | **Ngày:** 26/10/2026
* **Mục tiêu:** Xử lý lỗi giao diện chồng chéo nghiêm trọng trên Modal "Theo dõi diễn tiến: PD-001 (Milo)" (ảnh chụp do người dùng cung cấp).

> **Prompt của Sinh viên (Đính kèm ảnh màn hình):**
> ```text
> "UI đang bị xung đột quá, chỉnh lại model này rõ ràng ra"
> ```

* **Kết quả phân tích từ AI sau khi đọc ảnh chụp màn hình:**
  1. **Kích thước modal quá chật (`620 x 560`):** Không đủ không gian hiển thị cho slider + combobox trạng thái + 4 ô nhập lâm sàng + danh sách nhật ký JList + các nút bấm.
  2. **Lỗi chia lưới `GridLayout(4, 2)`:** Chỉ có 4 component được add vào lưới 4x2 dẫn tới việc Swing ép thành 1 cột và kéo dãn chiều dọc quá đà.
  3. **Xung đột `BorderLayout`:** `pNewLog` đặt ở `NORTH`, nhãn ở `CENTER` và `spList` (cao 140px) đặt ở `SOUTH`. Khi chiều cao nội dung vượt khung, `CENTER` và `SOUTH` bị sụp xuống và vẽ đè trực tiếp lên nhau.
  4. **Highlight của `JList` đè lên nhãn:** Dòng đầu tiên của JList có dải chọn màu xanh vẽ đè bẹp lên chữ *"Chi tiết ghi nhận:"*.

* **Thẩm định của Sinh viên:**
  * Nhận định của AI hoàn toàn chính xác 100% với hiện trạng thực tế trên màn hình máy tính của sinh viên.

---

#### Phiên 11: Tái thiết kế Modal theo dõi diễn tiến thành 3 Card độc lập & JTable nhật ký 6 cột
* **Mã phiên:** `SS-11` | **Ngày:** 26/10/2026
* **Mục tiêu:** Viết lại toàn bộ hàm `moDialogTheoDoiTienDo()` trong `PhacDoPanel.java` để giao diện rộng rãi, hiện đại, phân thẻ rõ ràng và triệt tiêu vĩnh viễn nguy cơ xung đột layout.

> **Prompt của Sinh viên:**
> ```text
> Hãy viết lại toàn bộ moDialogTheoDoiTienDo trong PhacDoPanel.java:
> 1. Tăng kích thước dialog lên 840x700 (min 800x640), bọc toàn bộ body trong JScrollPane mượt mà để không bao giờ bị vỡ khung.
> 2. Có Header Banner hiển thị rõ thông tin thú cưng (Mã phác đồ, tên pet, giống, cân nặng, tên chủ nuôi) và badge trạng thái.
> 3. Card 1 (Tiến độ & Trạng thái): Slider 0-100% hiển thị live nhãn %; kéo tới 100% thì tự đổi trạng thái sang 'Hoàn thành'.
> 4. Card 2 (Ghi nhận lâm sàng): 1 hàng 3 cột (Thân nhiệt, Cân nặng, Đánh giá) và 1 hàng ô Chi tiết triệu chứng rộng rãi.
> 5. Card 3 (Lịch sử nhật ký): Bỏ JList chuỗi ghép nối, thay bằng JTable 6 cột chuẩn: Thời gian, Bác sĩ, Thân nhiệt, Cân nặng, Đánh giá, Chi tiết ghi nhận.
> ```

* **Kết quả AI trả về (Đoạn mã cấu trúc 3 Card):**
```java
// Card 1: Slider % Tiến độ + Trạng thái
JPanel card1 = new JPanel(new BorderLayout(0, 10));
slider.addChangeListener(e -> {
    int val = slider.getValue();
    lblPct.setText(val + "%");
    if (val == 100) cbTrangThai.setSelectedItem(PhacDoDieuTri.HOAN_THANH);
});

// Card 2: Lưới lâm sàng 3 cột cân đối
JPanel pRowVitals = new JPanel(new GridLayout(1, 3, 16, 0));
pRowVitals.add(formRow("Thân nhiệt (°C):", txtNhietDo));
pRowVitals.add(formRow("Cân nặng đo được (kg):", txtCanNang));
pRowVitals.add(formRow("Đánh giá tiến triển:", cbDanhGia));

// Card 3: JTable 6 cột chuyên nghiệp thay thế JList thô sơ
String[] cols = {"Thời gian", "Bác sĩ", "Thân nhiệt", "Cân nặng", "Đánh giá", "Chi tiết ghi nhận"};
DefaultTableModel diaryModel = new DefaultTableModel(cols, 0);
JTable tableDiary = new JTable(diaryModel);
tableDiary.setRowHeight(28);
// Định dạng so le zebra striping và căn giữa các chỉ số
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * **Kiểm thử:** Biên dịch `javac --release 21`, chạy ứng dụng, mở phác đồ của bé Milo và bấm cập nhật tiến độ.
  * **Đánh giá thực tế:** Giao diện hiển thị cực kỳ đẹp mắt, rộng rãi, 3 thẻ Card phân cách rõ ràng, bảng nhật ký 6 cột ngay ngắn, thanh kéo phần trăm mượt mà, **không còn bất kỳ hiện tượng đè chữ hay xung đột giao diện nào!**

---

## 4. TỔNG KẾT HIỆU QUẢ VÀ KINH NGHIỆM PROMPT ENGINEERING

### 4.1. Hiệu quả định lượng trên 4 phân hệ:
* **Thời gian thực hiện:** Giảm từ dự kiến **40 – 50 giờ** xuống chỉ còn **8 – 10 giờ làm việc thực tế** (Nhanh hơn 450%).
* **Chất lượng mã nguồn:** Tách bạch 100% theo kiến trúc 3 lớp (`Entity`, `DAO`, `BUS`, `GUI`), không bị trộn lẫn mã giao diện vào logic xử lý dữ liệu.
* **Xử lý xung đột UI:** Nhờ kỹ thuật *Multimodal Prompting (gửi ảnh chụp màn hình)*, AI đã xác định đúng 100% nguyên nhân gốc rễ và đề xuất giải pháp Card Layout triệt tiêu hoàn toàn lỗi layout của Swing.

### 4.2. Bài học kinh nghiệm cốt lõi khi dùng AI:
1. **Prompt phải có ngữ cảnh và ràng buộc:** Khi yêu cầu AI viết code Java Swing, luôn phải nói rõ phiên bản Java (`Java 21`), phong cách UI (`Flat UI, Graphics2D`), và không được dùng thư viện lạ.
2. **Luôn kiểm soát trạng thái dữ liệu (State Management):** Form tạo mới phải luôn có hàm dọn sạch (`resetForm`), tránh việc AI tái sử dụng biến dẫn đến lỗi prefilled data cũ.
3. **Người lập trình giữ vai trò trọng tài quyết định:** AI rất mạnh về sinh cú pháp và thuật toán, nhưng việc kết nối các phân hệ (Thú cưng ➔ Phác đồ ➔ Đơn thuốc ➔ Trừ kho) đòi hỏi sinh viên phải tư duy logic nghiệp vụ thực tế để hướng dẫn AI thực hiện chính xác.

---
*Tài liệu được lập và lưu trữ chính thức trong cây thư mục dự án.*  
**Sinh viên thực hiện:** Nguyễn Ngọc Toàn  
**Đề án:** Hệ thống Quản lý Phòng khám Thú y (PetCare POS)

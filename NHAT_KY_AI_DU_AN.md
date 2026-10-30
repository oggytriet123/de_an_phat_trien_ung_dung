# BÁO CÁO NHẬT KÝ SỬ DỤNG TRÍ TUỆ NHÂN TẠO (AI USAGE & PROMPT LOGBOOK)
## PHÂN HỆ: QUẢN LÝ THÚ CƯNG, KHO HÀNG, ĐƠN THUỐC & PHÁC ĐỒ ĐIỀU TRỊ
### ĐỀ ÁN PHÁT TRIỂN ỨNG DỤNG: HỆ THỐNG QUẢN LÝ PHÒNG KHÁM THÚ Y (PETCARE POS)

---

> **Học phần:** Đề án Phát triển Ứng dụng  
> **Dự án:** Hệ thống Quản lý Phòng khám Thú y & Pet Shop (PetCare POS)  
> **Phân hệ phụ trách:**  
> 1. Quản lý Thú cưng ([`ThuCungPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/ThuCungPanel.java), [`ThuCungBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/ThuCungBUS.java))  
> 2. Quản lý Kho hàng & Lịch sử xuất nhập ([`KhoHangPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/KhoHangPanel.java), [`KhoHangBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/KhoHangBUS.java), [`CalendarPicker.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/CalendarPicker.java))  
> 3. Quản lý Đơn thuốc y tế ([`DonThuocPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/DonThuocPanel.java), [`DonThuocBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/DonThuocBUS.java))  
> 4. Lập & Theo dõi Phác đồ Điều trị ([`PhacDoPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/PhacDoPanel.java), [`PhacDoBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/PhacDoBUS.java))  
> **Sinh viên thực hiện:** Nguyễn Ngọc Toàn  
> **Nền tảng & Ngôn ngữ:** Java 21 • Java Swing Custom UI (Graphics2D) • Microsoft SQL Server (JDBC)  
> **Trợ lý AI sử dụng:** Antigravity AI (Google DeepMind) • ChatGPT (GPT-4o) / Claude  
> **Kho lưu trữ GitHub:** [oggytriet123/de_an_phat_trien_ung_dung](https://github.com/oggytriet123/de_an_phat_trien_ung_dung) (Nhánh: `ngoc-toan`)  
> **Thời gian thực hiện:** Tháng 10/2026  

---

## MỤC LỤC
1. [TỔNG QUAN PHƯƠNG PHÁP & NGUYÊN TẮC GHI NHẬT KÝ AI](#1-tổng-quan-phương-pháp--nguyên-tắc-ghi-nhật-ký-ai)
2. [BẢNG TỔNG HỢP CÁC PHIÊN PROMPT & KẾT QUẢ THEO 4 PHÂN HỆ](#2-bảng-tổng-hợp-các-phiên-prompt--kết-quả-theo-4-phân-hệ)
3. [CHI TIẾT 10 PHIÊN LÀM VIỆC CỤ THỂ BÁM SÁT MÃ NGUỒN DỰ ÁN](#3-chi-tiết-10-phiên-làm-việc-cụ-thể-bám-sát-mã-nguồn-dự-án)
   - [PHẦN I: PHÂN HỆ QUẢN LÝ THÚ CƯNG (PET MANAGEMENT)](#phần-i-phân-hệ-quản-lý-thú-cưng-pet-management)
     - [Phiên 1: Xây dựng giao diện ThuCungPanel, 4 thẻ KPI & Avatar tròn khử răng cưa](#phiên-1-xây-dựng-giao-diện-thucungpanel-4-thẻ-kpi--avatar-tròn-khử-răng-cưa)
     - [Phiên 2: Tích hợp menu ngữ cảnh chuột phải mở nhanh Kê đơn & Phác đồ điều trị](#phiên-2-tích-hợp-menu-ngữ-cảnh-chuột-phải-mở-nhanh-kê-đơn--phác-đồ-điều-trị)
   - [PHẦN II: PHÂN HỆ QUẢN LÝ KHO HÀNG (INVENTORY MANAGEMENT)](#phần-ii-phân-hệ-quản-lý-kho-hàng-inventory-management)
     - [Phiên 3: Xây dựng KhoHangPanel, bảng dược phẩm & Huy hiệu cảnh báo tồn kho thấp](#phiên-3-xây-dựng-khohangpanel-bảng-dược-phẩm--huy-hiệu-cảnh-báo-tồn-kho-thấp)
     - [Phiên 4: Viết hàm trừ tồn kho tự động xuatKho() & Observer Pattern đồng bộ dữ liệu](#phiên-4-viết-hàm-trừ-tồn-kho-tự-động-xuatkho--observer-pattern-đồng-bộ-dữ-liệu)
     - [Phiên 5: Xây dựng thành phần cuộn lịch CalendarPicker.java bằng Swing thuần](#phiên-5-xây-dựng-thành-phần-cuộn-lịch-calendarpickerjava-bằng-swing-thuần)
   - [PHẦN III: PHÂN HỆ QUẢN LÝ ĐƠN THUỐC (PRESCRIPTION MANAGEMENT)](#phần-iii-phân-hệ-quản-lý-đơn-thuốc-prescription-management)
     - [Phiên 6: Sửa lỗi form kê đơn tự điền sẵn dữ liệu thú cưng & thuốc index 0](#phiên-6-sửa-lỗi-form-kê-đơn-tự-điền-sẵn-dữ-liệu-thú-cưng--thuốc-index-0)
     - [Phiên 7: Tạo chức năng '+ Chọn thuốc từ kho' & Tính tổng tiền tự động](#phiên-7-tạo-chức-năng--chọn-thuốc-từ-kho--tính-tổng-tiền-tự-động)
     - [Phiên 8: Cấp phát thuốc tự động trừ kho hàng và tích hợp in đơn thuốc](#phiên-8-cấp-phát-thuốc-tự-động-trừ-kho-hàng-và-tích-hợp-in-đơn-thuốc)
   - [PHẦN IV: PHÂN HỆ PHÁC ĐỒ ĐIỀU TRỊ (TREATMENT PROTOCOL & CLINICAL DIARY)](#phần-iv-phân-hệ-phác-đồ-điều-trị-treatment-protocol--clinical-diary)
     - [Phiên 9: Sửa lỗi tạo phác đồ mới bị nạp nhầm dữ liệu phác đồ cũ](#phiên-9-sửa-lỗi-tạo-phác-đồ-mới-bị-nạp-nhầm-dữ-liệu-phác-đồ-cũ)
     - [Phiên 10: Xử lý triệt để lỗi xung đột UI Modal Theo dõi diễn tiến (Phân tích ảnh chụp & Tái thiết kế 3 Card)](#phiên-10-xử-lý-triệt-để-lỗi-xung-đột-ui-modal-theo-dõi-diễn-tiến-phân-tích-ảnh-chụp--tái-thiết-kế-3-card)
4. [TỔNG KẾT BÀI HỌC KINH NGHIỆM PROMPT ENGINEERING](#4-tổng-kết-bài-học-kinh-nghiệm-prompt-engineering)

---

## 1. TỔNG QUAN PHƯƠNG PHÁP & NGUYÊN TẮC GHI NHẬT KÝ AI

Tài liệu này ghi nhận trung thực và bám sát 100% mã nguồn thực tế của dự án. Mọi phiên làm việc với AI được tổ chức theo cấu trúc tiêu chuẩn học thuật:
$$\mathbf{Mục\ tiêu\ bài\ toán} \longrightarrow \mathbf{Prompt\ câu\ lệnh\ của\ Sinh\ viên} \longrightarrow \mathbf{Mã\ nguồn\ AI\ phản\ hồi} \longrightarrow \mathbf{Thẩm\ định\ \&\ Nghiệm\ thu\ thực\ tế}$$

Sinh viên không phụ thuộc hoàn toàn vào code sinh tự động của AI mà giữ vai trò **Human-in-the-loop**: kiểm tra tính đóng gói, liên kết dữ liệu giữa các module (Thú cưng ➔ Phác đồ ➔ Đơn thuốc ➔ Trừ kho), biên dịch với `javac --release 21` và chạy thử giao diện thực tế.

---

## 2. BẢNG TỔNG HỢP CÁC PHIÊN PROMPT & KẾT QUẢ THEO 4 PHÂN HỆ

| Mã phiên | Ngày thực hiện | Phân hệ | Nhiệm vụ kỹ thuật | Tóm tắt Prompt thực tế | Kết quả nghiệm thu |
| :---: | :---: | :--- | :--- | :--- | :--- |
| **SS-01** | 11/10/2026 | Thú cưng | Bảng danh sách bệnh nhân & Avatar | Viết `ThuCungPanel.java` có 4 thẻ KPI, Avatar tròn khử răng cưa và Badge tình trạng sức khỏe. | Đạt 100%, render mượt mà bằng Java 2D Graphics. |
| **SS-02** | 12/10/2026 | Thú cưng | Lối tắt chuột phải mở Kê đơn & Phác đồ | Thêm menu ngữ cảnh chuột phải trên `JTable` để gọi `moKeDonChoThuCung()` và `moPhacDoChoThuCung()`. | Bác sĩ thao tác 1-click mở form điền sẵn thông tin thú cưng. |
| **SS-03** | 11/10/2026 | Kho hàng | Quản lý dược phẩm, cảnh báo tồn kho | Xây dựng `KhoHangPanel.java` có thẻ KPI, bộ lọc danh mục và huy hiệu cảnh báo tồn kho thấp <= 10. | Bảng kho phân loại rõ ràng, giao diện phẳng chuẩn SaaS. |
| **SS-04** | 20/10/2026 | Kho hàng | Hàm trừ tồn kho tự động & Observer Pattern | Viết hàm `xuatKho()` trong `KhoHangBUS` và `addListener` để `KhoHangPanel` tự refresh khi xuất đơn. | Trừ kho tự động, bảng kho cập nhật thời gian thực. |
| **SS-05** | 13/10/2026 | Kho hàng | Cuộn lịch pop-up chọn ngày `CalendarPicker` | Viết `CalendarPicker.java` bằng Swing thuần để chọn ngày cho bộ lọc kho và phiếu nhập. | Hoạt động độc lập, không phụ thuộc thư viện bên ngoài. |
| **SS-06** | 26/10/2026 | Đơn thuốc | Sửa lỗi form kê đơn tự điền dữ liệu index 0 | *"điều chỉnh lại chỗ thêm phác đồ... có sẵn dữ liệu của cái đầu, kiểm tra lại toàn bộ logic đơn thuốc..."* | Tách bạch `isEdit` và `isCreate`, form tạo mới rỗng 100%. |
| **SS-07** | 26/10/2026 | Đơn thuốc | Nút '+ Chọn thuốc từ kho' & Tính tổng tiền | Tạo nút chọn thuốc từ `KhoHangBUS`, lấy đơn giá, ĐVT và tự động tính tổng tiền thuốc khi đổi số lượng. | Bác sĩ chọn thuốc trong 3 giây, chặn kê vượt tồn kho. |
| **SS-08** | 26/10/2026 | Đơn thuốc | Cấp phát thuốc tự động trừ kho & In đơn | Viết `capPhatThuoc(maDT, khoHangBus)` duyệt danh sách trừ kho thuốc và in đơn qua `PrinterJob`. | Đồng bộ 100% giữa Đơn thuốc và Kho hàng. |
| **SS-09** | 26/10/2026 | Phác đồ | Sửa lỗi tạo phác đồ mới bị nạp phác đồ cũ | Sửa logic `moDialogLapPhacDo`: tạo mới không được lấy phác đồ đầu tiên nạp vào form. | Form tạo phác đồ mới sạch 100%, tự tăng mã `PD-xxx`. |
| **SS-10** | 26/10/2026 | Phác đồ | Khắc phục xung đột UI Modal Theo dõi diễn tiến | *"UI đang bị xung đột quá, chỉnh lại model này rõ ràng ra" (Kèm ảnh chụp màn hình modal lỗi)* | Phân tích 5 lỗi layout, tái thiết kế thành 3 Card và JTable 6 cột. |

---

## 3. CHI TIẾT 10 PHIÊN LÀM VIỆC CỤ THỂ BÁM SÁT MÃ NGUỒN DỰ ÁN

---

### PHẦN I: PHÂN HỆ QUẢN LÝ THÚ CƯNG (PET MANAGEMENT)

#### Phiên 1: Xây dựng giao diện ThuCungPanel, 4 thẻ KPI & Avatar tròn khử răng cưa
* **Mã phiên:** `SS-01` | **Ngày:** 11/10/2026
* **Mục tiêu:** Xây dựng màn hình danh sách thú cưng bệnh nhân, hỗ trợ tìm kiếm nhanh, lọc theo loài và render ảnh avatar hình tròn.

> **Prompt câu lệnh thực tế của Sinh viên:**
> ```text
> Trong dự án PetCare POS (Java 21, Swing thuần, dùng bảng màu Theme.java):
> Hãy xây dựng class ThuCungPanel.java để quản lý danh sách bệnh nhân thú cưng.
> Yêu cầu cụ thể:
> 1. Trên cùng có 4 thẻ thống kê (StatCard): Tổng số thú cưng, Số lượng Chó, Số lượng Mèo, Số ca cần theo dõi/cảnh báo.
> 2. Thanh công cụ gồm: Ô tìm kiếm bằng PlaceholderField (tìm theo tên bé, mã PET, giống, tên chủ, SĐT), các chip lọc theo loài (Chó, Mèo, Thỏ, Khác) và nút sắp xếp theo cân nặng, tên A-Z.
> 3. Bảng JTable hiển thị: Mã thú cưng, Tên thú cưng, Loài, Giống loài, Giới tính, Tuổi, Cân nặng, Chủ nuôi, Tình trạng.
> 4. Cột Thú cưng tự vẽ Avatar hình tròn bằng Graphics2D có khử răng cưa (RenderingHints.KEY_ANTIALIASING).
> 5. Cột Tình trạng hiển thị huy hiệu bo góc (Badge): Khỏe mạnh (xanh lá), Đang điều trị (cam), Cần theo dõi (xanh dương).
> 6. Phân trang 7 dòng/trang có nút Trước, Sau và số trang.
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`ThuCungPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/ThuCungPanel.java)):**
```java
// Component Avatar tròn tự vẽ khử răng cưa
private static class PetAvatar extends JComponent {
    private final Image img;
    PetAvatar(Image img) { 
        this.img = img; 
        setPreferredSize(new Dimension(36, 36)); 
    }
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
  * Biên dịch chạy thử trên môi trường thực tế: Avatar thú cưng bo tròn mượt mà, không bị răng cưa.
  * Tìm kiếm theo tên chủ hoặc mã thú cưng phản hồi ngay lập tức (real-time filter).

---

#### Phiên 2: Tích hợp menu ngữ cảnh chuột phải mở nhanh Kê đơn & Phác đồ điều trị
* **Mã phiên:** `SS-02` | **Ngày:** 12/10/2026
* **Mục tiêu:** Tạo lối tắt thông minh: nhấp chuột phải vào thú cưng là lập tức mở form Kê đơn hoặc Lập phác đồ cho thú cưng đó.

> **Prompt câu lệnh thực tế của Sinh viên:**
> ```text
> Trong ThuCungPanel.java:
> 1. Thêm 2 nút 'Kê đơn' và 'Phác đồ' trên thanh nút header cạnh nút 'Xem chi tiết'.
> 2. Bổ sung sự kiện chuột phải trên JTable: khi nhấp chuột phải vào dòng thú cưng nào thì bật JPopupMenu với 2 mục: 'Kê đơn thuốc' và 'Lập phác đồ điều trị'.
> 3. Khi bấm, gọi hàm moKeDonChoThuCung(tc) và moPhacDoChoThuCung(tc) trên MainFrame để chuyển tab và điền sẵn thú cưng tc vào form.
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`ThuCungPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/ThuCungPanel.java#L947-L970)):**
```java
JMenuItem itKeDon = new JMenuItem("Kê đơn thuốc");
itKeDon.setFont(Theme.font(Font.PLAIN, 13));
itKeDon.setIcon(Icons.of("prescription", 16, Theme.ACCENT));
itKeDon.addActionListener(e -> {
    Window w = SwingUtilities.getWindowAncestor(this);
    if (w instanceof MainFrame mf) {
        mf.moKeDonChoThuCung(tc); // Chuyển tab Đơn thuốc & nạp sẵn thú cưng tc
    }
});

JMenuItem itPhacDo = new JMenuItem("Lập phác đồ điều trị");
itPhacDo.setFont(Theme.font(Font.PLAIN, 13));
itPhacDo.setIcon(Icons.of("protocol", 16, Theme.ACCENT));
itPhacDo.addActionListener(e -> {
    Window w = SwingUtilities.getWindowAncestor(this);
    if (w instanceof MainFrame mf) {
        mf.moPhacDoChoThuCung(tc); // Chuyển tab Phác đồ & nạp sẵn thú cưng tc
    }
});
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Nhấp chuột phải vào dòng bé Milo (Chó Poodle, 10.4kg), chọn "Kê đơn thuốc", tab Quản lý đơn thuốc lập tức mở ra với thông tin bệnh nhân Milo và chủ nuôi đã được điền sẵn.
  * Giảm thao tác cho bác sĩ từ 5 bước xuống còn 1 cú click chuột.

---

### PHẦN II: PHÂN HỆ QUẢN LÝ KHO HÀNG (INVENTORY MANAGEMENT)

#### Phiên 3: Xây dựng KhoHangPanel, bảng dược phẩm & Huy hiệu cảnh báo tồn kho thấp
* **Mã phiên:** `SS-03` | **Ngày:** 11/10/2026
* **Mục tiêu:** Tạo bảng theo dõi kho thuốc thú y, vật tư, tự động gắn nhãn cảnh báo khi tồn kho <= 10.

> **Prompt câu lệnh thực tế của Sinh viên:**
> ```text
> Thiết kế giao diện KhoHangPanel.java:
> 1. Trên cùng có 3 thẻ KPI: Tổng mặt hàng, Tồn kho an toàn, Cảnh báo sắp hết.
> 2. Bảng hiển thị: Mã SKU, Tên thuốc/vật tư, Danh mục, Quy cách, Tồn kho, ĐVT, Giá bán, Trạng thái.
> 3. Tự động kiểm tra số lượng tồn:
>    - Nếu tồn <= 0: Hiển thị badge đỏ 'Tạm hết hàng'.
>    - Nếu 1 <= tồn <= 10: Hiển thị badge cam 'Sắp hết ([SL])' kèm icon tam giác cảnh báo.
>    - Nếu > 10: Hiển thị badge xanh 'Đang bán'.
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`KhoHangPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/KhoHangPanel.java)):**
```java
// Custom renderer tự động phát hiện tồn kho thấp
if (sp.getSoLuongTon() <= 0) {
    lblBadge.setText("Tạm hết hàng");
    lblBadge.setBackground(Theme.RED_BG);
    lblBadge.setForeground(Theme.RED);
} else if (sp.getSoLuongTon() <= 10) {
    lblBadge.setText("Sắp hết (" + sp.getSoLuongTon() + ")");
    lblBadge.setBackground(Theme.ORANGE_BG);
    lblBadge.setForeground(Theme.ORANGE);
    lblIcon.setIcon(Icons.of("alert-triangle", 14, Theme.ORANGE));
} else {
    lblBadge.setText("Đang bán");
    lblBadge.setBackground(Theme.GREEN_BG);
    lblBadge.setForeground(Theme.GREEN);
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Bảng kho hiển thị màu sắc trực quan, các loại thuốc sắp hết như Amoxicillin được cảnh báo nổi bật.

---

#### Phiên 4: Viết hàm trừ tồn kho tự động xuatKho() & Observer Pattern đồng bộ dữ liệu
* **Mã phiên:** `SS-04` | **Ngày:** 20/10/2026
* **Mục tiêu:** Xây dựng cơ chế trừ tồn kho tự động khi kê đơn và thông báo cho bảng kho cập nhật tức thì.

> **Prompt câu lệnh thực tế của Sinh viên:**
> ```text
> Trong KhoHangBUS.java và KhoHangPanel.java:
> 1. Viết hàm xuatKho(String maSKU, int soLuongXuat) trong KhoHangBUS:
>    - Tìm sản phẩm theo maSKU.
>    - tonMoi = tonCu - soLuongXuat (không âm).
>    - Nếu tonMoi == 0, tự chuyển trạng thái sang 'Tạm hết hàng'.
> 2. Cài đặt Observer Pattern:
>    - Thêm List<Runnable> listeners, hàm addListener(Runnable) và notifyListeners().
> 3. Trong KhoHangPanel, đăng ký bus.addListener(this::refresh) để khi có đơn thuốc xuất kho thì bảng kho tự làm mới.
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`KhoHangBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/KhoHangBUS.java#L20-L40)):**
```java
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
            notifyListeners(); // Kích hoạt sự kiện đồng bộ toàn bộ giao diện
            return true;
        }
    }
    return false;
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Kê đơn xuất 5 lọ thuốc, quay lại tab Kho hàng thấy số lượng tồn giảm từ 20 xuống 15 ngay lập tức mà không cần reload.

---

#### Phiên 5: Xây dựng thành phần cuộn lịch CalendarPicker.java bằng Swing thuần
* **Mã phiên:** `SS-05` | **Ngày:** 13/10/2026
* **Mục tiêu:** Tạo hộp thoại chọn ngày tháng trực quan không cần thư viện ngoài như JCalendar.

> **Prompt câu lệnh thực tế của Sinh viên:**
> ```text
> Hãy viết class CalendarPicker.java bằng Swing thuần:
> - Hiển thị dạng popup cuộn lịch tháng.
> - Có 2 nút chuyển tháng trước / tháng sau (<, >) và combobox chọn năm.
> - Lưới hiển thị các ngày trong tuần (T2 đến CN), ngày hôm nay được viền highlight.
> - Khi người dùng click vào một ngày, gọi callback Consumer<LocalDate> và tự đóng popup.
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`CalendarPicker.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/CalendarPicker.java)):**
```java
// Dùng java.time.LocalDate để sinh lưới ngày tự động
LocalDate firstDay = currentYearMonth.atDay(1);
int startCol = firstDay.getDayOfWeek().getValue() - 1; // T2 = 0
int daysInMonth = currentYearMonth.lengthOfMonth();
for (int d = 1; d <= daysInMonth; d++) {
    LocalDate date = currentYearMonth.atDay(d);
    DayCell cell = new DayCell(date, date.equals(selectedDate));
    cell.addActionListener(e -> {
        onDateSelected.accept(date);
        popup.setVisible(false);
    });
    pnlDays.add(cell);
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Tích hợp thành công vào thanh lọc ngày của `KhoHangPanel` và `LichSuNhapXuatPanel`. Người dùng chỉ cần click vào icon lịch là chọn ngày nhanh chóng.

---

### PHẦN III: PHÂN HỆ QUẢN LÝ ĐƠN THUỐC (PRESCRIPTION MANAGEMENT)

#### Phiên 6: Sửa lỗi form kê đơn tự điền sẵn dữ liệu thú cưng & thuốc index 0
* **Mã phiên:** `SS-06` | **Ngày:** 26/10/2026
* **Mục tiêu:** Khắc phục lỗi khi mở form tạo đơn mới: hệ thống tự động điền sẵn thú cưng đầu tiên và thuốc mẫu Clavamox 125mg thay vì form trống.

> **Prompt câu lệnh thực tế của Sinh viên (Nguyên văn):**
> ```text
> "điều chỉnh lại chỗ thêm phác đồ thì lại là thành phần chỉnh sửa và có sẵn dữ liệu của cái đầu, kiểm tra lại toàn bộ logic quản lý đơn thuốc với tạo phác đồ điều trị rồi chỉnh sửa cho chuẩn"
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`DonThuocPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/DonThuocPanel.java#L760-L810)):**
```java
// AI phân định rõ ràng trạng thái isEdit và dọn sạch form khi tạo mới
boolean isEdit = (editTarget != null && editTarget.getMaDonThuoc() != null && !editTarget.getMaDonThuoc().isEmpty());

if (!isEdit && preSelectedTC == null) {
    cbThuCung.addItem("-- Chọn thú cưng điều trị --");
    cbThuCung.setSelectedIndex(0);
    txtChanDoan.setText("");
    txtTrieuChung.setText("");
    txtLoiDan.setText("");
    // Khởi tạo bảng danh mục thuốc rỗng 0 dòng
    listThuoc.clear();
    subModel.fireTableDataChanged();
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Mở form tạo đơn thuốc mới: combobox hiển thị `-- Chọn thú cưng điều trị --`, các ô chẩn đoán và bảng thuốc hoàn toàn rỗng.
  * Lỗi prefilled dữ liệu cũ đã được xóa bỏ triệt để.

---

#### Phiên 7: Tạo chức năng '+ Chọn thuốc từ kho' & Tính tổng tiền tự động
* **Mã phiên:** `SS-07` | **Ngày:** 26/10/2026
* **Mục tiêu:** Cho phép bác sĩ chọn thuốc trực tiếp từ kho hàng, tự động điền đơn vị tính, đơn giá và tính tổng tiền thuốc theo thời gian thực.

> **Prompt câu lệnh thực tế của Sinh viên:**
> ```text
> Trong form kê đơn thuốc của DonThuocPanel.java:
> 1. Thêm nút '+ Chọn thuốc từ kho': lấy danh sách các thuốc có trong KhoHangBUS có danh mục 'Thuốc & Y tế' hoặc mã SKU bắt đầu bằng 'TH'.
> 2. Hiển thị dialog chọn thuốc kèm số lượng tồn và đơn giá.
> 3. Khi click chọn, thêm thuốc vào subTable với số lượng mặc định 1, đơn giá lấy từ kho.
> 4. Viết hàm tinhTongTien tính tổng tiền và cập nhật nhãn 'Tổng tiền thuốc: [X]₫' màu xanh lá mỗi khi sửa số lượng hoặc xóa thuốc.
> 5. Thêm nút '+ Thêm dòng thuốc ngoài' cho thuốc mua ngoài.
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`DonThuocPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/DonThuocPanel.java#L900-L936)):**
```java
RoundedButton btnAddKho = new RoundedButton("+ Chọn thuốc từ kho", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 8).pad(12, 6);
btnAddKho.addActionListener(e -> {
    List<SanPham> spThuoc = new ArrayList<>();
    for (SanPham sp : khoHangBus.layDanhSach()) {
        if ("Thuốc & Y tế".equals(sp.getDanhMuc()) || sp.getMaSKU().startsWith("TH")) spThuoc.add(sp);
    }
    String[] names = new String[spThuoc.size()];
    for (int i = 0; i < spThuoc.size(); i++) {
        SanPham sp = spThuoc.get(i);
        names[i] = sp.getMaSKU() + " - " + sp.getTenSP() + " (Tồn: " + sp.getSoLuongTon() + " " + sp.getDonViTinh() + " • " + Theme.money(sp.getDonGia()) + ")";
    }
    String sel = (String) JOptionPane.showInputDialog(dlg, "Chọn thuốc từ danh mục Kho hàng:", "Thêm thuốc từ kho",
            JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
    if (sel != null) {
        // Nạp thuốc vào bảng và kích hoạt hàm tính tổng tiền
        listThuoc.add(new ChiTietDonThuoc(sp.getMaSKU(), sp.getTenSP(), sp.getDonViTinh(), 1, sp.getDonGia(), "Uống sau ăn"));
        subModel.fireTableDataChanged();
        tinhTongTien.run();
    }
});
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Chọn thuốc nhanh trong 3 giây, bảng tự động cộng thành tiền, không cần gõ thủ công tên thuốc hay đơn giá.

---

#### Phiên 8: Cấp phát thuốc tự động trừ kho hàng và tích hợp in đơn thuốc
* **Mã phiên:** `SS-08` | **Ngày:** 26/10/2026
* **Mục tiêu:** Khi bấm xác nhận cấp phát đơn thuốc, tự động trừ tồn kho các loại thuốc trong danh mục và hỗ trợ in đơn thuốc.

> **Prompt câu lệnh thực tế của Sinh viên:**
> ```text
> 1. Viết hàm capPhatThuoc(String maDT, KhoHangBUS khoHangBus) trong DonThuocBUS.java:
>    - Chuyển trạng thái đơn sang 'Đã cấp phát'.
>    - Duyệt qua danh sách ChiTietDonThuoc, lấy maSKU và soLuong để gọi khoHangBus.xuatKho(ct.getMaSKU(), ct.getSoLuong()).
> 2. Trong DonThuocPanel.java: Thêm hàm inDonThuoc(DonThuoc dt) dùng PrinterJob của Java AWT để in phiếu chỉ định dùng thuốc.
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`DonThuocBUS.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/Bus/DonThuocBUS.java#L82-L100)):**
```java
public boolean capPhatThuoc(String maDT, KhoHangBUS khoHangBus) {
    DonThuoc dt = timTheoMa(maDT);
    if (dt != null) {
        dt.setTrangThai(DonThuoc.DA_CAP_PHAT);
        dt.ghiLog("Đã xuất kho và cấp phát thuốc cho chủ nuôi");

        if (khoHangBus != null && dt.getDanhSachThuoc() != null) {
            for (ChiTietDonThuoc ct : dt.getDanhSachThuoc()) {
                if (ct.getMaSKU() != null && !ct.getMaSKU().trim().isEmpty()) {
                    khoHangBus.xuatKho(ct.getMaSKU(), ct.getSoLuong()); // Tự động trừ kho thuốc
                }
            }
        }
        notifyListeners();
        return true;
    }
    return false;
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Kê đơn xong bấm nút xuất thuốc, hộp thoại xác nhận hiện ra và tự trừ kho thuốc thành công.

---

### PHẦN IV: PHÂN HỆ PHÁC ĐỒ ĐIỀU TRỊ (TREATMENT PROTOCOL & CLINICAL DIARY)

#### Phiên 9: Sửa lỗi tạo phác đồ mới bị nạp nhầm dữ liệu phác đồ cũ
* **Mã phiên:** `SS-09` | **Ngày:** 26/10/2026
* **Mục tiêu:** Đảm bảo khi bấm "Lập phác đồ mới" thì toàn bộ trường nhập liệu phải rỗng, không được lấy phác đồ đầu tiên nạp vào form.

> **Prompt câu lệnh thực tế của Sinh viên (Nguyên văn):**
> ```text
> "điều chỉnh lại chỗ thêm phác đồ thì lại là thành phần chỉnh sửa và có sẵn dữ liệu của cái đầu"
> ```

* **Kết quả mã nguồn do AI cung cấp (Trích đoạn trong [`PhacDoPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/PhacDoPanel.java)):**
```java
boolean isEdit = (editTarget != null && editTarget.getMaPhacDo() != null && !editTarget.getMaPhacDo().isEmpty());

if (!isEdit && preSelectedTC == null) {
    cbThuCung.addItem("-- Chọn thú cưng điều trị --");
    cbThuCung.setSelectedIndex(0);
    txtTenPhacDo.setText("");
    txtChanDoan.setText("");
    txtMucTieu.setText("");
    txtDinhDuong.setText("");
}
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Form lập phác đồ mới mở ra sạch sẽ, hiển thị đúng tiêu đề "Thiết lập Phác đồ Điều trị mới".

---

#### Phiên 10: Xử lý triệt để lỗi xung đột UI Modal Theo dõi diễn tiến (Phân tích ảnh chụp & Tái thiết kế 3 Card)
* **Mã phiên:** `SS-10` | **Ngày:** 26/10/2026
* **Mục tiêu:** Xử lý lỗi giao diện chồng chéo nghiêm trọng trên modal theo dõi diễn tiến khi sinh viên gửi ảnh chụp màn hình bị lỗi.

> **Prompt câu lệnh thực tế của Sinh viên (Đính kèm ảnh chụp màn hình):**
> ```text
> "UI đang bị xung đột quá, chỉnh lại model này rõ ràng ra"
> (Đính kèm ảnh chụp dialog 'Theo dõi diễn tiến: PD-001 (Milo)' bị lỗi: ô nhập đè lên JList, text bị cắt xén, dải highlight xanh đè bẹp lên chữ 'Chi tiết ghi nhận:')
> ```

* **Phân tích nguyên nhân kỹ thuật do AI chỉ ra sau khi đọc ảnh:**
  1. **Kích thước modal quá bé (`620 x 560`):** Không đủ không gian chứa đồng thời thanh trượt slider, combobox trạng thái, 4 ô nhập lâm sàng và bảng nhật ký.
  2. **Lỗi `GridLayout(4, 2)`:** Thêm 4 component vào lưới 4x2 khiến Swing tự động ép thành 1 cột co giãn quá mức.
  3. **Xung đột `BorderLayout`:** Vùng `NORTH` (các ô nhập) và vùng `SOUTH` (danh sách `spList` cao 140px) bị thiếu chiều cao nên trong `BorderLayout` chúng đè trực tiếp lên nhau.
  4. **Dải highlight của `JList` che chữ:** Phần tử đầu tiên của `JList` bị chọn màu xanh đè bẹp lên nhãn nhập liệu bên trên.

* **Giải pháp mã nguồn do AI cung cấp (Tái thiết kế toàn diện [`PhacDoPanel.java`](file:///C:/Users/Nguyen%20Ngoc%20Toan/Downloads/de_an_phat_trien_ung_dung_full_LichHen/de_an_phat_trien_ung_dung_full/de_an_phat_trien_ung_dung_tiep_theo/src/GUI/PhacDoPanel.java#L657-L850)):**
  1. Tăng kích thước modal lên **`840 x 700`** (kích thước tối thiểu `800 x 640`), bọc trong `JScrollPane` mượt mà.
  2. **Header Banner:** Tiêu đề to rõ, thông tin thú cưng (Mã, Pet, giống, cân nặng, tên chủ nuôi) và badge trạng thái.
  3. **Card 1 (Tiến độ & Trạng thái):** Slider 0-100% hiển thị live nhãn %; kéo tới 100% tự chuyển sang "Hoàn thành".
  4. **Card 2 (Ghi nhận lâm sàng lần này):** Lưới 3 cột cân đối: *Thân nhiệt (°C)*, *Cân nặng (kg)*, *Đánh giá tiến triển*, kèm ô nhập *Chi tiết triệu chứng* rộng rãi bên dưới.
  5. **Card 3 (Lịch sử nhật ký điều trị):** Thay thế toàn bộ `JList` bằng **`JTable` 6 cột chuyên nghiệp** (`Thời gian`, `Bác sĩ`, `Thân nhiệt`, `Cân nặng`, `Đánh giá`, `Chi tiết ghi nhận`), định dạng dòng so le zebra.

```java
// Trích đoạn cấu trúc 3 Card mới:
// Card 1: Slider % Tiến độ
JPanel card1 = new JPanel(new BorderLayout(0, 10));
slider.addChangeListener(e -> {
    int val = slider.getValue();
    lblPct.setText(val + "%");
    if (val == 100) cbTrangThai.setSelectedItem(PhacDoDieuTri.HOAN_THANH);
});

// Card 2: Lưới 3 cột lâm sàng
JPanel pRowVitals = new JPanel(new GridLayout(1, 3, 16, 0));
pRowVitals.add(formRow("Thân nhiệt (°C):", txtNhietDo));
pRowVitals.add(formRow("Cân nặng đo được (kg):", txtCanNang));
pRowVitals.add(formRow("Đánh giá tiến triển:", cbDanhGia));

// Card 3: JTable 6 cột chuyên nghiệp thay thế JList thô sơ
String[] cols = {"Thời gian", "Bác sĩ", "Thân nhiệt", "Cân nặng", "Đánh giá", "Chi tiết ghi nhận"};
DefaultTableModel diaryModel = new DefaultTableModel(cols, 0);
JTable tableDiary = new JTable(diaryModel);
tableDiary.setRowHeight(28);
```

* **Thẩm định & Nghiệm thu của Sinh viên:**
  * Chạy thử nghiệm trên màn hình máy tính: Modal mở lên cực kỳ rộng rãi, thẩm mỹ vượt trội, phân chia 3 khối rõ ràng.
  * **Triệt tiêu hoàn toàn 100% lỗi đè chữ hay xung đột giao diện.** Bảng nhật ký 6 cột ghi lại chính xác lịch sử khám từng ngày của thú cưng.

---

## 4. TỔNG KẾT BÀI HỌC KINH NGHIỆM PROMPT ENGINEERING

1. **Hiệu quả của Prompt bằng hình ảnh (Multimodal):**
   * Trong lập trình giao diện Java Swing, việc mô tả lỗi layout bằng lời văn rất khó để AI hình dung chính xác. Việc chụp ảnh màn hình lỗi gửi trực tiếp cho AI giúp AI xác định đúng 100% xung đột giữa `GridLayout` và `BorderLayout`.
2. **Kỹ thuật State Isolation (Tách bạch trạng thái):**
   * Form dùng chung cho Thêm mới và Chỉnh sửa bắt buộc phải có cờ `isEdit` và hàm dọn sạch dữ liệu cũ (`clean state`), tránh việc AI tái sử dụng biến gây lỗi prefilled data cũ.
3. **Vai trò quyết định của Con người (Human-in-the-loop):**
   * AI là công cụ sinh mã nguồn và hỗ trợ giải thuật mạnh mẽ, nhưng việc kết nối logic khép kín: **Hồ sơ Thú cưng ➔ Lập Phác đồ ➔ Kê Đơn thuốc ➔ Tự động trừ Kho hàng** đòi hỏi sinh viên phải nắm vững nghiệp vụ thực tế để ra lệnh và nghiệm thu chặt chẽ.

---
*Tài liệu được lập và lưu trữ chính thức trong cây thư mục dự án.*  
**Sinh viên thực hiện:** Nguyễn Ngọc Toàn  
**Đề án:** Hệ thống Quản lý Phòng khám Thú y (PetCare POS)

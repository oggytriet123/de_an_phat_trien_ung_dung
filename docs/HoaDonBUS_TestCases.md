# Tài liệu cập nhật `HoaDonBUS` và bộ test case kiểm thử logic nghiệp vụ

## 1. Yêu cầu
- Cập nhật `HoaDonBUS.java`: bỏ việc tự động tạo mới khách hàng khi lưu hóa đơn; bắt buộc hóa đơn phải liên kết với khách hàng hợp lệ đã đăng ký; gắn nhân viên lập hóa đơn từ phiên đăng nhập.
- Biên soạn tài liệu và bộ test case để kiểm chứng logic nghiệp vụ.

## 2. Quy tắc nghiệp vụ sau khi cập nhật
| # | Quy tắc |
|---|---|
| R1 | Hóa đơn chỉ được lưu khi số điện thoại thuộc một khách hàng **đã đăng ký**. Hệ thống **không** tự tạo khách mới. |
| R2 | Tên khách trên hóa đơn phải khớp với khách đã đăng ký theo số điện thoại (không phân biệt hoa/thường, dấu, định dạng SĐT). |
| R3 | Hóa đơn luôn gắn mã khách hàng (`maKH`) của khách đã đăng ký. |
| R4 | Hóa đơn gắn mã nhân viên (`maNV`) lấy từ **phiên đăng nhập**. Chưa có nhân viên đăng nhập thì không được lập hóa đơn. |
| R5 | Người lập hóa đơn được giữ nguyên khi người khác sửa hoặc thanh toán hóa đơn đó. |
| R6 | Giữ nguyên các kiểm tra cũ: tên, SĐT hợp lệ, giỏ hàng không trống, phương thức thanh toán hợp lệ. |

## 3. Các file thay đổi
| File | Thay đổi |
|---|---|
| `src/Bus/HoaDonBUS.java` | Thêm `datNhanVienDangNhap`/`getNhanVienDangNhap`; `loiTrungSdt` báo lỗi khi khách chưa đăng ký; `luu()` không còn gọi `khachHang.them(...)`, ném lỗi nếu khách chưa đăng ký hoặc chưa có nhân viên đăng nhập, gắn `maNV` |
| `src/Entity/HoaDon.java` | Thêm thuộc tính `maNV` (getter, setter, sao chép trong `saoChep`) |
| `src/DAO/HoaDonDAO.java` | Bỏ đoạn tự tạo khách mới trong `luuHoaDon` (khách không tồn tại thì ném `SQLException`); ghi `maNV_BH` khi `INSERT` hóa đơn |
| `src/GUI/MainFrame.java` | Sau khi đăng nhập, gọi `hoaDonBus.datNhanVienDangNhap(taiKhoan.getMaTaiKhoan())` |
| `test/Bus/HoaDonBUSTest.java` | Bộ test case (mới) |

`TaoHoaDonPanel` không cần sửa: nó đã gọi `bus.loiTrungSdt(...)` nên thông báo "Khách hàng chưa đăng ký" tự hiện trên màn hình lập hóa đơn.

**Lưu ý về cơ sở dữ liệu:** cột `HOADON.maNV_BH` có khóa ngoại tới `NHANVIEN_BANHANG`. Do đó DAO chỉ ghi `maNV_BH` khi nhân viên đăng nhập thuộc bảng này (trong dữ liệu mẫu là NV002, NV005, NV009); nếu không thì để `NULL` để tránh lỗi khóa ngoại. Hóa đơn trong bộ nhớ vẫn mang `maNV` của người lập.

## 4. Cách chạy test
Từ thư mục gốc dự án (Java 21):
```
javac -encoding UTF-8 -cp lib/mssql-jdbc-13.2.1.jre11.jar -d out_test $(find src -name "*.java")
javac -encoding UTF-8 -cp out_test:lib/mssql-jdbc-13.2.1.jre11.jar -d out_test test/Bus/HoaDonBUSTest.java
java  -Dfile.encoding=UTF-8 -cp out_test:lib/mssql-jdbc-13.2.1.jre11.jar Bus.HoaDonBUSTest
```
(Trên Windows thay `:` bằng `;` và `$(find ...)` bằng danh sách file, hoặc dùng `sources.txt`.)

Test chạy được cả khi **chưa bật SQL Server**: lỗi kết nối chỉ được in ra, logic chạy trên bộ nhớ. Vì vậy các thông báo `SQLServerException ... Connection refused` trong console là bình thường.

## 5. Bộ test case
Kết quả chạy gần nhất: **21 đạt, 0 lỗi / 21**.

| ID | Quy tắc | Dữ liệu vào | Kết quả mong đợi | Kết quả |
|---|---|---|---|---|
| TC01 | R1 | Khách "Trần Thị Bình", SĐT 0912345678 (chưa đăng ký), có hàng | `kiemTra` trả lỗi chứa "chưa đăng ký" | Đạt |
| TC02 | R1 | Như TC01, gọi `luu()` | Ném `IllegalArgumentException` | Đạt |
| TC03 | R1 | Như TC02 | Số khách hàng không tăng (không tự tạo) | Đạt |
| TC04 | R1 | Như TC02 | Ô hóa đơn vẫn trống | Đạt |
| TC05 | R2 | SĐT 0901234567 (đã đăng ký cho "Nguyễn Văn An") nhưng tên "Lê Văn Cường" | Lỗi "đã thuộc khách hàng" | Đạt |
| TC06 | R2 | Tên "nguyen van an", SĐT "0901 234 567" | Hợp lệ (`kiemTra` = null) | Đạt |
| TC07 | R3 | Lưu hóa đơn hợp lệ | Ô được giữ hóa đơn | Đạt |
| TC08 | R3 | Như TC07 | `maKH` = mã của khách đã đăng ký | Đạt |
| TC09 | R4 | Đăng nhập NV002, lưu hóa đơn | `maNV` = NV002 | Đạt |
| TC10 | R1 | Như TC07 | Số khách hàng không đổi | Đạt |
| TC11 | R4 | `moNhap` hóa đơn đã lưu | Bản sao giữ `maNV` = NV002 | Đạt |
| TC12 | R4 | `HoaDonBUS` chưa gọi `datNhanVienDangNhap`, gọi `luu()` | Ném `IllegalStateException` | Đạt |
| TC13 | R4 | Đổi sang NV005, lưu hóa đơn mới | `maNV` = NV005 | Đạt |
| TC14 | R5 | Sau TC13, xem hóa đơn cũ | Vẫn là NV002 | Đạt |
| TC15 | R6 | Giỏ hàng trống | `kiemTra` trả lỗi | Đạt |
| TC16 | R6 | Tên rỗng | `kiemTra` trả lỗi | Đạt |
| TC17 | R6 | SĐT "123" | `kiemTra` trả lỗi | Đạt |
| TC18 | R6 | Thanh toán bằng "Bitcoin" | Ném `IllegalArgumentException` | Đạt |
| TC19 | R6 | Thanh toán "Tiền mặt" | Ô trống lại, hóa đơn vào lịch sử | Đạt |
| TC20 | R5 | Hóa đơn do NV002 lập, NV009 mở lại và lưu | `maNV` vẫn là NV002 | Đạt |
| TC21 | R3, R4 | Hóa đơn đã thanh toán | Giữ `maKH` và `maNV` | Đạt |

## 6. Phạm vi và hạn chế
- Test mới kiểm thử lớp BUS trên bộ nhớ. **Chưa kiểm thử với SQL Server thật**, nên việc ghi `maNV_BH` và khóa ngoại cần chạy thử trên máy có CSDL (lập hóa đơn bằng tài khoản NV002, rồi `SELECT maHD, maKH, maNV_BH FROM HOADON`).
- Thay đổi hành vi cần báo với nhóm: nhân viên **không còn lập được hóa đơn cho khách chưa đăng ký** ngay trên màn lập hóa đơn. Cần đăng ký khách hàng trước ở Danh Mục > Khách Hàng.

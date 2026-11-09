package DAO;

import java.sql.Connection;
import java.sql.Statement;
import ConnectDB.ConnectDB;

/**
 * Nạp lại toàn bộ dữ liệu mẫu chuẩn UTF-8 cho CSDL PetCare.
 * Đảm bảo phân quyền tài khoản, khách hàng, thú cưng, lịch hẹn, phẫu thuật, tiêm phòng, đối tác, kho.
 */
public class DataFixer {

    public static void main(String[] args) {
        System.out.println("=== BAT DAU NAP DU LIEU PETCARE CHUAN UTF-8 ===");
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            // Đảm bảo cấu trúc các bảng mở rộng
            try { new ChuongDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            try { new PhauThuatDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            try { new TiemPhongDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            try { new DoiTacDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            try { new PhieuKhoDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            try { new LichHenDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            try { new HoaDonDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            try { new MaGiamGiaDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            try { new DichVuDAO().damBaoCauTrucBang(); } catch (Exception ignored) {}
            
            // 1. Dọn dẹp dữ liệu cũ (theo thứ tự phụ thuộc khóa ngoại)
            System.out.println("-> Xóa dữ liệu cũ...");
            try { st.executeUpdate("DELETE FROM CHITIET_DICHVU_CHUONG"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM PHIEU_LUUTRU"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM CHITIETHOADON"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM HOADON"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM LICHHEN"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM PHAUTHUAT"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM TIEMPHONG"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM PHIEUKHO"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM DOITAC"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM MAGIAMGIA"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM DANHMUCBENH"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM CHUONGLUUTRU"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM SANPHAM"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM DICHVU"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM THUCUNG"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM KHACHHANG"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM QUANLY"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM NHANVIEN_BANHANG"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM NHANVIEN_KHO"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM NHANVIEN_BACSI"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM NHANVIEN"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM QUYEN_CHUCNANG"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM CHUCNANG"); } catch (Exception ignored) {}
            try { st.executeUpdate("DELETE FROM PHANQUYEN"); } catch (Exception ignored) {}

            // Reset identity
            try { st.executeUpdate("DBCC CHECKIDENT ('PHANQUYEN', RESEED, 0)"); } catch (Exception ignored) {}
            try { st.executeUpdate("DBCC CHECKIDENT ('CHUCNANG', RESEED, 0)"); } catch (Exception ignored) {}

            // 2. PHANQUYEN
            System.out.println("-> Nạp PHANQUYEN...");
            st.executeUpdate("INSERT INTO PHANQUYEN (tenQuyen) VALUES "
                    + "(N'Quản lý'), (N'Bán hàng'), (N'Kho'), (N'Bác sĩ')");

            // 3. CHUCNANG
            System.out.println("-> Nạp CHUCNANG...");
            st.executeUpdate("INSERT INTO CHUCNANG (tenChucNang, moTa) VALUES "
                    + "(N'Trang Chủ', N'Xem tổng quan hệ thống'), "
                    + "(N'Tài Khoản', N'Quản lý tài khoản cá nhân'), "
                    + "(N'Nhân Viên', N'Quản lý hồ sơ và phân quyền nhân viên'), "
                    + "(N'Khách Hàng', N'Quản lý thông tin và lịch sử khách hàng'), "
                    + "(N'Thú Cưng', N'Hồ sơ thú cưng và tình trạng sức khỏe'), "
                    + "(N'Dịch Vụ', N'Quản lý danh mục dịch vụ và bảng giá'), "
                    + "(N'Sản Phẩm', N'Quản lý sản phẩm và danh mục bán lẻ'), "
                    + "(N'Bán Hàng', N'Lập hóa đơn và thanh toán POS'), "
                    + "(N'Kho Hàng', N'Quản lý tồn kho và phiếu nhập/xuất'), "
                    + "(N'Lịch Hẹn', N'Đặt lịch, xếp lịch khám và nhắc hẹn'), "
                    + "(N'Lưu Trú', N'Quản lý chuồng và dịch vụ lưu trú'), "
                    + "(N'Bác Sĩ', N'Khám bệnh, tiêm phòng và phẫu thuật'), "
                    + "(N'Thống Kê', N'Báo cáo doanh thu và hiệu suất'), "
                    + "(N'Tìm Kiếm', N'Tra cứu nhanh thông tin hệ thống'), "
                    + "(N'Đơn Thuốc & Phác Đồ', N'Kê đơn thuốc và thiết lập phác đồ điều trị'), "
                    + "(N'Danh Mục Bệnh', N'Quản lý bệnh lý và triệu chứng'), "
                    + "(N'Khuyến Mãi', N'Chương trình khuyến mãi và mã giảm giá')");

            // 4. QUYEN_CHUCNANG
            System.out.println("-> Nạp QUYEN_CHUCNANG...");
            st.executeUpdate("INSERT INTO QUYEN_CHUCNANG (maQuyen, maChucNang) VALUES "
                    + "(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17), "
                    + "(2,1),(2,2),(2,4),(2,5),(2,6),(2,7),(2,8),(2,10),(2,11),(2,14),(2,17), "
                    + "(3,1),(3,2),(3,7),(3,9),(3,13),(3,14), "
                    + "(4,1),(4,2),(4,4),(4,5),(4,6),(4,10),(4,11),(4,12),(4,13),(4,14),(4,15),(4,16)");

            // 5. NHANVIEN (Mật khẩu: 123456 hash SHA-256)
            System.out.println("-> Nạp NHANVIEN...");
            String passHash = "8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92";
            st.executeUpdate("INSERT INTO NHANVIEN (maNV, hoTen, tenDangNhap, matKhau, maQuyen, soDienThoai, ngaySinh, email, trangThai, caLamViec, ngayVaoLam) VALUES "
                    + "(N'NV001', N'Hùng Hữu Thịnh', N'admin', '" + passHash + "', 1, '0901000001', '1995-03-15', 'thinh.hh@petcare.vn', N'Đang làm việc', N'Toàn thời gian', '2023-01-01'), "
                    + "(N'NV002', N'Hà Trọng Trung', N'trung.ht', '" + passHash + "', 2, '0901000002', '1998-07-20', 'trung.ht@petcare.vn', N'Đang làm việc', N'Ca sáng (07:30 - 15:30)', '2023-03-15'), "
                    + "(N'NV003', N'Lê Tuấn Anh', N'tuananh.lt', '" + passHash + "', 3, '0901000003', '1997-11-05', 'tuananh.lt@petcare.vn', N'Đang làm việc', N'Ca sáng (07:30 - 15:30)', '2023-02-01'), "
                    + "(N'NV004', N'Nguyễn Hoài Anh', N'hoaianh.nh', '" + passHash + "', 4, '0901000004', '1992-09-12', 'hoaianh.nh@petcare.vn', N'Đang làm việc', N'Toàn thời gian', '2022-10-01'), "
                    + "(N'NV005', N'Nguyễn Ngọc Toàn', N'toan.nn', '" + passHash + "', 2, '0901000005', '1999-01-25', 'toan.nn@petcare.vn', N'Đang làm việc', N'Ca chiều (14:30 - 21:30)', '2023-05-10'), "
                    + "(N'NV006', N'Võ Thanh Hùng', N'hung06', '" + passHash + "', 4, '0901000006', '1990-12-08', 'hung.vt@petcare.vn', N'Đang làm việc', N'Toàn thời gian', '2022-08-15'), "
                    + "(N'NV007', N'Đặng Thị Mai', N'mai07', '" + passHash + "', 3, '0901000007', '1996-04-18', 'mai.dt@petcare.vn', N'Đang làm việc', N'Ca chiều (14:30 - 21:30)', '2023-04-01'), "
                    + "(N'NV008', N'Ngô Gia Bảo', N'bao08', '" + passHash + "', 4, '0901000008', '1994-06-30', 'bao.ng@petcare.vn', N'Đang làm việc', N'Ca xoay', '2023-02-15'), "
                    + "(N'NV009', N'Đỗ Phương Thảo', N'thao09', '" + passHash + "', 2, '0901000009', '2000-08-14', 'thao.dp@petcare.vn', N'Đang làm việc', N'Ca sáng (07:30 - 15:30)', '2023-07-01'), "
                    + "(N'NV010', N'Bùi Hữu Thịnh', N'thinh10', '" + passHash + "', 1, '0901000010', '1993-02-28', 'thinh.bh@petcare.vn', N'Đang làm việc', N'Toàn thời gian', '2022-05-01')");

            // Sub-tables nhân viên
            st.executeUpdate("INSERT INTO QUANLY (maNV) VALUES (N'NV001'), (N'NV010')");
            st.executeUpdate("INSERT INTO NHANVIEN_BANHANG (maNV) VALUES (N'NV002'), (N'NV005'), (N'NV009')");
            st.executeUpdate("INSERT INTO NHANVIEN_KHO (maNV) VALUES (N'NV003'), (N'NV007')");
            st.executeUpdate("INSERT INTO NHANVIEN_BACSI (maNV) VALUES (N'NV004'), (N'NV006'), (N'NV008')");

            // 6. KHACHHANG
            System.out.println("-> Nạp KHACHHANG...");
            st.executeUpdate("INSERT INTO KHACHHANG (maKH, hoTen, soDienThoai, email, diemTichLuy, hangThe, maNV_BH, ngayTao) VALUES "
                    + "(N'KH001', N'Nguyễn Minh Anh', '0903123456', 'minhanh.ng@gmail.com', 1250, N'VIP', N'NV002', '2023-01-15'), "
                    + "(N'KH002', N'Lê Thị Cẩm Tú', '0938222111', 'camtu.le@gmail.com', 680, N'VIP', N'NV002', '2023-02-20'), "
                    + "(N'KH003', N'Phạm Gia Hân', '0977654321', 'giahan.pham@gmail.com', 320, N'Thân thiết', N'NV005', '2023-03-10'), "
                    + "(N'KH004', N'Võ Hoàng Long', '0386909808', 'hoanglong.vo@gmail.com', 890, N'VIP', N'NV005', '2023-01-28'), "
                    + "(N'KH005', N'Hoàng Minh Tuấn', '0945678901', 'minhtuan.h@gmail.com', 150, N'Thân thiết', N'NV009', '2023-06-15'), "
                    + "(N'KH006', N'Bùi Anh Khoa', '0919888000', 'anhkhoa.bui@gmail.com', 1420, N'VIP', N'NV002', '2023-02-05'), "
                    + "(N'KH007', N'Đặng Thu Hà', '0905777333', 'thuha.dang@gmail.com', 410, N'Thân thiết', N'NV009', '2023-04-12'), "
                    + "(N'KH008', N'Hoàng Thị Mai', '0932111222', 'mai.hoang@gmail.com', 95, N'Thường', N'NV005', '2023-07-20')");

            // 7. THUCUNG
            System.out.println("-> Nạp THUCUNG...");
            st.executeUpdate("INSERT INTO THUCUNG (petID, ten, loai, giong, gioiTinh, tuoi, canNang, theCanhBao, trangThai, ngayTiepNhan, maKH) VALUES "
                    + "(N'PET001', N'Kitty', N'Mèo', N'Anh lông ngắn', N'Cái', 2, 4.2, N'Bình thường', N'Khỏe mạnh', '2023-01-15', N'KH001'), "
                    + "(N'PET002', N'Milu', N'Chó', N'Golden Retriever', N'Đực', 3, 28.5, N'Dị ứng hải sản', N'Đang điều trị', '2023-02-20', N'KH002'), "
                    + "(N'PET003', N'Mochi', N'Mèo', N'Ba Tư (Persian)', N'Cái', 1, 3.8, N'Bình thường', N'Khỏe mạnh', '2023-03-01', N'KH002'), "
                    + "(N'PET004', N'Rocky', N'Chó', N'Rottweiler', N'Đực', 4, 35.0, N'Hung dữ khi gặp người lạ', N'Hậu phẫu', '2023-01-28', N'KH004'), "
                    + "(N'PET005', N'Mimi', N'Mèo', N'Xiêm (Siamese)', N'Cái', 2, 3.2, N'Bình thường', N'Chờ phẫu thuật', '2023-02-15', N'KH004'), "
                    + "(N'PET006', N'Bông Gòn', N'Chó', N'Samoyed', N'Đực', 1, 18.0, N'Bình thường', N'Khỏe mạnh', '2023-02-05', N'KH006'), "
                    + "(N'PET007', N'Buddy', N'Chó', N'Poodle Toy', N'Đực', 3, 4.5, N'Viêm da cơ địa', N'Đang lưu trú', '2023-06-15', N'KH005'), "
                    + "(N'PET008', N'Mochi Béo', N'Mèo', N'Munchkin', N'Cái', 2, 3.5, N'Bình thường', N'Khỏe mạnh', '2023-07-20', N'KH008'), "
                    + "(N'PET009', N'Corgi Mông', N'Chó', N'Corgi Pembroke', N'Đực', 2, 11.2, N'Bình thường', N'Khỏe mạnh', '2023-03-12', N'KH001'), "
                    + "(N'PET010', N'Bánh Bao', N'Thỏ', N'Thỏ Minilop', N'Cái', 1, 1.8, N'Nhạy cảm tiếng ồn', N'Khỏe mạnh', '2023-04-10', N'KH003'), "
                    + "(N'PET011', N'Đậu Phộng', N'Chó', N'Pomeranian', N'Cái', 2, 3.0, N'Bình thường', N'Khỏe mạnh', '2023-04-12', N'KH007'), "
                    + "(N'PET012', N'Bằng Xù', N'Mèo', N'Mèo Ragdoll', N'Đực', 3, 5.1, N'Bình thường', N'Đã hồi phục', '2023-05-02', N'KH006')");

            // 8. DICHVU
            System.out.println("-> Nạp DICHVU...");
            st.executeUpdate("INSERT INTO DICHVU (maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong) VALUES "
                    + "(N'DV001', N'Khám tổng quát', 200000, N'Khám & Điều trị', 30, N'Khám tổng quát tai mũi họng, tim phổi, kiểm tra thân nhiệt và tư vấn sức khỏe', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'DV002', N'Tiêm phòng vaccine', 250000, N'Phòng bệnh', 15, N'Tiêm vaccine định kỳ phòng ngừa các bệnh truyền nhiễm nguy hiểm', N'Đang cung cấp', N'Chó & Mèo'), "
                    + "(N'DV003', N'Tắm và cắt tỉa lông', 350000, N'Spa & Grooming', 90, N'Gói spa toàn diện: tắm sấy, chải lông rụng, cắt móng, vệ sinh tai và tạo kiểu lông', N'Đang cung cấp', N'Chó & Mèo'), "
                    + "(N'DV004', N'Lấy cao răng siêu âm', 400000, N'Nha khoa', 45, N'Làm sạch mảng bám răng bằng máy siêu âm hiện đại không đau', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'DV005', N'Lưu trú thú cưng (theo đêm)', 180000, N'Lưu trú', 1440, N'Dịch vụ khách sạn thú cưng tiện nghi, phòng máy lạnh, camera theo dõi 24/7', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'DV006', N'Cắt móng & vệ sinh tai', 80000, N'Spa & Grooming', 20, N'Dịch vụ cơ bản cắt mài móng an toàn và vệ sinh làm sạch ống tai ngoài', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'DV007', N'Xét nghiệm máu tổng quát', 450000, N'Xét nghiệm', 30, N'Tổng phân tích tế bào máu 18 thông số và sinh hóa máu cơ bản', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'DV008', N'Siêu âm ổ bụng', 300000, N'Chẩn đoán hình ảnh', 25, N'Siêu âm khảo sát các cơ quan nội tạng ổ bụng bằng máy siêu âm màu 4D', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'VAC-001', N'Tiêm vaccine phòng Dại (Rabies)', 250000, N'Tiêm phòng', 15, N'Tiêm vaccine phòng bệnh dại định kỳ cho chó và mèo', N'Đang cung cấp', N'Chó & Mèo'), "
                    + "(N'VAC-002', N'Tiêm vaccine 4 bệnh mèo (Nobivac Ducat)', 280000, N'Tiêm phòng', 15, N'Vaccine phòng 4 bệnh truyền nhiễm nguy hiểm ở mèo', N'Đang cung cấp', N'Mèo'), "
                    + "(N'VAC-003', N'Tiêm vaccine 5 bệnh chó', 260000, N'Tiêm phòng', 15, N'Vaccine phòng 5 bệnh truyền nhiễm nguy hiểm ở chó', N'Đang cung cấp', N'Chó'), "
                    + "(N'VAC-004', N'Tiêm vaccine 7 bệnh chó (Vanguard Plus 5/L4)', 300000, N'Tiêm phòng', 15, N'Vaccine tổng hợp 7 bệnh phổ biến cho chó', N'Đang cung cấp', N'Chó'), "
                    + "(N'SUR-001', N'Triệt sản mèo cái nội soi', 650000, N'Phẫu thuật', 45, N'Phẫu thuật nội soi triệt sản ít xâm lấn, hồi phục nhanh', N'Đang cung cấp', N'Mèo'), "
                    + "(N'SUR-002', N'Triệt sản mèo đực', 350000, N'Phẫu thuật', 30, N'Tiểu phẫu triệt sản an toàn cho mèo đực', N'Đang cung cấp', N'Mèo'), "
                    + "(N'SUR-003', N'Phẫu thuật lấy sỏi bàng quang', 1500000, N'Phẫu thuật', 60, N'Mổ hở gắp sỏi và thông đường tiết niệu', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'SUR-004', N'Phẫu thuật kết hợp xương gãy', 2500000, N'Phẫu thuật', 90, N'Cố định xương gãy bằng nẹp vis chuyên dụng', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'SUR-005', N'Cắt mộng mắt', 500000, N'Phẫu thuật', 30, N'Phẫu thuật tạo hình mộng mắt (Cherry eye) cho thú cưng', N'Đang cung cấp', N'Tất cả thú cưng'), "
                    + "(N'SUR-006', N'Mổ u nang ngoài da', 700000, N'Phẫu thuật', 40, N'Cắt bỏ khối u ngoài da và khâu thẩm mỹ', N'Đang cung cấp', N'Tất cả thú cưng')");

            // 9. SANPHAM
            System.out.println("-> Nạp SANPHAM...");
            st.executeUpdate("INSERT INTO SANPHAM (sku_maSP, tenSP, donGia, soLuongTon, hanSuDung, maNV_Kho) VALUES "
                    + "(N'SP001', N'Thức ăn hạt cho chó Royal Canin 2kg', 350000, 50, '2026-06-15', N'NV003'), "
                    + "(N'SP002', N'Pate cho mèo Whiskas cá hồi 85g', 25000, 200, '2026-12-30', N'NV003'), "
                    + "(N'SP003', N'Cát vệ sinh cho mèo Cature đậu nành 6L', 145000, 80, '2027-01-01', N'NV007'), "
                    + "(N'SP004', N'Sữa tắm trị ve rận Bio Derma 250ml', 95000, 45, '2026-09-20', N'NV007'), "
                    + "(N'SP005', N'Vòng cổ trị ve rận Seresto cho chó', 650000, 30, '2027-03-15', N'NV003'), "
                    + "(N'SP006', N'Xương gặm sạch răng Pedigree DentaStix 110g', 48000, 120, '2026-11-10', N'NV003'), "
                    + "(N'SP007', N'Dung dịch nhỏ mắt cho thú cưng Bioline 50ml', 65000, 60, '2026-08-05', N'NV007'), "
                    + "(N'SP008', N'Gel dinh dưỡng cho chó mèo Nourse 120g', 180000, 40, '2026-10-25', N'NV003')");

            // 10. CHUONGLUUTRU
            System.out.println("-> Nạp CHUONGLUUTRU...");
            st.executeUpdate("INSERT INTO CHUONGLUUTRU (cageID, size, trangThai, giaTheoDem, maNV_QL) VALUES "
                    + "(N'C-101', N'Lớn (120x80x90)', N'Đang sử dụng', 250000, N'NV001'), "
                    + "(N'C-102', N'Vừa (80x60x70)', N'Trống', 180000, N'NV001'), "
                    + "(N'C-201', N'VIP kính (100x70x80)', N'Trống', 220000, N'NV001'), "
                    + "(N'C-202', N'Đôi (120x80x80)', N'Trống', 300000, N'NV001')");

            // 10.1 PHIEU_LUUTRU & CHITIET_DICHVU_CHUONG
            System.out.println("-> Nạp PHIEU_LUUTRU & CHITIET_DICHVU_CHUONG...");
            try { st.executeUpdate("IF COL_LENGTH('PHIEU_LUUTRU', 'tongTien') IS NULL ALTER TABLE PHIEU_LUUTRU ADD tongTien FLOAT DEFAULT 0"); } catch (Exception ignored) {}
            st.executeUpdate("INSERT INTO PHIEU_LUUTRU (maPhieu, cageID, maKH, petID, tenPet, loaiPet, tenKhach, sdtKhach, ngayVao, ngayRaDuKien, trangThai, ghiChu, tongTien) VALUES "
                    + "(N'PLT001', N'C-101', N'KH005', N'PET007', N'Buddy', N'Chó Poodle Toy', N'Hoàng Minh Tuấn', '0945678901', GETDATE(), DATEADD(day, 3, GETDATE()), N'Đang lưu trú', N'Ăn 2 bữa/ngày, theo dõi sức khỏe', 750000)");
            st.executeUpdate("INSERT INTO CHITIET_DICHVU_CHUONG (maPhieu, maDV, tenDV, loai, thoiGian, ngaySuDung, soLuong, donGia) VALUES "
                    + "(N'PLT001', N'DV005', N'Lưu trú thú cưng (theo đêm)', N'Lưu trú', GETDATE(), CAST(GETDATE() AS DATE), 3, 250000)");

            // 11. DANHMUCBENH
            System.out.println("-> Nạp DANHMUCBENH...");
            st.executeUpdate("INSERT INTO DANHMUCBENH (maBenh, tenBenh, trieuChung, maNV_QL) VALUES "
                    + "(N'B001', N'Bệnh Care ở chó (Canine Distemper)', N'Sốt cao, chảy nước mũi, ho, co giật', N'NV001'), "
                    + "(N'B002', N'Bệnh Parvo ở chó (Canine Parvovirus)', N'Nôn mửa dữ dội, tiêu chảy ra máu mùi tanh', N'NV001'), "
                    + "(N'B003', N'Bệnh Giảm bạch cầu ở mèo (FPV)', N'Ủ rũ, bỏ ăn, sốt cao, nôn dịch vàng', N'NV001'), "
                    + "(N'B004', N'Viêm da do nấm (Dermatophytosis)', N'Rụng lông từng mảng tròn, da đóng vảy ngứa', N'NV001')");

            // 12. MAGIAMGIA
            System.out.println("-> Nạp MAGIAMGIA...");
            st.executeUpdate("INSERT INTO MAGIAMGIA (maGiamGia, phanTram, giaTri, hanDung, trangThai) VALUES "
                    + "(N'GIAM10', 1, 10, '2027-12-31', N'Hoạt động'), "
                    + "(N'PET100K', 0, 100000, '2027-12-31', N'Hoạt động'), "
                    + "(N'PET50K', 0, 50000, '2027-12-31', N'Hoạt động'), "
                    + "(N'VIP20', 1, 20, '2027-12-31', N'Hoạt động')");

            // 13. DOITAC (Cả nhà cung cấp nhập kho và đối tác xuất kho/chi nhánh)
            System.out.println("-> Nạp DOITAC...");
            st.executeUpdate("INSERT INTO DOITAC (maDT, tenDT, loaiDT, soDienThoai, diaChi, email, trangThai) VALUES "
                    + "(N'DT001', N'Công Ty CP Royal Canin VN', N'Nhà cung cấp thức ăn', '02838123456', N'KCN Tân Bình, TP.HCM', 'contact@royalcanin.vn', N'Đang hợp tác'), "
                    + "(N'DT002', N'Công Ty TNHH Zoetis Việt Nam', N'Nhà cung cấp dược thú y', '02839988776', N'Quận 1, TP.HCM', 'info@zoetis.vn', N'Đang hợp tác'), "
                    + "(N'DT003', N'Công Ty TNHH Virbac Việt Nam', N'Nhà cung cấp vaccine', '02743789123', N'KCN Sóng Thần, Bình Dương', 'support@virbac.vn', N'Đang hợp tác'), "
                    + "(N'DT004', N'Công Ty CP Guyomarc H VN', N'Nhà cung cấp thức ăn', '02836223344', N'Quận 7, TP.HCM', 'info@guyomarch.vn', N'Đang hợp tác'), "
                    + "(N'DT005', N'Chi nhánh PetCare Quận 7', N'Chi nhánh nội bộ', '02838765432', N'Quận 7, TP.HCM', 'q7@petcare.vn', N'Đang hợp tác'), "
                    + "(N'DT006', N'Chi nhánh PetCare Thủ Đức', N'Chi nhánh nội bộ', '02839112233', N'TP. Thủ Đức, TP.HCM', 'thuduc@petcare.vn', N'Đang hợp tác'), "
                    + "(N'DT007', N'Phòng khám thú y Happy Paws', N'Đối tác phân phối', '0908123456', N'Quận Bình Thạnh, TP.HCM', 'happypaws@gmail.com', N'Đang hợp tác'), "
                    + "(N'DT008', N'Phòng khám thú y An Việt', N'Đối tác phân phối', '0912345678', N'Quận 10, TP.HCM', 'anviet@gmail.com', N'Đang hợp tác'), "
                    + "(N'DT009', N'Thuốc thú y Minh Phương', N'Nhà cung cấp dược thú y', '0934567890', N'Quận 5, TP.HCM', 'minhphuong@gmail.com', N'Đang hợp tác'), "
                    + "(N'DT010', N'Công Ty Green Pet', N'Nhà cung cấp thức ăn', '02838991234', N'Bình Dương', 'greenpet@gmail.com', N'Đang hợp tác')");

            // 14. PHIEUKHO (Nhập: NV kho làm, xuất: bán/hủy cho đối tác)
            System.out.println("-> Nạp PHIEUKHO...");
            st.executeUpdate("INSERT INTO PHIEUKHO (maPhieu, loaiPhieu, ngayLap, gioLap, nguoiThucHien, doiTac, tongGiaTri, trangThai, ghiChu, maNV, maDT) VALUES "
                    + "(N'PN-2026-0041', N'Nhập mới', '2026-03-01', '08:30', N'Lê Tuấn Anh', N'Công Ty CP Royal Canin VN', 15240000, N'Hoàn tất', NULL, N'NV003', N'DT001'), "
                    + "(N'PN-2026-0042', N'Nhập mới', '2026-03-02', '09:15', N'Đặng Thị Mai', N'Thuốc thú y Minh Phương', 6820000, N'Hoàn tất', NULL, N'NV007', N'DT009'), "
                    + "(N'PN-2026-0043', N'Nhập mới', '2026-03-02', '10:00', N'Lê Tuấn Anh', N'Công Ty CP Royal Canin VN', 21000000, N'Hoàn tất', NULL, N'NV003', N'DT001'), "
                    + "(N'PN-2026-0044', N'Nhập mới', '2026-03-03', '08:00', N'Đặng Thị Mai', N'Công Ty TNHH Zoetis Việt Nam', 9540000, N'Hoàn tất', NULL, N'NV007', N'DT002'), "
                    + "(N'PN-2026-0045', N'Nhập mới', '2026-03-04', '09:45', N'Lê Tuấn Anh', N'Công Ty Green Pet', 12800000, N'Hoàn tất', NULL, N'NV003', N'DT010'), "
                    + "(N'PX-2026-0039', N'Xuất bán', '2026-03-01', '11:20', N'Hà Trọng Trung', N'Phòng khám thú y Happy Paws', 1980000, N'Hoàn tất', NULL, N'NV002', N'DT007'), "
                    + "(N'PX-2026-0040', N'Xuất hủy', '2026-03-02', '15:45', N'Võ Thanh Hùng', N'Chi nhánh PetCare Thủ Đức', 4750000, N'Đã hủy', NULL, N'NV006', N'DT006'), "
                    + "(N'PX-2026-0041', N'Xuất bán', '2026-03-03', '14:10', N'Nguyễn Hoài Anh', N'Phòng khám thú y An Việt', 2350000, N'Hoàn tất', NULL, N'NV004', N'DT008'), "
                    + "(N'PX-2026-0042', N'Xuất bán', '2026-03-04', '09:30', N'Hà Trọng Trung', N'Chi nhánh PetCare Quận 7', 2450000, N'Hoàn tất', NULL, N'NV002', N'DT005')");

            // 15. LICHHEN (Khách hàng & Thú cưng liên kết chính xác)
            System.out.println("-> Nạp LICHHEN...");
            st.executeUpdate("INSERT INTO LICHHEN (maLH, ngayHen, khungGio, trangThai, petID, maNV_BS, maDV, cageID, tenKH, soDienThoai, tenPet, tenDV, tenBacSi, gioHen, ghiChu, nhatKy, maKH) VALUES "
                    + "(N'LH001', '2026-10-20', '08:00 - 09:00', N'Đã xác nhận', N'PET001', N'NV004', N'DV001', NULL, N'Nguyễn Minh Anh', '0903123456', N'Kitty', N'Khám tổng quát', N'BS. Nguyễn Hoài Anh', '08:00', NULL, NULL, N'KH001'), "
                    + "(N'LH002', '2026-10-20', '09:00 - 10:00', N'Đã xác nhận', N'PET004', N'NV006', N'DV002', NULL, N'Võ Hoàng Long', '0386909808', N'Rocky', N'Tiêm phòng vaccine', N'BS. Võ Thanh Hùng', '09:00', NULL, NULL, N'KH004'), "
                    + "(N'LH003', '2026-10-21', '10:00 - 11:00', N'Đã xác nhận', N'PET002', N'NV006', N'DV003', NULL, N'Lê Thị Cẩm Tú', '0938222111', N'Milu', N'Tắm và cắt tỉa lông', N'BS. Võ Thanh Hùng', '10:00', NULL, NULL, N'KH002'), "
                    + "(N'LH004', '2026-10-22', '14:00 - 15:00', N'Chờ xác nhận', N'PET003', N'NV006', N'DV007', NULL, N'Lê Thị Cẩm Tú', '0938222111', N'Mochi', N'Xét nghiệm máu tổng quát', N'BS. Võ Thanh Hùng', '14:00', NULL, NULL, N'KH002'), "
                    + "(N'LH005', '2026-10-18', '08:00 - 09:00', N'Hoàn thành', N'PET005', N'NV004', N'DV001', NULL, N'Võ Hoàng Long', '0386909808', N'Mimi', N'Khám tổng quát', N'BS. Nguyễn Hoài Anh', '08:00', NULL, NULL, N'KH004'), "
                    + "(N'LH006', '2026-10-25', '15:00 - 16:00', N'Chờ xác nhận', N'PET007', N'NV006', N'DV005', N'C-101', N'Hoàng Minh Tuấn', '0945678901', N'Buddy', N'Lưu trú thú cưng (theo dõi)', N'BS. Võ Thanh Hùng', '15:00', NULL, NULL, N'KH005')");

            // 16. PHAUTHUAT
            System.out.println("-> Nạp PHAUTHUAT...");
            st.executeUpdate("INSERT INTO PHAUTHUAT (maCa, tenThuCung, loaiThuCung, tenChu, sdt, loaiPhauThuat, ngayMo, gioMo, bacSiChinh, chiPhi, trangThai, ghiChu, petID, maKH) VALUES "
                    + "(N'PT-001', N'Milu', N'Chó Golden Retriever', N'Lê Thị Cẩm Tú', '0938222111', N'Phẫu thuật lấy sỏi bàng quang', '2026-10-15', '08:00', N'BS. Võ Thanh Hùng', 1500000, N'Đang phẫu thuật', N'Gây mê ổn định', N'PET002', N'KH002'), "
                    + "(N'PT-002', N'Mimi', N'Mèo Xiêm (Siamese)', N'Võ Hoàng Long', '0386909808', N'Triệt sản mèo cái nội soi', '2026-10-18', '09:00', N'BS. Ngô Gia Bảo', 650000, N'Chờ phẫu thuật', N'Đã nhịn ăn 8h', N'PET005', N'KH004'), "
                    + "(N'PT-003', N'Rocky', N'Chó Rottweiler', N'Võ Hoàng Long', '0386909808', N'Phẫu thuật kết hợp xương gãy', '2026-10-20', '10:00', N'BS. Võ Thanh Hùng', 2500000, N'Theo dõi hậu phẫu', N'Cố định nẹp xương', N'PET004', N'KH004'), "
                    + "(N'PT-004', N'Bằng Xù', N'Mèo Ragdoll', N'Bùi Anh Khoa', '0919888000', N'Triệt sản mèo đực', '2026-10-22', '08:30', N'BS. Nguyễn Hoài Anh', 350000, N'Hoàn thành', N'Vết mổ khô ráo', N'PET012', N'KH006')");

            // 17. TIEMPHONG
            System.out.println("-> Nạp TIEMPHONG...");
            st.executeUpdate("INSERT INTO TIEMPHONG (maPhieu, tenThuCung, loaiThuCung, tenChu, sdt, loaiVaccine, ngayTiem, ngayHenNhac, bacSi, trangThai, ghiChu, petID, maKH) VALUES "
                    + "(N'TP-001', N'Kitty', N'Mèo Anh lông ngắn', N'Nguyễn Minh Anh', '0903123456', N'Tiêm vaccine 4 bệnh mèo (Nobivac Ducat)', '2026-10-01', '2027-10-01', N'BS. Nguyễn Hoài Anh', N'Đã tiêm', N'Đã cấp sổ tiêm chủng', N'PET001', N'KH001'), "
                    + "(N'TP-002', N'Milu', N'Chó Golden Retriever', N'Lê Thị Cẩm Tú', '0938222111', N'Tiêm vaccine phòng Dại (Rabies)', '2026-10-05', '2027-10-05', N'BS. Võ Thanh Hùng', N'Đã tiêm', N'Bình thường', N'PET002', N'KH002'), "
                    + "(N'TP-003', N'Rocky', N'Chó Rottweiler', N'Võ Hoàng Long', '0386909808', N'Tiêm vaccine 7 bệnh chó (Vanguard Plus 5/L4)', '2026-10-20', '2027-10-20', N'BS. Ngô Gia Bảo', N'Chờ tiêm', N'Hẹn buổi chiều', N'PET004', N'KH004'), "
                    + "(N'TP-004', N'Bông Gòn', N'Chó Samoyed', N'Bùi Anh Khoa', '0919888000', N'Tiêm vaccine 5 bệnh chó', '2026-09-15', '2026-10-15', N'BS. Nguyễn Hoài Anh', N'Quá hạn', N'Cần gọi nhắc lịch', N'PET006', N'KH006')");

            System.out.println("=== NAP CSDL HOAN TAT 100%! ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

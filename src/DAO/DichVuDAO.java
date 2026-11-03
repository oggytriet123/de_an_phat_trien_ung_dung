package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.SanPham;

/**
 * Quản lý dữ liệu Dịch vụ (bảng DICHVU) và Sản phẩm (bảng SANPHAM) trong SQL Server.
 */
public class DichVuDAO {

    public DichVuDAO() {
        damBaoCauTrucBang();
    }

    public void damBaoCauTrucBang() {
        try (Connection con = ConnectDB.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("IF COL_LENGTH('DICHVU', 'thoiGianThucHien') IS NULL ALTER TABLE DICHVU ADD thoiGianThucHien INT DEFAULT 30");
            st.executeUpdate("IF COL_LENGTH('DICHVU', 'doiTuong') IS NULL ALTER TABLE DICHVU ADD doiTuong NVARCHAR(100) DEFAULT N'Tất cả thú cưng'");
            st.executeUpdate("IF COL_LENGTH('DICHVU', 'trangThai') IS NULL ALTER TABLE DICHVU ADD trangThai NVARCHAR(30) DEFAULT N'Đang cung cấp'");
            st.executeUpdate("IF COL_LENGTH('SANPHAM', 'danhMuc') IS NULL ALTER TABLE SANPHAM ADD danhMuc NVARCHAR(100) NULL");
            st.executeUpdate("IF COL_LENGTH('SANPHAM', 'donViTinh') IS NULL ALTER TABLE SANPHAM ADD donViTinh NVARCHAR(30) NULL");
            st.executeUpdate("IF COL_LENGTH('SANPHAM', 'quyCach') IS NULL ALTER TABLE SANPHAM ADD quyCach NVARCHAR(100) NULL");
            suaLoiFontSanPham(con);
            damBaoDuLieuDichVuBanDau(con);
        } catch (SQLException ignored) {
        }
    }

    private void damBaoDuLieuDichVuBanDau(Connection con) {
        try (Statement st = con.createStatement()) {
            String checkVac = "SELECT COUNT(*) FROM DICHVU WHERE loaiDV = N'Tiêm phòng'";
            try (ResultSet rs = st.executeQuery(checkVac)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    st.executeUpdate("INSERT INTO DICHVU (maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong) VALUES "
                            + "(N'VAC-001', N'Tiêm vaccine phòng Dại (Rabies)', 250000, N'Tiêm phòng', 15, N'Tiêm vaccine phòng bệnh dại định kỳ cho chó và mèo', N'Đang cung cấp', N'Chó & Mèo'), "
                            + "(N'VAC-002', N'Tiêm vaccine 4 bệnh mèo (Nobivac Ducat)', 280000, N'Tiêm phòng', 15, N'Vaccine phòng 4 bệnh truyền nhiễm nguy hiểm ở mèo', N'Đang cung cấp', N'Mèo'), "
                            + "(N'VAC-003', N'Tiêm vaccine 5 bệnh chó', 260000, N'Tiêm phòng', 15, N'Vaccine phòng 5 bệnh truyền nhiễm nguy hiểm ở chó', N'Đang cung cấp', N'Chó'), "
                            + "(N'VAC-004', N'Tiêm vaccine 7 bệnh chó (Vanguard Plus 5/L4)', 300000, N'Tiêm phòng', 15, N'Vaccine tổng hợp 7 bệnh phổ biến cho chó', N'Đang cung cấp', N'Chó')");
                }
            }
            String checkSur = "SELECT COUNT(*) FROM DICHVU WHERE loaiDV = N'Phẫu thuật'";
            try (ResultSet rs = st.executeQuery(checkSur)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    st.executeUpdate("INSERT INTO DICHVU (maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong) VALUES "
                            + "(N'SUR-001', N'Triệt sản mèo cái nội soi', 650000, N'Phẫu thuật', 45, N'Phẫu thuật nội soi triệt sản ít xâm lấn, hồi phục nhanh', N'Đang cung cấp', N'Mèo'), "
                            + "(N'SUR-002', N'Triệt sản mèo đực', 350000, N'Phẫu thuật', 30, N'Tiểu phẫu triệt sản an toàn cho mèo đực', N'Đang cung cấp', N'Mèo'), "
                            + "(N'SUR-003', N'Phẫu thuật lấy sỏi bàng quang', 1500000, N'Phẫu thuật', 60, N'Mổ hở gắp sỏi và thông đường tiết niệu', N'Đang cung cấp', N'Tất cả thú cưng'), "
                            + "(N'SUR-004', N'Phẫu thuật kết hợp xương gãy', 2500000, N'Phẫu thuật', 90, N'Cố định xương gãy bằng nẹp vis chuyên dụng', N'Đang cung cấp', N'Tất cả thú cưng'), "
                            + "(N'SUR-005', N'Cắt mộng mắt', 500000, N'Phẫu thuật', 30, N'Phẫu thuật tạo hình mộng mắt (Cherry eye) cho thú cưng', N'Đang cung cấp', N'Tất cả thú cưng'), "
                            + "(N'SUR-006', N'Mổ u nang ngoài da', 700000, N'Phẫu thuật', 40, N'Cắt bỏ khối u ngoài da và khâu thẩm mỹ', N'Đang cung cấp', N'Tất cả thú cưng')");
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private void suaLoiFontSanPham(Connection con) {
        try {
            boolean canSua = false;
            String checkSql = "SELECT TOP 1 tenSP FROM SANPHAM WHERE tenSP LIKE '%Ã%' OR tenSP LIKE '%á»%' OR tenSP LIKE '%Ä%' OR tenSP LIKE '%áº%'";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(checkSql)) {
                if (rs.next()) canSua = true;
            }
            if (canSua) {
                String[][] spData = {
                    {"SP001", "Thức ăn hạt Royal Canin Mini Adult 2kg", "Túi 2kg", "Thức ăn", "bao"},
                    {"SP002", "Pate Whiskas cá thu & cá hồi 85g", "Gói 85g", "Thức ăn", "gói"},
                    {"SP003", "Cát vệ sinh Cature đậu nành tự nhiên 6L", "Túi 6L", "Phụ kiện", "túi"},
                    {"SP004", "Sữa tắm trị ve rận Bio Derma 250ml", "Chai 250ml", "Thuốc & Y tế", "chai"},
                    {"SP005", "Vòng cổ trị ve rận Seresto cho chó", "Hộp 1 cái", "Phụ kiện", "cái"},
                    {"SP006", "Xương gặm sạch răng Pedigree DentaStix 110g", "Gói 110g", "Thức ăn", "gói"},
                    {"SP007", "Dung dịch nhỏ mắt Bioline 50ml", "Lọ 50ml", "Thuốc & Y tế", "chai"},
                    {"SP008", "Gel dinh dưỡng Nourse 120g", "Tuýp 120g", "Thuốc & Y tế", "tuýp"},
                    {"SP009", "Bánh thưởng vị thịt gà SmartHeart 100g", "Gói 100g", "Thức ăn", "gói"},
                    {"SP010", "Cần câu mèo lông vũ gắn chuông", "Cây 50cm", "Đồ chơi", "cái"},
                    {"SP011", "Bóng cao su đồ chơi gặm sạch răng cho chó", "Quả đk 7cm", "Đồ chơi", "cái"},
                    {"SP012", "Thuốc tẩy giun Sanpet cho chó mèo", "Vỉ 10 viên", "Thuốc & Y tế", "viên"},
                    {"SP013", "Xịt khử mùi chuồng trại & khay cát Forcans 300ml", "Chai 300ml", "Phụ kiện", "chai"},
                    {"SP014", "Bát ăn chống trượt inox 2 ngăn cho thú cưng", "Cái 2 ngăn", "Phụ kiện", "cái"},
                    {"SP015", "Lược chải lông tự làm sạch Pet Brush", "Cây cầm tay", "Phụ kiện", "cái"},
                    {"SP016", "Dây dắt kèm yếm ngực phản quang cho chó", "Bộ dây + yếm", "Phụ kiện", "bộ"},
                    {"SP017", "Hạt Me-O vị cá ngừ cho mèo lớn 1.2kg", "Túi 1.2kg", "Thức ăn", "bao"},
                    {"SP018", "Cỏ tươi Catnip sấy khô cho mèo", "Hũ 45g", "Đồ chơi", "hũ"},
                    {"SP019", "Tã bỉm thú cưng Dono size M (12 miếng)", "Bịch 12 miếng", "Phụ kiện", "gói"},
                    {"SP020", "Bột điện giải và men vi sinh Biotic 5g", "Gói 5g", "Thuốc & Y tế", "gói"}
                };

                String updateSql = "UPDATE SANPHAM SET tenSP = ?, quyCach = ?, danhMuc = ?, donViTinh = ? WHERE sku_maSP = ?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    for (String[] row : spData) {
                        ps.setString(1, row[1]);
                        ps.setString(2, row[2]);
                        ps.setString(3, row[3]);
                        ps.setString(4, row[4]);
                        ps.setString(5, row[0]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<SanPham> layTatCaDichVu() {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong FROM DICHVU ORDER BY maDV";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ma = rs.getString("maDV");
                String ten = rs.getString("tenDV");
                long gia = Math.round(rs.getDouble("giaTien"));
                String loai = rs.getString("loaiDV");
                if (loai == null || loai.trim().isEmpty()) loai = "Dịch vụ";
                int thoiGian = rs.getInt("thoiGianThucHien");
                String doiTuong = rs.getString("doiTuong");
                String tt = rs.getString("trangThai");

                SanPham sp = new SanPham(ma, ten, loai, gia, true);
                if (thoiGian > 0) sp.setThoiLuong(thoiGian + " phút");
                if (doiTuong != null) sp.setDoiTuong(doiTuong);
                if (tt != null && tt.contains("ngưng")) sp.setDangMo(false);
                ds.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<SanPham> layDichVuTheoLoai(String loaiDV) {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong "
                + "FROM DICHVU "
                + "WHERE (loaiDV = ? OR (loaiDV = N'Phòng bệnh' AND ? = N'Tiêm phòng')) "
                + "  AND (trangThai IS NULL OR trangThai NOT LIKE N'%ngưng%') "
                + "ORDER BY maDV";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loaiDV);
            ps.setString(2, loaiDV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maDV");
                    String ten = rs.getString("tenDV");
                    long gia = Math.round(rs.getDouble("giaTien"));
                    String loai = rs.getString("loaiDV");
                    if (loai == null || loai.trim().isEmpty()) loai = loaiDV;
                    int thoiGian = rs.getInt("thoiGianThucHien");
                    String doiTuong = rs.getString("doiTuong");
                    String tt = rs.getString("trangThai");

                    SanPham sp = new SanPham(ma, ten, loai, gia, true);
                    if (thoiGian > 0) sp.setThoiLuong(thoiGian + " phút");
                    if (doiTuong != null) sp.setDoiTuong(doiTuong);
                    if (tt != null && tt.contains("ngưng")) sp.setDangMo(false);
                    ds.add(sp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<SanPham> layDichVuTiemPhong() {
        return layDichVuTheoLoai("Tiêm phòng");
    }

    public List<SanPham> layDichVuPhauThuat() {
        return layDichVuTheoLoai("Phẫu thuật");
    }

    public List<SanPham> layTatCaSanPham() {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT sku_maSP, tenSP, donGia, soLuongTon FROM SANPHAM ORDER BY sku_maSP";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String sku = rs.getString("sku_maSP");
                String ten = rs.getString("tenSP");
                long gia = Math.round(rs.getDouble("donGia"));
                String dm = "Sản phẩm";
                if (sku.startsWith("TA") || sku.startsWith("RC") || sku.startsWith("CAT") || sku.startsWith("PG") || sku.startsWith("WK") || sku.startsWith("SH") || sku.startsWith("MO")) dm = "Thức ăn";
                else if (sku.startsWith("TY")) dm = "Đồ chơi";
                else if (sku.startsWith("MED")) dm = "Thuốc";

                SanPham sp = new SanPham(sku, ten, dm, gia, false);
                ds.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<SanPham> laySanPhamKho() {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT sku_maSP, tenSP, donGia, soLuongTon, "
                + "       ISNULL(danhMuc, N'Phụ kiện') AS danhMuc, "
                + "       ISNULL(donViTinh, N'cái') AS donViTinh, "
                + "       ISNULL(quyCach, N'Tiêu chuẩn') AS quyCach "
                + "FROM SANPHAM ORDER BY sku_maSP";
        try (Connection con = ConnectDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String sku = rs.getString("sku_maSP");
                String ten = rs.getString("tenSP");
                long gia = Math.round(rs.getDouble("donGia"));
                int ton = rs.getInt("soLuongTon");
                String dm = rs.getString("danhMuc");
                String dvt = rs.getString("donViTinh");
                String qc = rs.getString("quyCach");
                String trangThai = ton <= 0 ? "Tạm hết hàng" : (ton <= 5 ? "Sắp hết hàng" : "Đang bán");

                SanPham sp = new SanPham(sku, ten, qc, dm, ton, dvt, gia, trangThai);
                ds.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public void themDichVu(SanPham sp) throws SQLException {
        String sql = "INSERT INTO DICHVU (maDV, tenDV, giaTien, loaiDV, thoiGianThucHien, moTa, trangThai, doiTuong) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getSku());
            ps.setString(2, sp.getTen());
            ps.setDouble(3, sp.getGia());
            ps.setString(4, sp.getDanhMuc());
            int phut = 30;
            if (sp.getThoiLuong() != null) {
                try {
                    String num = sp.getThoiLuong().replaceAll("\\D", "");
                    if (!num.isEmpty()) phut = Integer.parseInt(num);
                } catch (Exception ignored) {}
            }
            ps.setInt(5, phut);
            ps.setString(6, sp.getMoTa());
            ps.setString(7, sp.isDangMo() ? "Đang cung cấp" : "Tạm ngưng");
            ps.setString(8, sp.getDoiTuong() != null ? sp.getDoiTuong() : "Tất cả thú cưng");
            ps.executeUpdate();
        }
    }

    public void capNhatDichVu(SanPham sp) throws SQLException {
        String sql = "UPDATE DICHVU SET tenDV = ?, giaTien = ?, loaiDV = ?, thoiGianThucHien = ?, moTa = ?, trangThai = ?, doiTuong = ? WHERE maDV = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getTen());
            ps.setDouble(2, sp.getGia());
            ps.setString(3, sp.getDanhMuc());
            int phut = 30;
            if (sp.getThoiLuong() != null) {
                try {
                    String num = sp.getThoiLuong().replaceAll("\\D", "");
                    if (!num.isEmpty()) phut = Integer.parseInt(num);
                } catch (Exception ignored) {}
            }
            ps.setInt(4, phut);
            ps.setString(5, sp.getMoTa());
            ps.setString(6, sp.isDangMo() ? "Đang cung cấp" : "Tạm ngưng");
            ps.setString(7, sp.getDoiTuong() != null ? sp.getDoiTuong() : "Tất cả thú cưng");
            ps.setString(8, sp.getSku());
            ps.executeUpdate();
        }
    }

    public void xoaDichVu(String maDV) throws SQLException {
        String sql = "DELETE FROM DICHVU WHERE maDV = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDV);
            ps.executeUpdate();
        }
    }

    public void themSanPham(SanPham sp) throws SQLException {
        String sql = "INSERT INTO SANPHAM (sku_maSP, tenSP, donGia, soLuongTon) VALUES (?, ?, ?, 50)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getSku());
            ps.setString(2, sp.getTen());
            ps.setDouble(3, sp.getGia());
            ps.executeUpdate();
        }
    }

    public void capNhatSanPham(SanPham sp) throws SQLException {
        String sql = "UPDATE SANPHAM SET tenSP = ?, donGia = ? WHERE sku_maSP = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getTen());
            ps.setDouble(2, sp.getGia());
            ps.setString(3, sp.getSku());
            ps.executeUpdate();
        }
    }

    public void xoaSanPham(String sku) throws SQLException {
        String sql = "DELETE FROM SANPHAM WHERE sku_maSP = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sku);
            ps.executeUpdate();
        }
    }
}

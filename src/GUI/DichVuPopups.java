package GUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Bus.SanPhamBUS;
import Entity.SanPham;

/** Popup của trang Quản lý dịch vụ: form thêm / sửa dịch vụ hoặc sản phẩm và xem chi tiết. */
final class DichVuPopups {
    private DichVuPopups() {}

    /** Trạng thái các ô của form, giữ lại khi mở form tạo nhóm mới rồi quay lại. */
    private record TrangThaiForm(String ten, String gia, String thoiLuong, String doiTuong, boolean dangMo, String nhom) { }

    static String nhanTrangThai(boolean dichVu, boolean dangMo) {
        return dichVu ? (dangMo ? "Đang cung cấp" : "Tạm ngưng") : (dangMo ? "Đang bán" : "Ngừng bán");
    }

    /**
     * Form thêm (sua = null) hoặc sửa một dịch vụ / sản phẩm.
     * @param dichVu        true: dịch vụ (có thời lượng, đối tượng); false: sản phẩm
     * @param nhomMacDinh   nhóm chọn sẵn khi thêm mới (thường là nhóm đang lọc)
     * @param khiXong       gọi sau khi lưu thành công (popup đã đóng)
     */
    static void formMuc(Component host, SanPhamBUS bus, boolean dichVu, SanPham sua, String nhomMacDinh, Consumer<SanPham> khiXong) {
        TrangThaiForm st = sua == null
                ? new TrangThaiForm("", "", dichVu ? "30 phút" : "", SanPhamBUS.DOI_TUONG[0], true, nhomMacDinh)
                : new TrangThaiForm(sua.getTen(), String.valueOf(sua.getDonGia()), sua.getThoiLuong(), sua.getDoiTuong(),
                        sua.isDangMo(), sua.getDanhMuc());
        moForm(host, bus, dichVu, sua, st, khiXong);
    }

    private static void moForm(Component host, SanPhamBUS bus, boolean dichVu, SanPham sua, TrangThaiForm st, Consumer<SanPham> khiXong) {
        String loaiTen = dichVu ? "dịch vụ" : "sản phẩm";
        List<String> nhomDs = bus.layNhom(dichVu);
        String[] nhomChon = {nhomDs.contains(st.nhom()) ? st.nhom() : nhomDs.isEmpty() ? "" : nhomDs.get(0)};
        String[] dtChon = {st.doiTuong()};
        boolean[] moChon = {st.dangMo()};

        PlaceholderField tfTen = new PlaceholderField(dichVu ? "Ví dụ: Cắt móng, Tắm và cắt tỉa lông" : "Ví dụ: Cát vệ sinh cho mèo");
        PlaceholderField tfGia = new PlaceholderField("Ví dụ: 150000");
        PlaceholderField tfTl = new PlaceholderField("Ví dụ: 30 phút, 2 giờ, 1 ngày");
        tfTen.setText(st.ten());
        tfGia.setText(st.gia());
        tfTl.setText(st.thoiLuong());
        GioiHanNhap.ap(tfTen, 60, null);
        GioiHanNhap.ap(tfGia, 12, "[0-9.,]");
        GioiHanNhap.ap(tfTl, 12, null);
        FieldBox bTen = new FieldBox(tfTen, Color.WHITE, 42);
        FieldBox bGia = new FieldBox(tfGia, Color.WHITE, 42);
        FieldBox bTl = new FieldBox(tfTl, Color.WHITE, 42);
        JLabel err = HoaDonPopups.errorLabel();
        JLabel sku = new JLabel(" ");
        sku.setFont(Theme.font(Font.PLAIN, 12));
        sku.setForeground(Theme.MUTED);

        // ---- nhóm (nút) + nút tạo nhóm mới
        WrapPanel chipsNhom = new WrapPanel(8, 8, 568);
        List<Pill> pNhom = new ArrayList<>();
        Runnable capNhatNhom = () -> {
            for (Pill p : pNhom) p.setSelected(p.getText().equals(nhomChon[0]));
            sku.setText(sua == null
                    ? "Mã tự động: " + bus.maTiepTheo(nhomChon[0])
                    : "Mã " + sua.getSku() + " (không đổi khi sửa)");
        };
        for (String ten : nhomDs) {
            Pill p = new Pill(ten);
            p.addActionListener(e -> { nhomChon[0] = ten; capNhatNhom.run(); });
            pNhom.add(p);
            chipsNhom.add(p);
        }
        RoundedButton nhomMoi = new RoundedButton("+  Nhóm mới", Theme.PRIMARY_SOFT, new Color(0xDCE8FA),
                Theme.PRIMARY, Theme.PRIMARY_LINE, 16);
        nhomMoi.setFont(Theme.font(Font.BOLD, 12));
        nhomMoi.setPreferredSize(new Dimension(106, 32));
        chipsNhom.add(nhomMoi);
        capNhatNhom.run();

        // ---- đối tượng (chỉ dịch vụ) + trạng thái
        WrapPanel chipsDt = new WrapPanel(8, 8, 568);
        List<Pill> pDt = new ArrayList<>();
        Runnable capNhatDt = () -> { for (Pill p : pDt) p.setSelected(p.getText().equals(dtChon[0])); };
        if (dichVu) {
            for (String d : SanPhamBUS.DOI_TUONG) {
                Pill p = new Pill(d);
                p.addActionListener(e -> { dtChon[0] = d; capNhatDt.run(); });
                pDt.add(p);
                chipsDt.add(p);
            }
            capNhatDt.run();
        }
        WrapPanel chipsTt = new WrapPanel(8, 8, 568);
        List<Pill> pTt = new ArrayList<>();
        Runnable capNhatTt = () -> {
            for (int i = 0; i < pTt.size(); i++) pTt.get(i).setSelected((i == 0) == moChon[0]);
        };
        for (boolean mo : new boolean[] {true, false}) {
            Pill p = new Pill(nhanTrangThai(dichVu, mo));
            p.addActionListener(e -> { moChon[0] = mo; capNhatTt.run(); });
            pTt.add(p);
            chipsTt.add(p);
        }
        capNhatTt.run();

        int cao = 470 + chipsNhom.getPreferredSize().height + chipsTt.getPreferredSize().height
                + (dichVu ? 36 + chipsDt.getPreferredSize().height : 0);
        String tieuDe = (sua == null ? "Thêm " : "Chỉnh sửa ") + loaiTen;
        RoundedPanel card = HoaDonPopups.card(640, cao, tieuDe, sua == null
                ? "Điền thông tin để thêm vào danh mục. Mã được tạo tự động."
                : "Cập nhật thông tin của " + sua.getTen() + ".");

        DocumentListener xoaLoi = new DocumentListener() {
            private void c() { err.setText(" "); bTen.setError(false); bGia.setError(false); bTl.setError(false); }
            @Override public void insertUpdate(DocumentEvent e) { c(); }
            @Override public void removeUpdate(DocumentEvent e) { c(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        };
        for (PlaceholderField f : new PlaceholderField[] {tfTen, tfGia, tfTl}) f.getDocument().addDocumentListener(xoaLoi);

        // ---- bố cục
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 0, 0, 0));
        JComponent gTen = HoaDonPopups.nhom("Tên " + loaiTen + " *", bTen);
        JLabel lNhom = HoaDonPopups.label(dichVu ? "Nhóm dịch vụ *" : "Loại hàng *");
        JPanel hang = new JPanel(new GridLayout(1, dichVu ? 2 : 1, 14, 0));
        hang.setOpaque(false);
        hang.add(HoaDonPopups.nhom("Đơn giá (đ) *", bGia));
        if (dichVu) hang.add(HoaDonPopups.nhom("Thời lượng *", bTl));
        JLabel lDt = HoaDonPopups.label("Đối tượng áp dụng");
        JLabel lTt = HoaDonPopups.label("Trạng thái");
        for (JComponent c : new JComponent[] {gTen, lNhom, chipsNhom, sku, hang, lDt, chipsDt, lTt, chipsTt, err}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        gTen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        body.add(gTen);
        body.add(Box.createVerticalStrut(12));
        body.add(lNhom);
        body.add(Box.createVerticalStrut(8));
        body.add(chipsNhom);
        body.add(Box.createVerticalStrut(6));
        body.add(sku);
        body.add(Box.createVerticalStrut(12));
        body.add(hang);
        if (dichVu) {
            body.add(Box.createVerticalStrut(12));
            body.add(lDt);
            body.add(Box.createVerticalStrut(8));
            body.add(chipsDt);
        }
        body.add(Box.createVerticalStrut(12));
        body.add(lTt);
        body.add(Box.createVerticalStrut(8));
        body.add(chipsTt);
        body.add(Box.createVerticalStrut(6));
        body.add(err);
        card.add(body, BorderLayout.CENTER);

        // ---- nút
        RoundedButton huy = HoaDonPopups.secondary("Hủy", 130);
        RoundedButton luu = HoaDonPopups.primary(sua == null ? "Thêm " + loaiTen : "Lưu thay đổi", 170);
        Runnable dong = () -> Overlay.dong(host);
        huy.addActionListener(e -> dong.run());
        Runnable xong = () -> {
            long gia = HoaDonPopups.docSo(tfGia.getText());
            if (gia < 0) {
                err.setText(tfGia.getText().isBlank() ? "Vui lòng nhập đơn giá." : "Đơn giá phải là số.");
                bGia.setError(true);
                tfGia.requestFocusInWindow();
                return;
            }
            try {
                SanPham r = sua == null
                        ? bus.tao(tfTen.getText(), nhomChon[0], gia, tfTl.getText(), dtChon[0], moChon[0])
                        : bus.capNhat(sua.getSku(), tfTen.getText(), nhomChon[0], gia, tfTl.getText(), dtChon[0], moChon[0]);
                Overlay.dong(host);
                khiXong.accept(r);
            } catch (IllegalArgumentException ex) {
                String msg = ex.getMessage();
                err.setText(msg);
                if (msg.contains("Thời lượng")) bTl.setError(true);
                else if (msg.contains("Đơn giá")) bGia.setError(true);
                else bTen.setError(true);
            }
        };
        luu.addActionListener(e -> xong.run());
        for (PlaceholderField f : new PlaceholderField[] {tfTen, tfGia, tfTl}) f.addActionListener(e -> xong.run());

        // tạo nhóm mới giữa chừng: giữ nguyên mọi thứ đang nhập, chọn sẵn nhóm vừa tạo
        nhomMoi.addActionListener(e -> {
            TrangThaiForm hienTai = new TrangThaiForm(tfTen.getText(), tfGia.getText(), tfTl.getText(), dtChon[0], moChon[0], nhomChon[0]);
            HoaDonPopups.taoDanhMuc(host, bus, dichVu,
                    moi -> moForm(host, bus, dichVu, sua, new TrangThaiForm(hienTai.ten(), hienTai.gia(), hienTai.thoiLuong(),
                            hienTai.doiTuong(), hienTai.dangMo(), moi), khiXong),
                    () -> moForm(host, bus, dichVu, sua, hienTai, khiXong));
        });
        card.add(HoaDonPopups.buttons(huy, luu), BorderLayout.SOUTH);

        Overlay.hien(host, card, dong);
        SwingUtilities.invokeLater(tfTen::requestFocusInWindow);
    }

    /** Bảng thông tin chỉ đọc (xem chi tiết dịch vụ / sản phẩm / mã giảm giá). */
    static void chiTiet(Component host, String tieuDe, String phuDe, String[][] dong) {
        RoundedPanel card = HoaDonPopups.card(560, 230 + dong.length * 34, tieuDe, phuDe);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 0, 0, 0));
        for (String[] r : dong) {
            body.add(HoaDonPopups.infoRow(r[0], r[1]));
            body.add(Box.createVerticalStrut(10));
        }
        card.add(body, BorderLayout.CENTER);
        RoundedButton dong2 = HoaDonPopups.primary("Đóng", 130);
        Runnable d = () -> Overlay.dong(host);
        dong2.addActionListener(e -> d.run());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.add(dong2);
        card.add(south, BorderLayout.SOUTH);
        Overlay.hien(host, card, d);
    }
}

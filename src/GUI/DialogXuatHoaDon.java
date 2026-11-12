package GUI;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import Bus.KhachHangBUS;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.KhachHang;

/**
 * Hộp thoại hiển thị bản xem trước, xuất file và in hóa đơn thanh toán Pet Station / PetCare.
 * Thiết kế chuẩn mẫu in nhiệt siêu thị POS K80:
 * - Danh sách hàng hóa kèm SKU
 * - Tổng cộng / Tổng tiền khách trả / Phương thức / Tiền trả lại (tiền thừa)
 * - Thông tin hội viên & Giá trị thưởng tích lũy (loyalty points)
 * - Thuế GTGT, lưu ý xuất hóa đơn trong ngày và mã QR Code ở chân hóa đơn
 */
public class DialogXuatHoaDon extends JDialog {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HoaDon hd;
    private final Runnable onDong;
    private final ReceiptPaperPanel paperPanel;
    private final RoundedButton btnIn;
    private final RoundedButton btnXuat;
    private final RoundedButton btnDong;

    public static void hienThi(Window owner, HoaDon hd, Runnable onDong) {
        DialogXuatHoaDon dlg = new DialogXuatHoaDon(owner, hd, onDong);
        dlg.setVisible(true);
    }

    public DialogXuatHoaDon(Window owner, HoaDon hd, Runnable onDong) {
        super(owner, "Hóa Đơn Thanh Toán - " + hd.getMaHD(), ModalityType.APPLICATION_MODAL);
        this.hd = hd;
        this.onDong = onDong;

        setSize(580, 800);
        setMinimumSize(new Dimension(520, 680));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG);

        // 1. Header Bar
        JPanel pnlHeader = taoHeader();
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Center: Receipt Paper Preview in ScrollPane
        paperPanel = new ReceiptPaperPanel(hd);
        JPanel wrapPaper = new JPanel(new GridBagLayout());
        wrapPaper.setOpaque(false);
        wrapPaper.setBorder(new EmptyBorder(16, 16, 16, 16));
        wrapPaper.add(paperPanel);

        JScrollPane scroll = new JScrollPane(wrapPaper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        SlimScrollBarUI.apply(scroll);
        add(scroll, BorderLayout.CENTER);

        // 3. Bottom Action Bar (thiết kế 2 dòng tách biệt, ngăn ngừa hoàn toàn lỗi đè chữ)
        btnDong = new RoundedButton("Đóng (Esc)", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        btnDong.setPreferredSize(new Dimension(110, 40));
        btnDong.addActionListener(e -> dong());

        btnXuat = new RoundedButton("Xuất PDF / Tệp", Theme.BLUE_BG, new Color(0xD2E3ED), Theme.BLUE_TEXT, null, 8);
        btnXuat.setPreferredSize(new Dimension(135, 40));
        btnXuat.addActionListener(e -> xuatFile());

        btnIn = new RoundedButton("In hóa đơn", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        btnIn.setPreferredSize(new Dimension(135, 40));
        btnIn.addActionListener(e -> inHoaDon());

        JPanel pnlSouth = new JPanel();
        pnlSouth.setLayout(new BoxLayout(pnlSouth, BoxLayout.Y_AXIS));
        pnlSouth.setBackground(Color.WHITE);
        pnlSouth.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(10, 20, 14, 20)
        ));

        // Hàng 1: Tip hướng dẫn in ấn căn giữa
        JPanel pnlTip = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlTip.setOpaque(false);
        JLabel lblTip = new JLabel("💡 Chuẩn hóa đơn nhiệt POS K80 (80mm) · Laser A4/A5 · Xuất file PDF / HTML");
        lblTip.setFont(Theme.font(Font.PLAIN, 12));
        lblTip.setForeground(Theme.MUTED);
        pnlTip.add(lblTip);

        // Hàng 2: Các nút hành động căn phải
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        pnlBtns.add(btnDong);
        pnlBtns.add(btnXuat);
        pnlBtns.add(btnIn);

        pnlSouth.add(pnlTip);
        pnlSouth.add(Box.createVerticalStrut(10));
        pnlSouth.add(pnlBtns);
        add(pnlSouth, BorderLayout.SOUTH);

        // Phím tắt ESC để đóng, Ctrl+P để in
        JRootPane root = getRootPane();
        root.registerKeyboardAction(e -> dong(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        root.registerKeyboardAction(e -> inHoaDon(), KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onDong != null) {
                    onDong.run();
                }
            }
        });
    }

    private JPanel taoHeader() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JPanel pnlTitle = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("Hóa đơn thanh toán - " + hd.getMaHD());
        lblTitle.setFont(Theme.font(Font.BOLD, 16));
        lblTitle.setForeground(Theme.TEXT);

        JLabel lblSub = new JLabel("Xem trước mẫu in POS nhiệt, xuất file lưu tệp hoặc in trực tiếp");
        lblSub.setFont(Theme.font(Font.PLAIN, 12));
        lblSub.setForeground(Theme.MUTED);

        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSub);
        pnl.add(pnlTitle, BorderLayout.WEST);

        JPanel pnlBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        pnlBadge.setOpaque(false);
        JLabel lblStatus = new JLabel(hd.daThanhToan() ? "ĐÃ THANH TOÁN" : "CHƯA THANH TOÁN");
        lblStatus.setFont(Theme.font(Font.BOLD, 11));
        lblStatus.setForeground(hd.daThanhToan() ? Theme.GREEN : Theme.RED);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(hd.daThanhToan() ? Theme.GREEN : Theme.RED, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        lblStatus.setOpaque(true);
        lblStatus.setBackground(hd.daThanhToan() ? Theme.GREEN_BG : Theme.RED_BG);
        pnlBadge.add(lblStatus);
        pnl.add(pnlBadge, BorderLayout.EAST);

        return pnl;
    }

    private void dong() {
        dispose();
    }

    /** In trực tiếp hóa đơn qua hệ thống máy in (POS / Laser / Microsoft Print to PDF). */
    private void inHoaDon() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("PetCare_HoaDon_" + hd.getMaHD());

        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        paper.setImageableArea(18, 18, paper.getWidth() - 36, paper.getHeight() - 36);
        pf.setPaper(paper);

        job.setPrintable(paperPanel, pf);

        if (job.printDialog()) {
            btnIn.setEnabled(false);
            btnIn.setText("Đang in...");
            new Thread(() -> {
                try {
                    job.print();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Đã gửi lệnh in hóa đơn " + hd.getMaHD() + " tới máy in thành công!",
                                "In hóa đơn thành công", JOptionPane.INFORMATION_MESSAGE);
                        btnIn.setEnabled(true);
                        btnIn.setText("In hóa đơn");
                    });
                } catch (PrinterException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Không thể in hóa đơn: " + ex.getMessage(),
                                "Lỗi in ấn", JOptionPane.ERROR_MESSAGE);
                        btnIn.setEnabled(true);
                        btnIn.setText("In hóa đơn");
                    });
                }
            }).start();
        }
    }

    /** Xuất hóa đơn ra file PDF hoặc HTML có thể xem và in lại trên mọi thiết bị. */
    private void xuatFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu hóa đơn thanh toán");
        fc.setSelectedFile(new File("HoaDon_" + hd.getMaHD() + ".html"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Trang in Hóa đơn (*.html)", "html", "htm"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Tệp tài liệu PDF (*.pdf)", "pdf"));

        int r = fc.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        String path = f.getAbsolutePath();

        if (path.toLowerCase().endsWith(".pdf")) {
            xuatPDFThucTe(f);
            return;
        }

        if (!path.toLowerCase().endsWith(".html") && !path.toLowerCase().endsWith(".htm")) {
            f = new File(path + ".html");
        }

        try {
            ghiFileHtml(f);
            int opt = JOptionPane.showConfirmDialog(this,
                    "Đã xuất hóa đơn thành công ra tệp:\n" + f.getAbsolutePath() + "\n\nBạn có muốn mở ngay không?",
                    "Xuất hóa đơn thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (opt == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu tệp: " + ex.getMessage(),
                    "Lỗi xuất hóa đơn", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatPDFThucTe(File targetPdf) {
        PrintService pdfService = timMayInPDF();
        if (pdfService == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn máy in 'Microsoft Print to PDF' trong hộp thoại in để lưu file PDF.",
                    "Lưu PDF", JOptionPane.INFORMATION_MESSAGE);
            inHoaDon();
            return;
        }

        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(pdfService);
            job.setJobName("PetCare_HoaDon_" + hd.getMaHD());

            PageFormat pf = job.defaultPage();
            Paper paper = pf.getPaper();
            paper.setImageableArea(18, 18, paper.getWidth() - 36, paper.getHeight() - 36);
            pf.setPaper(paper);
            job.setPrintable(paperPanel, pf);

            PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
            attr.add(new Destination(targetPdf.toURI()));

            job.print(attr);
            int opt = JOptionPane.showConfirmDialog(this,
                    "Đã xuất file PDF thành công:\n" + targetPdf.getAbsolutePath() + "\n\nBạn có muốn mở ngay không?",
                    "Xuất PDF thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (opt == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(targetPdf);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu PDF: " + ex.getMessage(),
                    "Lỗi xuất PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PrintService timMayInPDF() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService s : services) {
            String name = s.getName().toLowerCase();
            if (name.contains("pdf")) {
                return s;
            }
        }
        return null;
    }

    private static String imageToBase64(Image img) {
        if (img == null) return "";
        try {
            BufferedImage bimg;
            if (img instanceof BufferedImage) {
                bimg = (BufferedImage) img;
            } else {
                bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = bimg.createGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bimg, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    private void ghiFileHtml(File f) throws IOException {
        KhachHang kh = hd.getKhachHang();
        if (kh == null && hd.getSdt() != null && !hd.getSdt().trim().isEmpty()) {
            try { kh = new KhachHangBUS().timTheoSdtAnToan(hd.getSdt()); } catch (Exception ignored) {}
        }
        if (kh == null && hd.getMaKH() != null && !hd.getMaKH().trim().isEmpty()) {
            try { kh = new KhachHangBUS().timTheoMa(hd.getMaKH()); } catch (Exception ignored) {}
        }

        String tenKhach = (kh != null && kh.getHoTen() != null && !kh.getHoTen().isEmpty()) ? kh.getHoTen() : (hd.getTenKH().isEmpty() ? "Khách vãng lai" : hd.getTenKH());
        String maThe = (kh != null && kh.getMaKH() != null && !kh.getMaKH().isEmpty()) ? kh.getMaKH() : (hd.getMaKH().isEmpty() ? "KH---" : hd.getMaKH());
        String sdtHienThi = (kh != null && kh.getSoDienThoai() != null && !kh.getSoDienThoai().isEmpty()) ? kh.getSoDienThoai() : hd.getSdt();
        if (!sdtHienThi.isEmpty()) maThe += " · " + sdtHienThi;

        int diemTichLuy = kh != null ? kh.getDiemTichLuy() : 0;
        int diemCong = (int) KhachHangBUS.diemTuSoTien(hd.canThanhToan());
        String hangThe = kh != null ? kh.getHangThe() : "Thường";

        long tongCong = hd.canThanhToan();
        long tienKhachTra = hd.getTienKhachDua();
        long tienTraLai = hd.getTienTraLai();
        String phuongThuc = hd.getPhuongThuc() != null ? hd.getPhuongThuc() : "Tiền mặt";
        String ngay = hd.getNgayThanhToan() != null ? DT_FMT.format(hd.getNgayThanhToan()) : DT_FMT.format(hd.getNgayTao());

        Image qrImg = VietQRUtil.taoAnhQR(tongCong, hd.getMaHD(), 140);
        String qrBase64 = imageToBase64(qrImg);

        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            w.println("<!DOCTYPE html>");
            w.println("<html lang=\"vi\">");
            w.println("<head>");
            w.println("<meta charset=\"UTF-8\">");
            w.println("<title>Hóa đơn " + hd.getMaHD() + " - Pet Station</title>");
            w.println("<style>");
            w.println("  body { font-family: 'Courier New', Consolas, 'Segoe UI', monospace; background: #eef2f5; margin: 0; padding: 20px; color: #111; }");
            w.println("  .ticket { max-width: 380px; margin: 0 auto; background: #fff; padding: 22px 20px; border-radius: 4px; box-shadow: 0 4px 14px rgba(0,0,0,0.08); font-size: 13px; line-height: 1.35; }");
            w.println("  .center { text-align: center; }");
            w.println("  .brand { font-size: 15px; font-weight: bold; text-transform: uppercase; margin-bottom: 3px; }");
            w.println("  .sub { font-size: 11px; color: #444; line-height: 1.3; }");
            w.println("  .dash { border-top: 1px dashed #777; margin: 10px 0; }");
            w.println("  .title { font-size: 15px; font-weight: bold; margin: 4px 0 2px 0; }");
            w.println("  .code { font-size: 13px; font-weight: bold; }");
            w.println("  .row { display: flex; justify-content: space-between; margin: 3px 0; }");
            w.println("  .row-indent { display: flex; justify-content: space-between; margin: 2px 0 2px 14px; font-size: 12.5px; }");
            w.println("  .bold { font-weight: bold; }");
            w.println("  table { width: 100%; border-collapse: collapse; font-size: 12px; margin: 6px 0; }");
            w.println("  th { text-align: left; padding: 4px 0; border-bottom: 1px dashed #777; font-weight: bold; }");
            w.println("  td { padding: 4px 0; vertical-align: top; }");
            w.println("  .sku-tag { font-size: 10px; color: #555; font-family: monospace; }");
            w.println("  .num { text-align: right; white-space: nowrap; }");
            w.println("  .total-block { font-size: 13.5px; }");
            w.println("  .qr-box { text-align: center; margin: 10px 0 6px 0; }");
            w.println("  .barcode { text-align: center; font-weight: bold; letter-spacing: 2px; font-size: 14px; margin-top: 6px; }");
            w.println("  @media print {");
            w.println("    body { background: #fff; padding: 0; }");
            w.println("    .ticket { box-shadow: none; padding: 0; width: 100%; max-width: 100%; }");
            w.println("  }");
            w.println("</style>");
            w.println("</head>");
            w.println("<body>");
            w.println("<div class=\"ticket\">");
            w.println("  <div class=\"center\">");
            w.println("    <div class=\"brand\">PHÒNG KHÁM THÚ Y & PET SHOP PETCARE</div>");
            w.println("    <div class=\"sub\">12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM<br>Hotline: 0901.000.005 &nbsp;|&nbsp; petcare.vn</div>");
            w.println("    <div class=\"dash\"></div>");
            w.println("    <div class=\"title\">PHIẾU THANH TOÁN</div>");
            w.println("    <div class=\"code\">Mã HĐ: " + hd.getMaHD() + "</div>");
            w.println("  </div>");
            w.println("  <div class=\"dash\"></div>");

            w.println("  <div class=\"row\"><span>Thời gian:</span><span>" + ngay + "</span></div>");
            w.println("  <div class=\"row\"><span>Thu ngân:</span><span>" + (hd.getMaNV().isEmpty() ? "NV001" : hd.getMaNV()) + "</span></div>");
            w.println("  <div class=\"dash\"></div>");

            w.println("  <table>");
            w.println("    <thead><tr><th>Mặt hàng / Dịch vụ</th><th class=\"num\">SL</th><th class=\"num\">Đơn giá</th><th class=\"num\">T.Tiền</th></tr></thead>");
            w.println("    <tbody>");
            for (ChiTietHoaDon d : hd.getDong()) {
                String sku = d.getSanPham().getSku() != null ? d.getSanPham().getSku() : "";
                w.println("      <tr>");
                w.println("        <td><span class=\"sku-tag\">" + sku + "</span><br>" + d.getSanPham().getTen() + "</td>");
                w.println("        <td class=\"num\">" + d.getSoLuong() + "</td>");
                w.println("        <td class=\"num\">" + Theme.money(d.getSanPham().getDonGia()) + "</td>");
                w.println("        <td class=\"num bold\">" + Theme.money(Math.round(d.getDonGia() * d.getSoLuong())) + "</td>");
                w.println("      </tr>");
            }
            w.println("    </tbody>");
            w.println("  </table>");

            w.println("  <div class=\"dash\"></div>");
            if (hd.tamTinh() != hd.canThanhToan()) {
                w.println("  <div class=\"row\"><span>Tạm tính:</span><span>" + Theme.money(hd.tamTinh()) + "</span></div>");
                String giamTxt = hd.getMaGiamGia() == null ? "Giảm giá:" : "Giảm giá (" + hd.getMaGiamGia().getMa() + "):";
                w.println("  <div class=\"row\"><span>" + giamTxt + "</span><span>" + (hd.tienGiam() > 0 ? "-" + Theme.money(hd.tienGiam()) : Theme.money(0)) + "</span></div>");
                w.println("  <div class=\"dash\"></div>");
            }

            // Phần tổng kết tiền chuẩn mẫu siêu thị (Tổng cộng, Tổng tiền khách trả, Phương thức, Tiền trả lại)
            w.println("  <div class=\"total-block\">");
            w.println("    <div class=\"row bold\" style=\"font-size:14px;\"><span>Tổng cộng:</span><span>" + Theme.money(tongCong) + "</span></div>");
            w.println("    <div class=\"row bold\"><span>Tổng tiền khách trả:</span><span>" + Theme.money(tienKhachTra) + "</span></div>");
            w.println("    <div class=\"row-indent\"><span>+ " + phuongThuc + ":</span><span>" + Theme.money(tienKhachTra) + "</span></div>");
            w.println("    <div class=\"row-indent bold\"><span>+ Tiền trả lại:</span><span>" + Theme.money(tienTraLai) + "</span></div>");
            w.println("  </div>");

            // Phần thông tin khách hàng & Tích lũy điểm (Y hệt mẫu ảnh)
            w.println("  <div class=\"dash\"></div>");
            w.println("  <div class=\"row\"><span>Tên khách:</span><span class=\"bold\">" + tenKhach + "</span></div>");
            w.println("  <div class=\"row\"><span>Mã thẻ:</span><span>" + maThe + "</span></div>");
            if (kh != null) {
                w.println("  <div class=\"row\"><span>Hạng thẻ:</span><span>" + hangThe + "</span></div>");
                w.println("  <div class=\"row\"><span>Giá trị thưởng tích lũy:</span><span class=\"bold\">" + String.format("%,d điểm", diemTichLuy) + "</span></div>");
                if (diemCong > 0) {
                    w.println("  <div class=\"row-indent\"><span>+ Điểm tích lũy đợt này:</span><span>+" + String.format("%,d điểm", diemCong) + "</span></div>");
                }
            }
            w.println("  <div class=\"row\" style=\"font-size:11px;color:#555;\"><span>&nbsp;</span><span>Tích lũy đến: " + ngay + "</span></div>");

            // Phần lưu ý pháp lý & Chân trang
            w.println("  <div class=\"dash\"></div>");
            w.println("  <div class=\"center sub\">(Giá đã bao gồm thuế GTGT)</div>");
            w.println("  <div class=\"center sub\">Lưu ý: Siêu thị / Cửa hàng chỉ xuất hóa đơn trong ngày</div>");
            w.println("  <div class=\"center bold\" style=\"margin-top:4px;\">Trân trọng cảm ơn Quý khách!</div>");
            w.println("  <div class=\"dash\"></div>");

            w.println("  <div class=\"center sub\" style=\"margin-top:6px;\">Khách hàng vui lòng quét mã QRcode<br>để mua hàng qua Zalo của cửa hàng</div>");
            if (!qrBase64.isEmpty()) {
                w.println("  <div class=\"qr-box\"><img src=\"" + qrBase64 + "\" style=\"width:125px;height:125px;\"></div>");
            }
            w.println("  <div class=\"barcode\">* " + hd.getMaHD() + " *</div>");
            w.println("</div>");
            w.println("</body>");
            w.println("</html>");
        }
    }

    /**
     * Panel đại diện cho tờ hóa đơn giấy (Receipt Paper).
     * Triển khai Printable để in trực tiếp qua AWT Graphics2D chuẩn vector.
     */
    public static class ReceiptPaperPanel extends JPanel implements Printable {
        private final HoaDon hd;

        public ReceiptPaperPanel(HoaDon hd) {
            this.hd = hd;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0xD0D7DE), 1, true),
                    new EmptyBorder(22, 20, 22, 20)
            ));
            setPreferredSize(new Dimension(420, tinhChieuCao()));
            setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

            buildComponents();
        }

        private int tinhChieuCao() {
            int base = 620;
            base += hd.getDong().size() * 42;
            return Math.max(680, base);
        }

        private void buildComponents() {
            KhachHang kh = hd.getKhachHang();
            if (kh == null && hd.getSdt() != null && !hd.getSdt().trim().isEmpty()) {
                try { kh = new KhachHangBUS().timTheoSdtAnToan(hd.getSdt()); } catch (Exception ignored) {}
            }
            if (kh == null && hd.getMaKH() != null && !hd.getMaKH().trim().isEmpty()) {
                try { kh = new KhachHangBUS().timTheoMa(hd.getMaKH()); } catch (Exception ignored) {}
            }

            String tenKhach = (kh != null && kh.getHoTen() != null && !kh.getHoTen().isEmpty()) ? kh.getHoTen() : (hd.getTenKH().isEmpty() ? "Khách vãng lai" : hd.getTenKH());
            String maThe = (kh != null && kh.getMaKH() != null && !kh.getMaKH().isEmpty()) ? kh.getMaKH() : (hd.getMaKH().isEmpty() ? "KH---" : hd.getMaKH());
            String sdtHienThi = (kh != null && kh.getSoDienThoai() != null && !kh.getSoDienThoai().isEmpty()) ? kh.getSoDienThoai() : hd.getSdt();
            if (!sdtHienThi.isEmpty()) maThe += " · " + sdtHienThi;

            int diemTichLuy = kh != null ? kh.getDiemTichLuy() : 0;
            int diemCong = (int) KhachHangBUS.diemTuSoTien(hd.canThanhToan());
            String hangThe = kh != null ? kh.getHangThe() : "Thường";

            long tongCong = hd.canThanhToan();
            long tienKhachTra = hd.getTienKhachDua();
            long tienTraLai = hd.getTienTraLai();
            String phuongThuc = hd.getPhuongThuc() != null ? hd.getPhuongThuc() : "Tiền mặt";
            String ngay = hd.getNgayThanhToan() != null ? DT_FMT.format(hd.getNgayThanhToan()) : DT_FMT.format(hd.getNgayTao());

            // 1. Header thương hiệu
            add(centerLabel("PHÒNG KHÁM THÚ Y & PET SHOP PETCARE", Font.BOLD, 14, Theme.DARK));
            add(Box.createVerticalStrut(3));
            add(centerLabel("12 Nguyễn Văn Bảo, P.4, Q. Gò Vấp, TP.HCM", Font.PLAIN, 11, Theme.MUTED));
            add(centerLabel("Hotline: 0901.000.005  ·  petcare.vn", Font.PLAIN, 11, Theme.MUTED));
            add(Box.createVerticalStrut(8));
            add(new DashedLine());
            add(Box.createVerticalStrut(6));

            // 2. Tiêu đề hóa đơn
            add(centerLabel("PHIẾU THANH TOÁN", Font.BOLD, 15, Theme.TEXT));
            add(Box.createVerticalStrut(2));
            add(centerLabel("Mã HĐ: " + hd.getMaHD(), Font.BOLD, 13, Theme.PRIMARY));
            add(Box.createVerticalStrut(6));
            add(new DashedLine());
            add(Box.createVerticalStrut(6));

            // 3. Thông tin chung
            add(metaRow("Thời gian:", ngay));
            add(metaRow("Thu ngân:", hd.getMaNV().isEmpty() ? "NV001" : hd.getMaNV()));
            add(Box.createVerticalStrut(6));
            add(new DashedLine());
            add(Box.createVerticalStrut(6));

            // 4. Bảng danh sách mặt hàng kèm SKU
            JPanel tableHead = new JPanel(new BorderLayout());
            tableHead.setOpaque(false);
            JLabel th1 = new JLabel("Mặt hàng / Dịch vụ");
            th1.setFont(Theme.font(Font.BOLD, 11));
            th1.setForeground(Theme.MUTED);

            JPanel thRight = new JPanel(new GridLayout(1, 3, 6, 0));
            thRight.setOpaque(false);
            thRight.setPreferredSize(new Dimension(210, 18));
            JLabel thSl = new JLabel("SL", SwingConstants.CENTER);
            JLabel thGia = new JLabel("Đơn giá", SwingConstants.RIGHT);
            JLabel thTien = new JLabel("T.Tiền", SwingConstants.RIGHT);
            thSl.setFont(Theme.font(Font.BOLD, 11)); thSl.setForeground(Theme.MUTED);
            thGia.setFont(Theme.font(Font.BOLD, 11)); thGia.setForeground(Theme.MUTED);
            thTien.setFont(Theme.font(Font.BOLD, 11)); thTien.setForeground(Theme.MUTED);
            thRight.add(thSl);
            thRight.add(thGia);
            thRight.add(thTien);

            tableHead.add(th1, BorderLayout.CENTER);
            tableHead.add(thRight, BorderLayout.EAST);
            add(tableHead);
            add(Box.createVerticalStrut(4));

            for (ChiTietHoaDon d : hd.getDong()) {
                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(3, 0, 3, 0));

                JPanel pnlName = new JPanel();
                pnlName.setLayout(new BoxLayout(pnlName, BoxLayout.Y_AXIS));
                pnlName.setOpaque(false);

                String sku = d.getSanPham().getSku() != null ? d.getSanPham().getSku() : "";
                JLabel lblSku = new JLabel(sku);
                lblSku.setFont(Theme.font(Font.BOLD, 10));
                lblSku.setForeground(new Color(0x718096));

                JLabel lblTen = new JLabel(d.getSanPham().getTen());
                lblTen.setFont(Theme.font(Font.PLAIN, 12));
                lblTen.setForeground(Theme.TEXT);

                pnlName.add(lblSku);
                pnlName.add(lblTen);

                JPanel rRight = new JPanel(new GridLayout(1, 3, 6, 0));
                rRight.setOpaque(false);
                rRight.setPreferredSize(new Dimension(210, 26));

                JLabel lblSl = new JLabel(String.valueOf(d.getSoLuong()), SwingConstants.CENTER);
                JLabel lblGia = new JLabel(Theme.money(d.getSanPham().getDonGia()), SwingConstants.RIGHT);
                JLabel lblTien = new JLabel(Theme.money(Math.round(d.getDonGia() * d.getSoLuong())), SwingConstants.RIGHT);
                lblSl.setFont(Theme.font(Font.PLAIN, 12));
                lblGia.setFont(Theme.font(Font.PLAIN, 12));
                lblTien.setFont(Theme.font(Font.BOLD, 12));
                lblTien.setForeground(Theme.TEXT);

                rRight.add(lblSl);
                rRight.add(lblGia);
                rRight.add(lblTien);

                row.add(pnlName, BorderLayout.CENTER);
                row.add(rRight, BorderLayout.EAST);
                add(row);
            }

            add(Box.createVerticalStrut(6));
            add(new DashedLine());
            add(Box.createVerticalStrut(6));

            // 5. Tính tiền: Tạm tính & Giảm giá (nếu có)
            if (hd.tamTinh() != hd.canThanhToan()) {
                add(metaRow("Tạm tính:", Theme.money(hd.tamTinh())));
                String giamTxt = hd.getMaGiamGia() == null ? "Giảm giá:" : "Giảm giá (" + hd.getMaGiamGia().getMa() + "):";
                add(metaRow(giamTxt, hd.tienGiam() > 0 ? "-" + Theme.money(hd.tienGiam()) : Theme.money(0)));
                add(Box.createVerticalStrut(4));
                add(new DashedLine());
                add(Box.createVerticalStrut(4));
            }

            // 6. Khu vực Tổng cộng / Tổng tiền khách trả / Tiền trả lại (Mũi tên đỏ trong ảnh thực tế)
            add(totalRow("Tổng cộng:", Theme.money(tongCong)));
            add(metaRowBold("Tổng tiền khách trả:", Theme.money(tienKhachTra)));
            add(indentRow("+ " + phuongThuc + ":", Theme.money(tienKhachTra)));
            add(indentRowBold("+ Tiền trả lại:", Theme.money(tienTraLai), Theme.GREEN));

            add(Box.createVerticalStrut(6));
            add(new DashedLine());
            add(Box.createVerticalStrut(6));

            // 7. Khu vực thành viên & Tích lũy điểm (Y hệt mẫu ảnh)
            add(metaRowBold("Tên khách:", tenKhach));
            add(metaRow("Mã thẻ:", maThe));
            if (kh != null) {
                add(metaRow("Hạng thẻ:", hangThe));
                add(metaRowBold("Giá trị thưởng tích lũy:", String.format("%,d điểm", diemTichLuy)));
                if (diemCong > 0) {
                    add(indentRow("+ Điểm tích lũy đợt này:", String.format("+%,d điểm", diemCong)));
                }
            }
            add(metaRow("               Tích lũy đến:", ngay));

            add(Box.createVerticalStrut(6));
            add(new DashedLine());
            add(Box.createVerticalStrut(6));

            // 8. Chân trang pháp lý & Lời cảm ơn
            add(centerLabel("(Giá đã bao gồm thuế GTGT)", Font.PLAIN, 11, Theme.MUTED));
            add(Box.createVerticalStrut(2));
            add(centerLabel("Lưu ý: Siêu thị / Cửa hàng chỉ xuất hóa đơn trong ngày", Font.PLAIN, 11, Theme.MUTED));
            add(Box.createVerticalStrut(2));
            add(centerLabel("Trân trọng cảm ơn Quý khách!", Font.BOLD, 12, Theme.DARK));
            add(Box.createVerticalStrut(8));

            // 9. QR Code thanh toán / Zalo OA siêu thị
            add(centerLabel("Khách hàng vui lòng quét mã QRcode", Font.PLAIN, 11, Theme.MUTED));
            add(centerLabel("để mua hàng qua Zalo của siêu thị", Font.PLAIN, 11, Theme.MUTED));
            add(Box.createVerticalStrut(6));

            Image qrImg = VietQRUtil.taoAnhQR(tongCong, hd.getMaHD(), 125);
            if (qrImg != null) {
                JLabel lblQr = new JLabel(new ImageIcon(qrImg));
                lblQr.setAlignmentX(Component.CENTER_ALIGNMENT);
                add(lblQr);
                add(Box.createVerticalStrut(6));
            }

            add(centerLabel("* " + hd.getMaHD() + " *", Font.BOLD, 13, Theme.DARK));
        }

        private JLabel centerLabel(String text, int style, int size, Color color) {
            JLabel l = new JLabel(text, SwingConstants.CENTER);
            l.setFont(Theme.font(style, size));
            l.setForeground(color);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            return l;
        }

        private JPanel metaRow(String k, String v) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(2, 0, 2, 0));

            JLabel lk = new JLabel(k);
            lk.setFont(Theme.font(Font.PLAIN, 12));
            lk.setForeground(Theme.MUTED);

            JLabel lv = new JLabel(v, SwingConstants.RIGHT);
            lv.setFont(Theme.font(Font.PLAIN, 12));
            lv.setForeground(Theme.TEXT);

            p.add(lk, BorderLayout.WEST);
            p.add(lv, BorderLayout.EAST);
            return p;
        }

        private JPanel metaRowBold(String k, String v) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(2, 0, 2, 0));

            JLabel lk = new JLabel(k);
            lk.setFont(Theme.font(Font.BOLD, 12));
            lk.setForeground(Theme.TEXT);

            JLabel lv = new JLabel(v, SwingConstants.RIGHT);
            lv.setFont(Theme.font(Font.BOLD, 12));
            lv.setForeground(Theme.TEXT);

            p.add(lk, BorderLayout.WEST);
            p.add(lv, BorderLayout.EAST);
            return p;
        }

        private JPanel indentRow(String k, String v) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(1, 14, 1, 0));

            JLabel lk = new JLabel(k);
            lk.setFont(Theme.font(Font.PLAIN, 11));
            lk.setForeground(Theme.MUTED);

            JLabel lv = new JLabel(v, SwingConstants.RIGHT);
            lv.setFont(Theme.font(Font.PLAIN, 12));
            lv.setForeground(Theme.TEXT);

            p.add(lk, BorderLayout.WEST);
            p.add(lv, BorderLayout.EAST);
            return p;
        }

        private JPanel indentRowBold(String k, String v, Color c) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(1, 14, 1, 0));

            JLabel lk = new JLabel(k);
            lk.setFont(Theme.font(Font.BOLD, 12));
            lk.setForeground(Theme.TEXT);

            JLabel lv = new JLabel(v, SwingConstants.RIGHT);
            lv.setFont(Theme.font(Font.BOLD, 13));
            lv.setForeground(c);

            p.add(lk, BorderLayout.WEST);
            p.add(lv, BorderLayout.EAST);
            return p;
        }

        private JPanel totalRow(String k, String v) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(4, 0, 4, 0));

            JLabel lk = new JLabel(k);
            lk.setFont(Theme.font(Font.BOLD, 14));
            lk.setForeground(Theme.TEXT);

            JLabel lv = new JLabel(v, SwingConstants.RIGHT);
            lv.setFont(Theme.font(Font.BOLD, 15));
            lv.setForeground(Theme.PRIMARY);

            p.add(lk, BorderLayout.WEST);
            p.add(lv, BorderLayout.EAST);
            return p;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (pageIndex > 0) return NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            double pw = getWidth() > 0 ? getWidth() : getPreferredSize().width;
            double ph = getHeight() > 0 ? getHeight() : getPreferredSize().height;

            double scaleX = pageFormat.getImageableWidth() / pw;
            double scale = Math.min(scaleX, 1.0);
            g2.scale(scale, scale);

            printAll(g2);
            return PAGE_EXISTS;
        }
    }

    /** Đường nét đứt phong cách máy in nhiệt receipt. */
    private static class DashedLine extends JComponent {
        public DashedLine() {
            setPreferredSize(new Dimension(0, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0x9EACB9));
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0);
            g2.setStroke(dashed);
            int y = getHeight() / 2;
            g2.drawLine(0, y, getWidth(), y);
            g2.dispose();
        }
    }
}

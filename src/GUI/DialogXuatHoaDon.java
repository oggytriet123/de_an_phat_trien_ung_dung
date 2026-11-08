package GUI;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.print.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import Entity.DongGioHang;
import Entity.HoaDon;

/**
 * Hộp thoại hiển thị bản xem trước, xuất file và in hóa đơn thanh toán PetCare.
 * Tự động hiển thị ngay sau khi thanh toán thành công hoặc xem lại từ lịch sử hóa đơn.
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

        setSize(560, 750);
        setMinimumSize(new Dimension(500, 600));
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

        // 3. Bottom Action Bar
        btnDong = new RoundedButton("Đóng (Esc)", Color.WHITE, Theme.FIELD_BG, Theme.TEXT, Theme.BORDER, 8);
        btnDong.setPreferredSize(new Dimension(110, 40));
        btnDong.addActionListener(e -> dong());

        btnXuat = new RoundedButton("Xuất PDF / Tệp", Theme.BLUE_BG, new Color(0xD2E3ED), Theme.BLUE_TEXT, null, 8);
        btnXuat.setPreferredSize(new Dimension(135, 40));
        btnXuat.addActionListener(e -> xuatFile());

        btnIn = new RoundedButton("In hóa đơn", Theme.PRIMARY, Theme.PRIMARY_HOVER, Color.WHITE, null, 8);
        btnIn.setPreferredSize(new Dimension(135, 40));
        btnIn.addActionListener(e -> inHoaDon());

        JPanel pnlSouth = new JPanel(new BorderLayout());
        pnlSouth.setBackground(Color.WHITE);
        pnlSouth.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel lblTip = new JLabel("Hỗ trợ máy in POS K80 / A4 / A5 / PDF");
        lblTip.setFont(Theme.font(Font.PLAIN, 12));
        lblTip.setForeground(Theme.MUTED);
        pnlSouth.add(lblTip, BorderLayout.WEST);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        pnlBtns.add(btnDong);
        pnlBtns.add(btnXuat);
        pnlBtns.add(btnIn);
        pnlSouth.add(pnlBtns, BorderLayout.EAST);
        add(pnlSouth, BorderLayout.SOUTH);

        // Phím tắt ESC để đóng, Ctrl+P để in
        JRootPane root = getRootPane();
        root.registerKeyboardAction(e -> dong(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        root.registerKeyboardAction(e -> inHoaDon(), KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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

        JLabel lblSub = new JLabel("Xem trước mẫu in, in qua máy in nhiệt/laser hoặc xuất lưu tệp");
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
        if (onDong != null) {
            onDong.run();
        }
    }

    /** In trực tiếp hóa đơn qua hệ thống máy in (POS / Laser / Microsoft Print to PDF). */
    private void inHoaDon() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("PetCare_HoaDon_" + hd.getMaHD());

        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        // Căn lề in 18 pt (~6.3mm) gọn gàng
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

        // 1. Nếu người dùng chọn lưu file .pdf: thử in ra file PDF qua Microsoft Print to PDF
        if (path.toLowerCase().endsWith(".pdf")) {
            xuatPDFThucTe(f);
            return;
        }

        // 2. Mặc định xuất ra file HTML in ấn sắc nét chuẩn CSS vector
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
            // Không tìm thấy service tự động, mở PrintDialog để người dùng chọn máy in PDF
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

    private void ghiFileHtml(File f) throws IOException {
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            String ngay = hd.getNgayThanhToan() != null ? DT_FMT.format(hd.getNgayThanhToan()) : DT_FMT.format(hd.getNgayTao());
            String phuongThuc = hd.getPhuongThuc() != null ? hd.getPhuongThuc() : "Tiền mặt";

            w.println("<!DOCTYPE html>");
            w.println("<html lang=\"vi\">");
            w.println("<head>");
            w.println("<meta charset=\"UTF-8\">");
            w.println("<title>Hóa đơn " + hd.getMaHD() + " - PetCare</title>");
            w.println("<style>");
            w.println("  body { font-family: 'Segoe UI', Arial, sans-serif; background: #eef2f5; margin: 0; padding: 20px; color: #1f2a33; }");
            w.println("  .ticket { max-width: 420px; margin: 0 auto; background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 4px 14px rgba(0,0,0,0.08); }");
            w.println("  .center { text-align: center; }");
            w.println("  .brand { font-size: 16px; font-weight: bold; color: #1e3442; margin-bottom: 4px; }");
            w.println("  .sub { font-size: 12px; color: #6b7a86; line-height: 1.4; }");
            w.println("  .dash { border-top: 1px dashed #c0c8d0; margin: 14px 0; }");
            w.println("  .title { font-size: 16px; font-weight: bold; margin: 6px 0 2px 0; }");
            w.println("  .code { font-size: 13px; font-weight: bold; color: #2a5cb8; }");
            w.println("  .meta { font-size: 12px; margin: 4px 0; display: flex; justify-content: space-between; }");
            w.println("  .meta-label { color: #6b7a86; }");
            w.println("  .meta-val { font-weight: 500; }");
            w.println("  table { width: 100%; border-collapse: collapse; font-size: 12px; margin: 8px 0; }");
            w.println("  th { text-align: left; padding: 6px 0; border-bottom: 1px dashed #c0c8d0; color: #6b7a86; }");
            w.println("  td { padding: 6px 0; vertical-align: top; }");
            w.println("  .num { text-align: right; }");
            w.println("  .sum-row { display: flex; justify-content: space-between; font-size: 12px; margin: 4px 0; }");
            w.println("  .total { font-size: 16px; font-weight: bold; color: #2a5cb8; margin-top: 8px; }");
            w.println("  .thanks { text-align: center; font-size: 12px; color: #6b7a86; margin-top: 14px; font-style: italic; }");
            w.println("  .barcode { text-align: center; letter-spacing: 5px; font-family: monospace; font-size: 15px; margin-top: 10px; color: #1e3442; }");
            w.println("  @media print {");
            w.println("    body { background: #fff; padding: 0; }");
            w.println("    .ticket { box-shadow: none; padding: 0; width: 100%; max-width: 100%; }");
            w.println("    .no-print { display: none; }");
            w.println("  }");
            w.println("</style>");
            w.println("</head>");
            w.println("<body>");
            w.println("<div class=\"ticket\">");
            w.println("  <div class=\"center\">");
            w.println("    <div class=\"brand\">PHÒNG KHÁM THÚ Y & SPA PETCARE</div>");
            w.println("    <div class=\"sub\">12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM<br>Hotline: 0901.000.005 &nbsp;|&nbsp; petcare.vn</div>");
            w.println("    <div class=\"dash\"></div>");
            w.println("    <div class=\"title\">HÓA ĐƠN BÁN HÀNG & DỊCH VỤ</div>");
            w.println("    <div class=\"code\">" + hd.getMaHD() + "</div>");
            w.println("  </div>");
            w.println("  <div class=\"dash\"></div>");

            w.println("  <div class=\"meta\"><span class=\"meta-label\">Thời gian:</span><span class=\"meta-val\">" + ngay + "</span></div>");
            w.println("  <div class=\"meta\"><span class=\"meta-label\">Khách hàng:</span><span class=\"meta-val\">" + hd.getTenKH() + (hd.getMaKH().isEmpty() ? "" : " (" + hd.getMaKH() + ")") + "</span></div>");
            w.println("  <div class=\"meta\"><span class=\"meta-label\">Số điện thoại:</span><span class=\"meta-val\">" + (hd.getSdt().isEmpty() ? "---" : hd.getSdt()) + "</span></div>");
            w.println("  <div class=\"meta\"><span class=\"meta-label\">Phương thức:</span><span class=\"meta-val\">" + phuongThuc + "</span></div>");
            w.println("  <div class=\"meta\"><span class=\"meta-label\">Trạng thái:</span><span class=\"meta-val\" style=\"color:#1f8a4f;font-weight:bold;\">Đã thanh toán</span></div>");

            w.println("  <div class=\"dash\"></div>");
            w.println("  <table>");
            w.println("    <thead><tr><th>Món / Dịch vụ</th><th class=\"num\">SL</th><th class=\"num\">Đơn giá</th><th class=\"num\">Thành tiền</th></tr></thead>");
            w.println("    <tbody>");
            for (DongGioHang d : hd.getDong()) {
                w.println("      <tr>");
                w.println("        <td>" + d.getSanPham().getTen() + "</td>");
                w.println("        <td class=\"num\">" + d.getSoLuong() + "</td>");
                w.println("        <td class=\"num\">" + Theme.money(d.getSanPham().getDonGia()) + "</td>");
                w.println("        <td class=\"num\">" + Theme.money(d.thanhTien()) + "</td>");
                w.println("      </tr>");
            }
            w.println("    </tbody>");
            w.println("  </table>");

            w.println("  <div class=\"dash\"></div>");
            w.println("  <div class=\"sum-row\"><span>Tạm tính:</span><span>" + Theme.money(hd.tamTinh()) + "</span></div>");
            String giamTxt = hd.getMaGiamGia() == null ? "Giảm giá:" : "Giảm giá (" + hd.getMaGiamGia().getMa() + "):";
            w.println("  <div class=\"sum-row\"><span>" + giamTxt + "</span><span>" + (hd.tienGiam() > 0 ? "-" + Theme.money(hd.tienGiam()) : Theme.money(0)) + "</span></div>");
            w.println("  <div class=\"dash\"></div>");
            w.println("  <div class=\"sum-row total\"><span>TỔNG CỘNG:</span><span>" + Theme.money(hd.canThanhToan()) + "</span></div>");

            w.println("  <div class=\"dash\"></div>");
            w.println("  <div class=\"thanks\">Cảm ơn Quý khách và Hẹn gặp lại Bé Cưng!<br>Vui lòng giữ hóa đơn để đối soát dịch vụ.</div>");
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
                    new LineBorder(Theme.BORDER, 1, true),
                    new EmptyBorder(22, 22, 22, 22)
            ));
            setPreferredSize(new Dimension(420, tinhChieuCao()));
            setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

            buildComponents();
        }

        private int tinhChieuCao() {
            int base = 480;
            base += hd.getDong().size() * 32;
            return Math.max(520, base);
        }

        private void buildComponents() {
            // Header thương hiệu
            add(centerLabel("PHÒNG KHÁM THÚ Y & SPA PETCARE", Font.BOLD, 15, Theme.DARK));
            add(Box.createVerticalStrut(3));
            add(centerLabel("12 Nguyễn Văn Bảo, P.4, Q. Gò Vấp, TP.HCM", Font.PLAIN, 11, Theme.MUTED));
            add(centerLabel("Hotline: 0901.000.005  ·  petcare.vn", Font.PLAIN, 11, Theme.MUTED));
            add(Box.createVerticalStrut(10));
            add(new DashedLine());
            add(Box.createVerticalStrut(8));

            // Tiêu đề hóa đơn
            add(centerLabel("PHIẾU THANH TOÁN", Font.BOLD, 15, Theme.TEXT));
            add(Box.createVerticalStrut(2));
            add(centerLabel("Mã HĐ: " + hd.getMaHD(), Font.BOLD, 13, Theme.PRIMARY));
            add(Box.createVerticalStrut(8));
            add(new DashedLine());
            add(Box.createVerticalStrut(8));

            // Thông tin chung
            String ngay = hd.getNgayThanhToan() != null ? DT_FMT.format(hd.getNgayThanhToan()) : DT_FMT.format(hd.getNgayTao());
            String phuongThuc = hd.getPhuongThuc() != null ? hd.getPhuongThuc() : "Tiền mặt";

            add(metaRow("Thời gian:", ngay));
            add(metaRow("Khách hàng:", hd.getTenKH() + (hd.getMaKH().isEmpty() ? "" : " (" + hd.getMaKH() + ")")));
            add(metaRow("Số điện thoại:", hd.getSdt().isEmpty() ? "---" : hd.getSdt()));
            add(metaRow("Phương thức:", phuongThuc));
            add(Box.createVerticalStrut(8));
            add(new DashedLine());
            add(Box.createVerticalStrut(8));

            // Bảng danh sách mặt hàng
            JPanel tableHead = new JPanel(new BorderLayout());
            tableHead.setOpaque(false);
            JLabel th1 = new JLabel("Món / Dịch vụ");
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
            add(Box.createVerticalStrut(6));

            for (DongGioHang d : hd.getDong()) {
                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(3, 0, 3, 0));

                JLabel lblTen = new JLabel(d.getSanPham().getTen());
                lblTen.setFont(Theme.font(Font.PLAIN, 12));
                lblTen.setForeground(Theme.TEXT);

                JPanel rRight = new JPanel(new GridLayout(1, 3, 6, 0));
                rRight.setOpaque(false);
                rRight.setPreferredSize(new Dimension(210, 18));

                JLabel lblSl = new JLabel(String.valueOf(d.getSoLuong()), SwingConstants.CENTER);
                JLabel lblGia = new JLabel(Theme.money(d.getSanPham().getDonGia()), SwingConstants.RIGHT);
                JLabel lblTien = new JLabel(Theme.money(d.thanhTien()), SwingConstants.RIGHT);
                lblSl.setFont(Theme.font(Font.PLAIN, 12));
                lblGia.setFont(Theme.font(Font.PLAIN, 12));
                lblTien.setFont(Theme.font(Font.BOLD, 12));
                lblTien.setForeground(Theme.TEXT);

                rRight.add(lblSl);
                rRight.add(lblGia);
                rRight.add(lblTien);

                row.add(lblTen, BorderLayout.CENTER);
                row.add(rRight, BorderLayout.EAST);
                add(row);
            }

            add(Box.createVerticalStrut(8));
            add(new DashedLine());
            add(Box.createVerticalStrut(8));

            // Tổng kết tiền
            add(sumRow("Tạm tính:", Theme.money(hd.tamTinh()), false));
            String giamTxt = hd.getMaGiamGia() == null ? "Giảm giá:" : "Giảm giá (" + hd.getMaGiamGia().getMa() + "):";
            add(sumRow(giamTxt, hd.tienGiam() > 0 ? "-" + Theme.money(hd.tienGiam()) : Theme.money(0), false));
            add(Box.createVerticalStrut(6));
            add(new DashedLine());
            add(Box.createVerticalStrut(8));
            add(sumRow("TỔNG THANH TOÁN:", Theme.money(hd.canThanhToan()), true));

            add(Box.createVerticalStrut(14));
            add(new DashedLine());
            add(Box.createVerticalStrut(10));

            // Footer
            add(centerLabel("Cảm ơn Quý khách và Hẹn gặp lại Bé Cưng!", Font.ITALIC, 11, Theme.MUTED));
            add(Box.createVerticalStrut(2));
            add(centerLabel("Vui lòng giữ hóa đơn để đối soát dịch vụ", Font.PLAIN, 10, new Color(0x9AA6B2)));
            add(Box.createVerticalStrut(8));
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
            lv.setFont(Theme.font(Font.BOLD, 12));
            lv.setForeground(Theme.TEXT);

            p.add(lk, BorderLayout.WEST);
            p.add(lv, BorderLayout.EAST);
            return p;
        }

        private JPanel sumRow(String k, String v, boolean isTotal) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(isTotal ? 4 : 2, 0, isTotal ? 4 : 2, 0));

            JLabel lk = new JLabel(k);
            lk.setFont(Theme.font(isTotal ? Font.BOLD : Font.PLAIN, isTotal ? 14 : 12));
            lk.setForeground(isTotal ? Theme.TEXT : Theme.MUTED);

            JLabel lv = new JLabel(v, SwingConstants.RIGHT);
            lv.setFont(Theme.font(Font.BOLD, isTotal ? 16 : 12));
            lv.setForeground(isTotal ? Theme.PRIMARY : Theme.TEXT);

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

            // Căn tọa độ vào vùng in cho phép của trang giấy
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            double pw = getWidth() > 0 ? getWidth() : getPreferredSize().width;
            double ph = getHeight() > 0 ? getHeight() : getPreferredSize().height;

            double scaleX = pageFormat.getImageableWidth() / pw;
            double scale = Math.min(scaleX, 1.0); // Không phóng to vượt quá 100% nếu trang quá lớn
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
            g2.setColor(new Color(0xC6D0DA));
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0);
            g2.setStroke(dashed);
            int y = getHeight() / 2;
            g2.drawLine(0, y, getWidth(), y);
            g2.dispose();
        }
    }
}


package GUI;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import Bus.KhachHangBUS;
import Entity.KhachHang;
import Entity.ThuCung;

/**
 * Popup thêm khách hàng mới dùng chung cho:
 * - Danh mục > Khách hàng > Thêm khách hàng
 * - Xử lý > Lập hóa đơn > + Thêm khách hàng mới
 *
 * Tái sử dụng trọn vẹn giao diện và cơ chế sinh mã khách hàng (KHxxx) từ KhachHangDAO / KhachHangBUS.
 */
public class DialogThemKhachHang extends JDialog {
    private final KhachHangBUS bus;
    private KhachHang khachHangDaTao = null;

    private static final class PetRow {
        final JPanel panel = new JPanel(new GridBagLayout());
        final JTextField tfTen = new JTextField();
        final JComboBox<String> cbLoai = new JComboBox<>(new String[] {"Chó", "Mèo", "Thỏ", "Khác"});
        final JTextField tfCan = new JTextField();
    }

    public static KhachHang hienThi(Component parent, KhachHangBUS bus, String sdtGoiY, String tenGoiY) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        DialogThemKhachHang dlg = new DialogThemKhachHang(owner, bus, sdtGoiY, tenGoiY);
        dlg.setVisible(true);
        return dlg.khachHangDaTao;
    }

    public DialogThemKhachHang(Window owner, KhachHangBUS bus, String sdtGoiY, String tenGoiY) {
        super(owner, "Thêm khách hàng mới", Dialog.ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        initUI(sdtGoiY, tenGoiY);
        pack();
        setLocationRelativeTo(owner);
    }

    private JTextField taoTextField() {
        JTextField f = new JTextField();
        f.setFont(Theme.font(Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(340, 36));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(0xD5DCE1)), new EmptyBorder(0, 10, 0, 10)));
        return f;
    }

    private PetRow taoDongThuCung(JPanel list, List<PetRow> rows) {
        PetRow r = new PetRow();
        Font f = Theme.font(Font.PLAIN, 13);
        r.tfTen.setFont(f);
        r.cbLoai.setFont(f);
        r.tfCan.setFont(f);
        for (JTextField t : new JTextField[] {r.tfTen, r.tfCan}) {
            t.setBorder(new CompoundBorder(new LineBorder(new Color(0xD5DCE1)), new EmptyBorder(0, 8, 0, 8)));
        }
        RoundedButton del = new RoundedButton("×", Color.WHITE, new Color(0xFBE9E9), Theme.RED, Theme.BORDER, 8);
        del.setFont(new Font("Arial", Font.PLAIN, 20));
        r.panel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 6, 6);
        c.gridx = 0; c.weightx = 1;   r.panel.add(r.tfTen, c);
        c.gridx = 1; c.weightx = 0;   r.cbLoai.setPreferredSize(new Dimension(78, 32)); r.panel.add(r.cbLoai, c);
        c.gridx = 2;                  r.tfCan.setPreferredSize(new Dimension(64, 32)); r.panel.add(r.tfCan, c);
        c.gridx = 3; c.insets = new Insets(0, 0, 6, 0);
        del.setPreferredSize(new Dimension(32, 32));
        r.panel.add(del, c);
        r.tfTen.setPreferredSize(new Dimension(120, 32));
        del.addActionListener(e -> {
            rows.remove(r);
            list.remove(r.panel);
            list.revalidate();
            list.repaint();
        });
        rows.add(r);
        list.add(r.panel);
        list.revalidate();
        list.repaint();
        return r;
    }

    private void initUI(String sdtGoiY, String tenGoiY) {
        JTextField tfTen = taoTextField();
        if (tenGoiY != null && !tenGoiY.isBlank()) tfTen.setText(tenGoiY.trim());

        JTextField tfSdt = taoTextField();
        if (sdtGoiY != null && !sdtGoiY.isBlank()) tfSdt.setText(sdtGoiY.trim());

        JSpinner spDiem = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 10));
        spDiem.setFont(Theme.font(Font.PLAIN, 14));
        spDiem.setPreferredSize(new Dimension(340, 36));

        JPanel petList = new JPanel();
        petList.setLayout(new BoxLayout(petList, BoxLayout.Y_AXIS));
        petList.setBackground(Color.WHITE);
        List<PetRow> petRows = new ArrayList<>();
        taoDongThuCung(petList, petRows);

        JScrollPane petScroll = new JScrollPane(petList);
        petScroll.setBorder(new LineBorder(new Color(0xE2E8F0)));
        petScroll.setPreferredSize(new Dimension(340, 110));

        RoundedButton addPet = new RoundedButton("+ Thêm thú cưng", Color.WHITE, new Color(0xF3F6F8), Theme.ACCENT, Theme.BORDER, 8).pad(14, 6);
        addPet.setFont(Theme.font(Font.PLAIN, 12));
        addPet.addActionListener(e -> taoDongThuCung(petList, petRows));

        JLabel err = new JLabel(" ");
        err.setFont(Theme.font(Font.PLAIN, 12));
        err.setForeground(Theme.RED);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 8, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        JLabel lblTen = new JLabel("Họ tên khách hàng *");
        lblTen.setFont(Theme.font(Font.BOLD, 12));
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(lblTen, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(tfTen, c);

        JLabel lblSdt = new JLabel("Số điện thoại *");
        lblSdt.setFont(Theme.font(Font.BOLD, 12));
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(lblSdt, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(tfSdt, c);

        JLabel lblDiem = new JLabel("Điểm tích lũy ban đầu:");
        lblDiem.setFont(Theme.font(Font.BOLD, 12));
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(lblDiem, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 10, 0); form.add(spDiem, c);

        JPanel petHead = new JPanel(new BorderLayout());
        petHead.setBackground(Color.WHITE);
        JLabel lblPet = new JLabel("Thú cưng [tên · loài · cân nặng kg]");
        lblPet.setFont(Theme.font(Font.BOLD, 12));
        petHead.add(lblPet, BorderLayout.WEST);
        petHead.add(addPet, BorderLayout.EAST);
        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0); form.add(petHead, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 8, 0); form.add(petScroll, c);
        c.gridy = y; c.insets = new Insets(0, 0, 0, 0); form.add(err, c);

        RoundedButton ok = new RoundedButton("Lưu", Theme.ACCENT, Theme.ACCENT_HOVER, Color.WHITE, null, 10).pad(26, 9);
        RoundedButton cancel = new RoundedButton("Hủy", Color.WHITE, new Color(0xF3F6F8), Theme.TEXT, Theme.BORDER, 10).pad(26, 9);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(new EmptyBorder(8, 24, 18, 24));
        btns.add(cancel);
        btns.add(ok);

        cancel.addActionListener(e -> {
            khachHangDaTao = null;
            dispose();
        });

        ok.addActionListener(e -> {
            String ten = tfTen.getText().trim().replaceAll("\\s+", " ");
            String sdtRaw = tfSdt.getText().trim();
            String sdt = KhachHangBUS.chiSo(sdtRaw);

            if (ten.isEmpty()) {
                err.setText("Vui lòng nhập họ tên khách hàng.");
                tfTen.requestFocusInWindow();
                return;
            }
            if (sdt.length() < 9 || sdt.length() > 11) {
                err.setText("Số điện thoại phải gồm 9–11 chữ số.");
                tfSdt.requestFocusInWindow();
                return;
            }

            List<ThuCung> pets = new ArrayList<>();
            for (PetRow r : petRows) {
                String tenPet = r.tfTen.getText().trim();
                if (tenPet.isEmpty()) continue;
                double can = 0;
                try {
                    if (!r.tfCan.getText().trim().isEmpty()) {
                        can = Double.parseDouble(r.tfCan.getText().trim().replace(',', '.'));
                    }
                } catch (Exception ignored) {}
                pets.add(new ThuCung(null, tenPet, (String) r.cbLoai.getSelectedItem(), can));
            }

            try {
                if (bus.tonTaiSoDienThoai(sdt)) {
                    err.setText("Số điện thoại này đã tồn tại cho một khách hàng khác.");
                    tfSdt.requestFocusInWindow();
                    return;
                }
                int diem = ((Number) spDiem.getValue()).intValue();
                String maMoi = bus.them(ten, sdt, diem, pets);
                bus.taiLai();

                KhachHang kh = bus.timTheoMa(maMoi);
                if (kh == null) {
                    kh = new KhachHang(maMoi, ten, KhachHangBUS.dinhDangSdt(sdt),
                            KhachHangBUS.tinhHang(diem), diem, "", null, 0);
                }
                khachHangDaTao = kh;
                dispose();
            } catch (SQLException ex) {
                err.setText("Lỗi cơ sở dữ liệu: " + ex.getMessage());
            }
        });

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
    }
}

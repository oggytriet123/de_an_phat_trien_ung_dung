package GUI;

import java.awt.CardLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Bus.HoaDonBUS;
import Bus.KhachHangBUS;
import Bus.MaGiamGiaBUS;
import Bus.SanPhamBUS;
import Entity.HoaDon;

/** Menu "Xử Lý": chuyển giữa trang 20 ô hóa đơn và trang tạo / chi tiết hóa đơn. */
public class XuLyPanel extends JPanel implements Refreshable {
    private static final String TRANG_O = "quanLy";
    private static final String TRANG_TAO = "tao";

    private final CardLayout cards = new CardLayout();
    private final HoaDonBUS bus;
    private final QuanLyHoaDonPanel quanLy;
    private final TaoHoaDonPanel tao;
    private boolean dangTao;   // đang hiển thị trang tạo / chi tiết hóa đơn

    public XuLyPanel(HoaDonBUS bus, SanPhamBUS spBus, MaGiamGiaBUS mgBus, KhachHangBUS khBus) {
        super(null);
        setLayout(cards);
        this.bus = bus;
        quanLy = new QuanLyHoaDonPanel(bus, this::moO, this::taoMoi);
        tao = new TaoHoaDonPanel(bus, spBus, mgBus, khBus, this::veQuanLy);
        add(quanLy, TRANG_O);
        add(tao, TRANG_TAO);
        cards.show(this, TRANG_O);
    }

    /** Nhấn ô: ô trống thì tạo hóa đơn mới, ô có hóa đơn thì mở lại để xử lý tiếp. */
    private void moO(int viTri) {
        HoaDon nhap = bus.layO(viTri) == null ? bus.taoNhap(viTri) : bus.moNhap(viTri);
        tao.batDau(nhap);
        dangTao = true;
        cards.show(this, TRANG_TAO);
    }

    /** Nút "+ Tạo hóa đơn mới": mở hóa đơn mới ở ô trống đầu tiên. */
    private void taoMoi() {
        int i = bus.oTrongDauTien();
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "Cả 20 ô đều đang có hóa đơn.\nHãy thanh toán bớt hóa đơn để giải phóng ô.",
                    "Hết ô trống", JOptionPane.WARNING_MESSAGE);
            return;
        }
        moO(i);
    }

    /** Menu "Quản lý hóa đơn": đang soạn hóa đơn thì hỏi xác nhận như nút Hủy rồi mới rời trang. */
    public void hienQuanLy() {
        if (dangTao) tao.yeuCauHuy();
        else veQuanLy(null);
    }

    private void veQuanLy(String thongBao) {
        dangTao = false;
        quanLy.lamMoi();
        if (thongBao != null) quanLy.thongBao(thongBao);
        cards.show(this, TRANG_O);
    }

    @Override public void lamMoi() {
        quanLy.lamMoi();
    }
}

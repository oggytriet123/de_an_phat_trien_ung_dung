package Entity;

/**
 * Lựa chọn quyền trong combobox — dùng cho form thêm/sửa nhân viên.
 */
public class QuyenOption {
    public final int maQuyen;
    public final String tenQuyen;

    public QuyenOption(int maQuyen, String tenQuyen) {
        this.maQuyen = maQuyen;
        this.tenQuyen = tenQuyen;
    }

    @Override
    public String toString() {
        return tenQuyen;
    }
}

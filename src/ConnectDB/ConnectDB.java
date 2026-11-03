package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Kết nối SQL Server linh hoạt: tự động nhận diện và thử các cấu hình,
 * mật khẩu phổ biến (123456, sapassword, sa, 12345678) trên localhost:1433 hoặc SQLEXPRESS.
 */
public final class ConnectDB {
    private static final String[] PASSWORDS = {
        "123456", "sapassword", "sa", "12345678", "123", "1234", "12345", "123456789", "admin", "root"
    };
    private static final String[] URLS = {
        "jdbc:sqlserver://localhost:1433;databaseName=PetCare;encrypt=true;trustServerCertificate=true;loginTimeout=2",
        "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=PetCare;encrypt=true;trustServerCertificate=true;loginTimeout=2",
        "jdbc:sqlserver://localhost;databaseName=PetCare;encrypt=true;trustServerCertificate=true;loginTimeout=2"
    };

    private static String workingUrl = null;
    private static String workingPassword = null;
    private static long lastFailTime = 0;

    private ConnectDB() {}

    public static Connection getConnection() throws SQLException {
        // Nếu cấu hình đã được xác thực trước đó, sử dụng ngay
        if (workingUrl != null) {
            try {
                return DriverManager.getConnection(workingUrl, "sa", workingPassword);
            } catch (SQLException e) {
                workingUrl = null;
                workingPassword = null;
            }
        }

        // Tránh bị treo UI khi CSDL hoàn toàn offline
        if (System.currentTimeMillis() - lastFailTime < 2500) {
            throw new SQLException("CSDL SQL Server đang offline hoặc chưa bật TCP/IP cổng 1433.");
        }

        SQLException lastEx = null;
        for (String url : URLS) {
            for (String pw : PASSWORDS) {
                try {
                    Connection conn = DriverManager.getConnection(url, "sa", pw);
                    workingUrl = url;
                    workingPassword = pw;
                    System.out.println("[ConnectDB] Đã kết nối CSDL thành công: " + url + " (sa / " + pw + ")");
                    return conn;
                } catch (SQLException e) {
                    lastEx = e;
                }
            }
        }

        lastFailTime = System.currentTimeMillis();
        throw lastEx != null ? lastEx : new SQLException("Không thể kết nối đến SQL Server localhost.");
    }
}

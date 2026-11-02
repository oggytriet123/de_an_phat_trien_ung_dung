package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Kết nối SQL Server. Sửa URL / USER / PASSWORD cho khớp máy của bạn,
 * sau đó chạy file data/PetCare.sql để tạo CSDL.
 */
public final class ConnectDB {
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=PetCare;encrypt=true;trustServerCertificate=true;loginTimeout=3";
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";
    private static volatile String activePassword = null;

    private ConnectDB() {}

    public static Connection getConnection() throws SQLException {
        if (activePassword != null) {
            return DriverManager.getConnection(URL, USER, activePassword);
        }
        try {
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            activePassword = PASSWORD;
            return con;
        } catch (SQLException e) {
            try {
                Connection con = DriverManager.getConnection(URL, USER, "123456");
                activePassword = "123456";
                return con;
            } catch (SQLException ex) {
                throw e;
            }
        }
    }
}

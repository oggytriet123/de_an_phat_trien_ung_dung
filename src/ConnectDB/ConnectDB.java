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

    private ConnectDB() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

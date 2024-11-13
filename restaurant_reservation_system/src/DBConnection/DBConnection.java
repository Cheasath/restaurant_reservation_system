package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/restaurant";
        String username = "root";
        String password = "Asath@mysql11";
        return DriverManager.getConnection(url, username, password);
    }
}

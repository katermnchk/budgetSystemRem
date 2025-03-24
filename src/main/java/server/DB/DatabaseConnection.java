package server.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection dbConnection;
    private static DatabaseConnection instance;
    
    private static final String URL = "jdbc:postgresql://localhost:5432/budgetsystem";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgresql";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к БД", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

}

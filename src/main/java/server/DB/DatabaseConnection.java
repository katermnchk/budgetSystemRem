package server.DB;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseConnection {
    public static Connection dbConnection;
    private static DatabaseConnection instance;
    private Statement statement;
    private DataSource dataSource;

    private static final String URL = "jdbc:postgresql://localhost:5432/budgetsystem";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgresql";

    private DatabaseConnection() throws SQLException {
        try {
            this.dbConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            this.statement = dbConnection.createStatement();
            System.out.println("Подключение к базе данных успешно!");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
            throw e;
        }
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return dbConnection;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        if (dbConnection == null) {
            throw new IllegalStateException("Соединение с БД не установлено");
        }
        return dbConnection.prepareCall(sql);
    }

    public ArrayList<String[]> getArrayResult(String query) {
        ArrayList<String[]> masResult = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery(query)) {
            int count = resultSet.getMetaData().getColumnCount();

            while (resultSet.next()) {
                String[] arrayString = new String[count];
                for (int i = 1; i <= count; i++) {
                    arrayString[i - 1] = resultSet.getString(i);
                }
                masResult.add(arrayString);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return masResult;
    }

    public static void testConnection() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Тестовое подключение успешно!");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        }
    }
}
